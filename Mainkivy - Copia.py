# Mainkivy.py
# carrega Sheets uma única vez ao iniciar, usa JSON depois, back = 1 passo
# exportação por e-mail com confirmação e salvamento síncrono pré-envio

from threading import Thread
from datetime import datetime, date
from pathlib import Path
import json, os, tempfile

from kivy.lang import Builder
from kivy.clock import Clock
from kivy.core.window import Window
from kivy.metrics import Metrics
from kivy.properties import StringProperty, NumericProperty
from kivy.factory import Factory

from kivymd.app import MDApp
from kivymd.uix.card import MDCard
from kivymd.uix.dialog import MDDialog
from kivymd.uix.button import MDRaisedButton, MDFlatButton
from kivymd.toast import toast

import gspread
from oauth2client.service_account import ServiceAccountCredentials

# --- seus módulos
from Produtos import produto
from salvarexecel import salvar_producao_diaria


# ---------------------------- Tiles ----------------------------
class TipoTile(MDCard):
    title = StringProperty("")
    tipo = StringProperty("")


class ProdutoTile(MDCard):
    title = StringProperty("")
    nome = StringProperty("")
    tipo = StringProperty("")
    quantity = NumericProperty(0)


# ---------------------------- App ----------------------------
class AppCozinha(MDApp):
    # responsividade (usados no KV)
    cols_grid = NumericProperty(4)
    tipo_font_sp = NumericProperty(22)
    produto_font_sp = NumericProperty(16)
    tipo_card_height_dp = NumericProperty(140)
    produto_card_height_dp = NumericProperty(120)
    data_hoje_str = StringProperty("")

    # estado geral
    keypad_value = StringProperty("")
    produto_selecionado = StringProperty("")
    tipo_atual = StringProperty("")
    qtd_atual_display = NumericProperty(0)  # usado na tela de detalhe

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self._base_dir = Path(__file__).resolve().parent
        self._kv_path = self._base_dir / "ui" / "screens.kv"
        self._creds_json = self._base_dir / "projeto-ibersol-467808-eb721841ac2c.json"

        # arquivo Excel completo da loja
        self._xlsx_path = self._base_dir / "Loja012_2025.xlsx"

        # cache local
        self._cache_path = self._base_dir / "cache_produtos.json"
        self._versao_menu_cache = None

        # flags
        self._data_loaded = False      # já temos dados em memória (de JSON/Sheets)
        self._loading = False          # há um carregamento em andamento?

        # dados em memória
        self.dicionario_produtos = {}

        # dialogos
        self._dialog_export = None

        # nome da planilha (uma por loja)
        self._spreadsheet_title = "ProdutosdaLoja012"

    # ---------------- ciclo de vida ----------------
    def build(self):
        self.theme_cls.theme_style = "Light"
        self.theme_cls.primary_palette = "Indigo"

        if not self._kv_path.exists():
            raise FileNotFoundError(f"KV não encontrado: {self._kv_path}")
        root = Builder.load_file(str(self._kv_path))

        Window.bind(size=self.update_breakpoints)
        self.update_breakpoints()
        return root

    def on_start(self):
        """
        ARRANQUE:
          1) tenta carregar JSON local p/ UI rápida;
          2) em paralelo, checa versão no Sheets e atualiza JSON se mudou.
        Depois disso, durante o uso, tudo vem do JSON em memória.
        """
        # 1) UI imediata via JSON (se existir)
        if self._cache_path.exists():
            try:
                self._carregar_cache_local()
                self._data_loaded = True
            except Exception as e:
                print("[cache] falha ao ler:", e)

        # 2) em paralelo, tentativa de atualizar JSON a partir do Sheets (silencioso)
        Thread(target=self._atualizar_cache_do_sheets_startup, daemon=True).start()

    # ---------------- navegação ----------------
    def go(self, screen_name: str):
        """Navegação direta pelos botões da Home."""
        self.root.current = screen_name
        if screen_name == "producao":
            # usa só o que já está em memória; agenda para o próximo frame
            if self._data_loaded:
                Clock.schedule_once(lambda dt: self._mostrar_tipos(), 0)
            else:
                self._set_status("⏳ A carregar dados...")

    def nav_back(self):
        """Volta SEMPRE um passo."""
        cur = self.root.current
        if cur == "detalhe_produto":
            if self.tipo_atual:
                self.abrir_tipo(self.tipo_atual)
            self.root.current = "producao"
        elif cur == "producao":
            if self.tipo_atual:     # estava vendo produtos de um tipo
                self.voltar_aos_tipos()
            else:
                self.root.current = "home"
        elif cur == "inventario":
            self.root.current = "home"
        elif cur == "resumo":
            self.root.current = "producao"
        else:
            pass

    def desmarcar_produto(self):
        """Limpa a seleção e volta para a lista adequada."""
        self.produto_selecionado = ""
        if self.tipo_atual:
            Clock.schedule_once(lambda dt: self.abrir_tipo(self.tipo_atual), 0)
        else:
            Clock.schedule_once(lambda dt: self._mostrar_tipos(), 0)
        self._mostrar_botoes_acao(False)

    # ---------------- helpers UI ----------------
    def _set_status(self, msg: str):
        try:
            self.root.get_screen("producao").ids.status.text = msg
        except Exception as e:
            print("[status]", e)

    def _toast(self, msg: str):
        try:
            toast(msg)
        except Exception:
            print(msg)

    def _limpar_grid(self):
        try:
            self.root.get_screen("producao").ids.grid.clear_widgets()
        except Exception as e:
            print("[grid]", e)

    def _mostrar_botoes_acao(self, mostrar: bool):
        tela = self.root.get_screen("producao")
        # action_bar pode existir ou não
        try:
            box = tela.ids['action_bar']
            box.opacity = 1 if mostrar else 0
            box.disabled = not mostrar
        except Exception:
            pass
        # selected_label pode existir ou não
        try:
            lbl = tela.ids['selected_label']
            lbl.opacity = 1 if mostrar else 0
            lbl.text = f"Selecionado: [b]{self.produto_selecionado}[/b]" if mostrar else ""
        except Exception:
            pass

    # ---------------- responsividade ----------------
    def update_breakpoints(self, *args):
        width_dp = Window.width / max(Metrics.density, 1)

        if width_dp >= 1280:
            self.cols_grid = 4; self.tipo_font_sp = 22; self.produto_font_sp = 16
            self.tipo_card_height_dp = 140; self.produto_card_height_dp = 120
        elif width_dp >= 980:
            self.cols_grid = 3; self.tipo_font_sp = 22; self.produto_font_sp = 16
            self.tipo_card_height_dp = 150; self.produto_card_height_dp = 125
        elif width_dp >= 720:
            self.cols_grid = 3; self.tipo_font_sp = 20; self.produto_font_sp = 16
            self.tipo_card_height_dp = 160; self.produto_card_height_dp = 130
        else:
            self.cols_grid = 2; self.tipo_font_sp = 20; self.produto_font_sp = 16
            self.tipo_card_height_dp = 170; self.produto_card_height_dp = 140

    # --------------- cache JSON local ---------------
    def _carregar_cache_local(self):
        data = json.loads(self._cache_path.read_text(encoding="utf-8"))
        self._versao_menu_cache = data.get("versao_menu")
        itens = data.get("itens", {})
        dicio = {}
        for nome, info in itens.items():
            dicio[nome] = produto(
                nome,
                info.get("tipo") or "Sem Tipo",
                int(info.get("quantidade") or 0),
            )
        self.dicionario_produtos = dicio
        # se já estamos na tela Produção, redesenha
        if self.root.current == "producao":
            Clock.schedule_once(lambda dt: self._mostrar_tipos(), 0)

    def _salvar_cache_local(self, versao_menu: str):
        data = {
            "versao_menu": versao_menu,
            "itens": {
                n: {"tipo": p.gettipo(), "quantidade": p.getquantidade()}
                for n, p in self.dicionario_produtos.items()
            },
        }
        with tempfile.NamedTemporaryFile("w", delete=False, encoding="utf-8") as tf:
            json.dump(data, tf, ensure_ascii=False)
            tmp = tf.name
        os.replace(tmp, self._cache_path)
        self._versao_menu_cache = versao_menu

    # --------------- Sheets: arranque (uma vez) ---------------
    def _atualizar_cache_do_sheets_startup(self):
        """No início do app: compara versão do Sheets com a do JSON.
        Se mudou, baixa dados e regrava o JSON. Depois usamos só o JSON."""
        if self._loading:
            return
        self._loading = True
        try:
            scope = [
                "https://spreadsheets.google.com/feeds",
                "https://www.googleapis.com/auth/drive",
            ]
            creds = ServiceAccountCredentials.from_json_keyfile_name(
                str(self._creds_json), scope
            )
            client = gspread.authorize(creds)

            sh = client.open(self._spreadsheet_title)

            # tenta achar 'Config' ou 'config'
            try:
                ws_cfg = sh.worksheet("Config")
            except gspread.WorksheetNotFound:
                try:
                    ws_cfg = sh.worksheet("config")
                except gspread.WorksheetNotFound:
                    ws_cfg = None

            versao_sheets = ""
            if ws_cfg is not None:
                try:
                    versao_sheets = ws_cfg.acell("B1").value or ""
                except Exception:
                    versao_sheets = ""

            precisa_baixar = not self._data_loaded or (
                versao_sheets and versao_sheets != self._versao_menu_cache
            )

            if precisa_baixar:
                ws = sh.sheet1  # 1ª aba: Nome | Tipo | Quantidade
                registros = ws.get_all_records()

                dicio = {}
                for item in registros:
                    nome = item.get("Nome")
                    tipo = item.get("Tipo") or "Sem Tipo"
                    try:
                        qtd = int(item.get("Quantidade") or 0)
                    except (ValueError, TypeError):
                        qtd = 0
                    if nome:
                        dicio[nome] = produto(nome, tipo, qtd)

                # atualiza memória e JSON (atômico)
                self.dicionario_produtos = dicio
                self._data_loaded = True
                self._salvar_cache_local(
                    versao_sheets or datetime.utcnow().isoformat()
                )
                Clock.schedule_once(lambda dt: self._mostrar_tipos(), 0)
                print("[startup] cache atualizado a partir do Sheets.")
            else:
                # já temos cache da mesma versão
                self._data_loaded = True
                print("[startup] usando cache local (mesma versão).")

        except Exception as e:
            # offline ou outro erro: usa cache se existir
            print("[startup] sem atualização do Sheets:", e)
            if self._cache_path.exists():
                self._data_loaded = True
                Clock.schedule_once(lambda dt: self._mostrar_tipos(), 0)
                Clock.schedule_once(
                    lambda dt: self._set_status("⚠️ Offline. Usando dados locais."),
                    0,
                )
            else:
                Clock.schedule_once(
                    lambda dt: self._set_status("❌ Sem internet e sem cache local."),
                    0,
                )
        finally:
            self._loading = False

    # --------------- Suporte à tela de detalhe ---------------
    def _sync_qtd_display(self):
        """Atualiza a qtd para a tela de detalhe antes da tela aparecer."""
        nome = self.produto_selecionado
        qtd = 0
        try:
            if nome in self.dicionario_produtos:
                qtd = int(self.dicionario_produtos[nome].getquantidade())
        except Exception:
            qtd = 0
        self.qtd_atual_display = qtd

    # --------------- Construção dinâmica da tela ---------------
    def _mostrar_tipos(self):
        self._set_status("Toque num TIPO para ver os produtos.")
        self._limpar_grid()
        self._mostrar_botoes_acao(False)
        self.tipo_atual = ""  # estamos na lista de tipos
        grid = self.root.get_screen("producao").ids.grid

        tipos = sorted({p.gettipo() for p in self.dicionario_produtos.values()})
        if not tipos:
            self._set_status("Nenhum tipo encontrado.")
            return

        for tipo in tipos:
            tile = TipoTile(title=tipo, tipo=tipo)
            tile.ids.hit.on_release = (lambda t=tipo: self.abrir_tipo(t))
            grid.add_widget(tile)

    def abrir_tipo(self, tipo: str):
        self.tipo_atual = tipo  # estamos dentro deste tipo
        self._set_status(f"Tipo: [b]{tipo}[/b]. Toque num produto.")
        self._limpar_grid()
        self._mostrar_botoes_acao(False)
        grid = self.root.get_screen("producao").ids.grid

        produtos_tipo = sorted(
            [
                (n, p.getquantidade())
                for n, p in self.dicionario_produtos.items()
                if p.gettipo() == tipo
            ],
            key=lambda x: x[0].lower(),
        )
        for nome, qtd in produtos_tipo:
            tile = ProdutoTile(title=nome, nome=nome, tipo=tipo, quantity=qtd)
            # Abre a tela de detalhe com teclado numérico embutido
            tile.ids.hit.on_release = (
                lambda n=nome, t=tipo: self.abrir_detalhe_produto(n, t)
            )
            grid.add_widget(tile)

    def voltar_aos_tipos(self):
        self._mostrar_tipos()

    # --------------- Tela de DETALHE ---------------
    def abrir_detalhe_produto(self, nome: str, tipo: str):
        """Vai para a tela de detalhes do produto selecionado."""
        self.produto_selecionado = nome
        self.tipo_atual = tipo
        self.keypad_value = ""
        self._sync_qtd_display()
        self.root.current = "detalhe_produto"

    def confirmar_ajuste_quantidade(self, direcao: int):
        """
        Aplica o valor digitado ao produto (direcao: +1 adicionar, -1 subtrair),
        salva no Excel e mantém na tela de detalhe.
        Evita números negativos.
        """
        try:
            valor = int(self.keypad_value or "0")
        except ValueError:
            valor = 0

        if valor <= 0:
            return

        nome = self.produto_selecionado
        if nome not in self.dicionario_produtos:
            return

        p = self.dicionario_produtos[nome]
        atual = int(p.getquantidade() or 0)

        if direcao == 1:
            p.aumentarquantidade(valor)
        else:
            # nunca deixar negativo
            decremento = min(valor, max(0, atual))
            if decremento > 0:
                p.diminuirquantidade(decremento)

        # atualiza display e limpa entrada
        self._sync_qtd_display()
        self.keypad_value = ""

        # salva no Excel (e cache) em background
        Thread(target=self._salvar_excel_bg, daemon=True).start()

        # se o resumo estiver aberto, atualiza
        self.atualizar_resumo()

    def cancelar_entrada(self):
        """Ignora o valor digitado e volta a mostrar 0."""
        self.keypad_value = ""

    # --------------- Teclado numérico ---------------
    def keypad_add_digit(self, d: str):
        novo = (self.keypad_value or "") + d
        if len(novo) <= 6 and novo.isdigit():
            self.keypad_value = str(int(novo)) if novo != "" else "0"

    def keypad_backspace(self):
        self.keypad_value = self.keypad_value[:-1] if self.keypad_value else ""

    def keypad_clear(self):
        self.keypad_value = ""

    # --------------- Tela resumo ------------------------------------------
    def abrir_resumo(self):
        self.root.current = "resumo"
        self._montar_resumo()

    def atualizar_resumo(self):
        # reconstrói a lista (ex.: após add/subtrair quantidade)
        if self.root.current == "resumo":
            self._montar_resumo()

    def _montar_resumo(self):
        """Constrói a tabela agrupada por tipo, com alternância de linhas e totais."""
        try:
            scr = self.root.get_screen("resumo")
        except Exception:
            return

        cont = scr.ids.table_container
        cont.clear_widgets()

        # data no título
        self.data_hoje_str = datetime.today().strftime("%d/%m/%Y")

        # agrupa por tipo
        por_tipo = {}
        for nome, prod in self.dicionario_produtos.items():
            tipo = prod.gettipo() or "Sem Tipo"
            por_tipo.setdefault(tipo, []).append((nome, int(prod.getquantidade() or 0)))

        # ordena tipos e produtos
        for tipo in sorted(por_tipo.keys(), key=str.lower):
            items = sorted(por_tipo[tipo], key=lambda t: t[0].lower())

            # cabeçalho do tipo
            header = Factory.TableGroupHeader()
            header.ids.left.text = tipo
            header.ids.right.text = "Quantidade"
            cont.add_widget(header)

            # linhas alternadas
            total_tipo = 0
            for i, (nome, qtd) in enumerate(items):
                row = Factory.TableRow()
                # zebra (linhas alternadas)
                row.bg_color = (1, 1, 1, 1) if (i % 2 == 0) else (0.97, 0.98, 1, 1)
                row.ids.left.text = nome
                row.ids.right.text = str(qtd)
                cont.add_widget(row)
                total_tipo += qtd

            # total do tipo
            total = Factory.TableTotal()
            total.ids.left.text = f"Total {tipo}"
            total.ids.right.text = str(total_tipo)
            cont.add_widget(total)

    # --------------- Salvar Excel + cache (background) ---------------
    def _salvar_excel_bg(self):
        try:
            salvar_producao_diaria(self.dicionario_produtos, data=date.today(), nome_arquivo=str(self._xlsx_path))
            # persiste também no cache local para próximos arranques
            self._salvar_cache_local(self._versao_menu_cache or datetime.utcnow().isoformat())
            data_str = date.today().strftime("%d-%m-%Y")
            msg = f"✅ Produção salva. Planilha atualizada na aba '{data_str}'."
            Clock.schedule_once(lambda dt: self._set_status(msg), 0)
        except Exception as e:
            print("[Excel] erro:", e)
            Clock.schedule_once(lambda dt: self._set_status(f"❌ Erro ao salvar: {e}"), 0)

    # --------------- Salvar Excel + cache (síncrono, para exportação) ---------------
    def _salvar_excel_sync(self) -> str:
        """
        Salva a produção do dia no Excel de forma síncrona (bloqueante)
        e retorna o caminho absoluto do arquivo gerado/atualizado.
        """
        salvar_producao_diaria(
            self.dicionario_produtos,
            data=date.today(),
            nome_arquivo=str(self._xlsx_path),
        )
        # Atualiza também o cache local (mesma lógica usada no save em background)
        self._salvar_cache_local(self._versao_menu_cache or datetime.utcnow().isoformat())
        return str(self._xlsx_path)

    # --------------- Exportação por e-mail (com confirmação) ---------------
    def abrir_confirmacao_exportar(self):
        """Abre dialog de confirmação para exportar a planilha por e-mail."""
        if self._dialog_export:
            try:
                self._dialog_export.dismiss()
            except Exception:
                pass
            self._dialog_export = None

        self._dialog_export = MDDialog(
            title="Deseja exportar produção?",
            text="Será enviado por e-mail o Excel COMPLETO com todas as abas.",
            buttons=[
                MDFlatButton(
                    text="NÃO",
                    on_release=lambda *a: self._fechar_dialog_export()
                ),
                MDRaisedButton(
                    text="SIM",
                    on_release=lambda *a: self._confirmar_exportacao()
                ),
            ],
            auto_dismiss=False,
        )
        self._dialog_export.open()

    def _fechar_dialog_export(self):
        if self._dialog_export:
            try:
                self._dialog_export.dismiss()
            except Exception:
                pass
            self._dialog_export = None

    def _confirmar_exportacao(self):
        self._fechar_dialog_export()
        self._executar_exportacao()

    def _executar_exportacao(self):
        """
        Executa a exportação: salva sincronamente o Excel com os dados mais atuais
        e em seguida envia por e-mail em background.
        """
        def _job():
            try:
                # 1) Garante que o arquivo contém as últimas alterações
                caminho = self._salvar_excel_sync()

                # 2) Envia o ARQUIVO COMPLETO por e-mail
                from email_service import EmailService
                assunto = f"Produção — Loja 012 — {datetime.now().strftime('%d/%m/%Y %H:%M')}"
                corpo = (
                    "Segue em anexo a planilha de produção completa.\n\n"
                    "Este e-mail foi enviado automaticamente pelo app de Gestão da Cozinha."
                )
                EmailService().send_excel(
                    filepath=caminho,
                    subject=assunto,
                    body=corpo,
                )

                # 3) Feedback para o usuário
                Clock.schedule_once(lambda dt: self._toast("📧 Exportado e enviado com sucesso."), 0)

            except Exception as e:
                print("[Exportação] erro:", e)
                Clock.schedule_once(lambda dt: self._toast(f"❌ Falha ao exportar/enviar: {e}"), 0)

        Thread(target=_job, daemon=True).start()


if __name__ == "__main__":
    AppCozinha().run()
