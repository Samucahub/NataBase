package com.belasoft.natabase_alpha

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import com.belasoft.natabase_alpha.utils.CacheManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager

class MainActivity : AppCompatActivity() {

    private lateinit var mapaProducao: MapaProducao
    private lateinit var recycler: RecyclerView
    private var producaoAtualIndex = 1
    private lateinit var currentAdapter: ItemProducaoAdapter
    private val MAX_PRODUCAO = 5

    private val calculadoraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val produtoNome = data?.getStringExtra("PRODUTO_NOME") ?: ""
            val tipoOperacao = data?.getStringExtra("TIPO_OPERACAO") ?: ""
            val valor = data?.getIntExtra("VALOR", 0) ?: 0
            val producaoIdx = data?.getIntExtra("PRODUCAO_INDEX", 1) ?: 1

            currentAdapter.atualizarItem(produtoNome, tipoOperacao, valor, producaoIdx)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val mapaCache = CacheManager.carregarProducao(this)
        mapaProducao = mapaCache ?: ExcelService.carregarMapaProducao(this)

        producaoAtualIndex = CacheManager.carregarProducaoIndex(this)

        findViewById<Button>(R.id.btnProducao).setOnClickListener {
            setContentView(R.layout.activity_producao)
            recycler = findViewById(R.id.recyclerProdutos)

            adicionarBotaoDebug()
            carregarCategorias()

            findViewById<Button>(R.id.action_salvar_producao).setOnClickListener {
                salvarProducaoAtual()
            }

            findViewById<Button>(R.id.action_resumo).setOnClickListener {
                val intent = Intent(this, ResumoActivity::class.java)
                startActivity(intent)
            }
        }

