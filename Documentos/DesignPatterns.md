# Design Patterns

## 1. Factory (Fábrica) - Rafa

**Objetivo:**  
Centralizar a criação de diálogos do app, permite criar AlertDialogs de forma consistente e limpa, sem repetir código.

**Situação:**  
O app precisa mostrar diálogos de confirmação para **limpar produções** ou **criar um novo Excel**, com ações diferentes dependendo do botão clicado, no futuro, ao aplicarmos várias roles, iríamos ter de reescrever cada diálogo para cada user e isso ía crescer o código.

---

## Como o DialogFactory se aplica

- **Fábrica (Factory):** `DialogFactory`  
  - É o objeto singleton que cria os diálogos (`AlertDialog`) para qualquer Activity que precise, sem expor a lógica de construção.  
  - Contém funções como `criarDialogoLimpeza()` e `criarDialogoRegeneracao()`.

- **Client (Cliente):** `MainActivity`  
  - Chama a fábrica e passa o **contexto** e a **ação a executar** quando o utilizador confirma.  
  - Não precisa saber como o diálogo é construído, apenas recebe um AlertDialog pronto para mostrar.

**Fluxo de funcionamento:**

1. O `MainActivity` quer mostrar um diálogo de confirmação.  
2. Chama a função correspondente da `DialogFactory`, passa:  
   - `context` → o contexto da Activity  
   - `onConfirm` → a função a executar quando o user clicar “Sim”  
3. A fábrica cria o `AlertDialog` com título, mensagem, botões e ação positiva.  
4. O MainActivity chama `.show()` no diálogo retornado, mostrando-o na tela.  
5. Quando o utilizador clica “Sim”, a lambda `onConfirm` é executada (por exemplo, `limparProducoes()` ou `regenerarExcelCompleto()`).

---

## Vantagens:

- Mantém a lógica de **criação de diálogos separada** do código da Activity.  
- Evita **repetição de código** em várias Activities ou funções.  
- Facilita **alterar títulos, mensagens ou botões** de todos os diálogos num único lugar.  
- Permite passar qualquer **ação personalizada** para o botão “Sim”, torna o diálogo **flexível e reutilizável**.


---

## 2. Singleton - Davide

**Objetivo:** Garantir que **uma classe tenha apenas uma instância** e fornecer um **ponto global de acesso** à classe.

---

## Como funciona no nosso caso

- **O que usar como Singleton:** Serviço de envio de emails  
- **Por que usar:**  
  - Só precisamos de **uma instância do serviço de email** para enviar o relatório diário.  
  - Evita criar múltiplas conexões desnecessárias com o servidor de email durante o dia.  
  - Garante que todas as partes do app (registro de bolos, relatórios, logs) usam **o mesmo serviço** para enviar emails.

**Fluxo simplificado:**

1. O app inicia e acessa a **instância única do serviço de email**.  
2. Quando chega o fim do dia, o relatório diário usa **essa mesma instância** para enviar o email ao gerente.  

---

## Vantagens

- Evita criar múltiplas instâncias do serviço de email, economiza recursos.  
- Garante que todos os emails enviados usam **as mesmas configurações e credenciais**.  
- Facilita a manutenção e futuras alterações no serviço de email, pois há **uma única instância centralizada**.


---
## 3. Facade (Fachada) - Zé

**Objetivo:** Fornecer uma **interface simplificada** para um conjunto de funcionalidades complexas.

### Como funciona no nosso caso

- **O que usar como Facade:** Registro de vendas e geração de relatórios e design 
- **Por que usar:**  
  - O app precisa registar vendas, atualizar relatórios, gravar logs e enviar emails.  
  - Cada função envolve vários passos internos e classes diferentes.  
  - A **fachada** cria uma interface única para o app, esconde toda a complexidade interna.
  - Não é apenas design frontend mas também "design" backend com a separação bonita de funções.

### Fluxo simplificado

1. `CafeteriaFacade.registrar_venda(bolo)` → adiciona venda ao sumário, atualiza relatório, grava log  
2. `CafeteriaFacade.enviar_relatorio_diario()` → gera relatório, escolhe meio de envio, envia ao gerente

### Vantagens

- Simplifica o uso do sistema para o app ou outras partes do código.  
- Esconde complexidade interna, tornando o código mais limpo e fácil de manter.  
- Facilita mudanças internas sem impactar quem usa a fachada.

---
## 4. Strategy (Estratégia) - Samuel

**Objetivo:** Permitir que um algoritmo ou comportamento seja **trocado dinamicamente** sem modificar o objeto que o usa.

---

## Como funciona no nosso caso

- **O que usar como Strategy:** Formas de envio do relatório diário  
- **Por que usar:**  
  - O app pode enviar o relatório para o gerente de diferentes maneiras:  
    - **Email**  
    - **Mensagem via WhatsApp**
    - **Mensagem via Telegram**
  - Cada método de envio é encapsulado em uma **estratégia separada**. Ou seja, numa função para cada tipo de envio.
  - O app ou o user decide em tempo de execução qual estratégia usar, sem alterar o código que gera os dados do relatório.

**Fluxo simplificado:**

1. O relatório diário é gerado.  
2. O app ou user escolhe a **estratégia de envio** conforme a configuração ou preferência.  
3. O relatório é enviado através da estratégia escolhida (email, Telegram ou WhatsApp).

**AVISO PARA QUEM ESCOLHER ESTE:**
- Não precisamos de implementar o envio por whatsapp, telegram ou qualquer outro método, este design pattern serve apenas para, caso algum dia queiramos adicionar outro método de envio, o código ser **TOTALMENTE INDEPENDENTE** da função de envio.

---

## Vantagens

- Permite **trocar a forma de envio** facilmente, sem modificar a lógica do relatório.  
- Evita condicionais complexas espalhadas pelo sistema.  
- Facilita a manutenção e adição de novos métodos de envio no futuro.


---

## 5. Template Method (Método Template) - Rodrigo

**Objetivo:** Definir a **estrutura geral de um algoritmo**, permite que algumas etapas sejam implementadas por subclasses.

---

## Como funciona no nosso caso

- **O que usar como Template:** Processo de geração de relatórios  
- **Por que usar:**  
  - O app gera relatórios **diários** e **semanais** (SEMANAIS ERA FIXE NO FUTURO???).  
  - O processo geral é o mesmo: coletar dados, formatar informações, enviar ao gerente.  
  - Algumas etapas podem variar:  
    - **Relatório diário:** inclui apenas o total de bolos vendidos.  
    - **Relatório semanal:** inclui totais diários, médias e gráficos.  
  - O Template Method define **a estrutura geral**, enquanto as subclasses implementam os detalhes específicos.

**Fluxo simplificado:**

1. Iniciar geração de relatório (template).  
2. Coletar dados (implementado por subclasses conforme tipo de relatório).  
3. Formatar dados (implementado por subclasses).  
4. Enviar relatório ao gerente (mesmo passo para todos os tipos de relatório).

---

## Vantagens

- Evita duplicação de código entre relatórios diários e semanais.  
- Mantém a consistência da lógica geral de geração de relatórios.  
- Facilita adicionar novos tipos de relatórios no futuro, implementando apenas os detalhes específicos.