        findViewById<Button>(R.id.btnInventario).setOnClickListener {
            setContentView(R.layout.activity_inventario)
        }
    }

    private fun adicionarBotaoDebug() {
        val debugLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.END
            }
        }

        val limparButton = Button(this).apply {
            text = "Limpar"
            setBackgroundResource(R.drawable.button_back)
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 8, 0)
            }
            setOnClickListener {
                mostrarDialogoLimpeza()
            }
        }

        val regenerarButton = Button(this).apply {
            text = "Novo Excel"
            setBackgroundResource(R.drawable.button_back)
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                mostrarDialogoRegeneracao()
            }
        }

        debugLayout.addView(limparButton)
        debugLayout.addView(regenerarButton)

        val topBar = findViewById<LinearLayout>(R.id.topBar)
        topBar?.addView(debugLayout)
    }

    private fun mostrarDialogoLimpeza() {
        AlertDialog.Builder(this)
            .setTitle("Limpar Produções")
            .setMessage("Tem certeza que deseja limpar TODAS as produções? Esta ação não pode ser desfeita.")
            .setPositiveButton("Sim") { _, _ ->
                limparProducoes()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoRegeneracao() {
        AlertDialog.Builder(this)
            .setTitle("Criar Novo Excel")
            .setMessage("Tem certeza que deseja criar um NOVO arquivo Excel? Isto apagará o arquivo atual e criará um novo com a estrutura correta (sem Validade Exposição).")
            .setPositiveButton("Sim") { _, _ ->
                regenerarExcelCompleto()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun limparProducoes() {
        ExcelService.limparProducoes(this)

        producaoAtualIndex = 1
        CacheManager.salvarProducaoIndex(this, producaoAtualIndex)

        mapaProducao = ExcelService.carregarMapaProducao(this)
        CacheManager.salvarProducao(this, mapaProducao)

        Toast.makeText(this, "Produções limpas com sucesso!", Toast.LENGTH_SHORT).show()
        carregarCategorias()
    }

    private fun regenerarExcelCompleto() {
        mapaProducao = ExcelService.regenerarExcelCompleto(this)
        producaoAtualIndex = 1
        CacheManager.salvarProducaoIndex(this, producaoAtualIndex)
        CacheManager.salvarProducao(this, mapaProducao)

        Toast.makeText(this, "Novo Excel criado com estrutura correta!", Toast.LENGTH_SHORT).show()
        carregarCategorias()
    }

    private fun salvarProducaoAtual() {
        val sucesso = ExcelService.salvarProducao(this, mapaProducao, producaoAtualIndex)

        if (!sucesso) {
            Toast.makeText(this, "Produção 5 já foi preenchida.", Toast.LENGTH_LONG).show()
            return
        }

        producaoAtualIndex++

        if (producaoAtualIndex > MAX_PRODUCAO) {
            Toast.makeText(this, "Todas as 5 produções foram preenchidas!", Toast.LENGTH_LONG).show()
            producaoAtualIndex = MAX_PRODUCAO
        }

        CacheManager.salvarProducaoIndex(this, producaoAtualIndex)
        carregarCategorias()
        Toast.makeText(this, "Produção ${producaoAtualIndex-1} salva com sucesso!", Toast.LENGTH_SHORT).show()
    }

    private fun carregarCategorias() {
        val categorias = mapaProducao.itens
            .map { it.categoria }
            .filter { it.isNotBlank() }
            .distinct()

        println("DEBUG - Categorias carregadas: ${categorias.size}")
        categorias.forEachIndexed { index, cat ->
            println("DEBUG - Categoria $index: $cat")
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = CategoriaAdapter(categorias) { categoria ->
            abrirProdutosDaCategoria(categoria)
        }

        val dataFormatada = if (mapaProducao.data.isNotBlank()) {
            mapaProducao.data
        } else {
            val hoje = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
            hoje
        }

        findViewById<TextView>(R.id.statusText)?.text =
            "Produção $producaoAtualIndex - $dataFormatada"
    }

    private fun abrirProdutosDaCategoria(categoria: String) {
        val todosProdutosCategoria = mapaProducao.itens
            .filter { it.categoria == categoria }

        val produtosCategoria = todosProdutosCategoria.toMutableList()

        currentAdapter = ItemProducaoAdapter(
            produtosCategoria,
            producaoAtualIndex,
            onQuantidadeAlterada = { item ->
                val index = mapaProducao.itens.indexOfFirst { it.produto == item.produto && it.categoria == item.categoria }
                if (index != -1) {
                    val novaLista = mapaProducao.itens.toMutableList()
                    novaLista[index] = item
                    mapaProducao = mapaProducao.copy(itens = novaLista)
                    CacheManager.salvarProducao(this, mapaProducao)
                }
                Toast.makeText(this, "${item.produto} atualizado", Toast.LENGTH_SHORT).show()
            },
            salvarCallback = { itensAtualizados ->
                itensAtualizados.forEach { itemAtualizado ->
                    val index = mapaProducao.itens.indexOfFirst {
                        it.produto == itemAtualizado.produto && it.categoria == itemAtualizado.categoria
                    }
                    if (index != -1) {
                        val novaLista = mapaProducao.itens.toMutableList()
                        novaLista[index] = itemAtualizado
                        mapaProducao = mapaProducao.copy(itens = novaLista)
                    }
                }
                CacheManager.salvarProducao(this, mapaProducao)
            },
            onCalculadoraRequest = { produtoNome, tipoOperacao, producaoIndex ->
                val intent = Intent(this, CalculadoraActivity::class.java).apply {
                    putExtra("PRODUTO_NOME", produtoNome)
                    putExtra("TIPO_OPERACAO", tipoOperacao)
                    putExtra("PRODUCAO_INDEX", producaoIndex)
                }
                calculadoraLauncher.launch(intent)
            }
        )

        recycler.adapter = currentAdapter
    }

    private fun enviarEmail() {
        val file = File(getExternalFilesDir(null), "mapa_producao.xlsx")
        if (file.exists()) {
            Thread {
                try {
                    val emailService = EmailService(
                        senderEmail = "relatorioloja012@gmail.com",
                        senderAppPassword = "cwvt qgcg etrd ydzw",
                        toAddresses = listOf("...@gmail.com")
                    )
                    emailService.sendExcel(file, "Mapa de Produção - ${mapaProducao.data}",
                        "Segue em anexo o mapa de produção atualizado.")

                    runOnUiThread {
                        Toast.makeText(this, "Email enviado com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, "Erro ao enviar email: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        } else {
            Toast.makeText(this, "Arquivo não encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        producaoAtualIndex = CacheManager.carregarProducaoIndex(this)
    }
}