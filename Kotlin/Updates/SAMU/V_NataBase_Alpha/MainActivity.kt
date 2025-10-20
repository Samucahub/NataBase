package com.belasoft.natabase_alpha

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.belasoft.natabase_alpha.utils.CacheManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager

class MainActivity : AppCompatActivity() {

    private lateinit var mapaProducao: MapaProducao
    private lateinit var recycler: RecyclerView
    private var producaoAtualIndex = 1
    private lateinit var currentAdapter: ItemProducaoAdapter
    private var categoriaAtual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val hoje = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val dataCache = CacheManager.carregarProducao(this)?.data ?: ""

        if (dataCache != hoje) {
            CacheManager.salvarProducaoIndex(this, 1)
        }

        producaoAtualIndex = CacheManager.carregarProducaoIndex(this)
        val mapaCache = CacheManager.carregarProducao(this)
        mapaProducao = (mapaCache ?: ExcelService.carregarMapaProducao(this))

        producaoAtualIndex = CacheManager.carregarProducaoIndex(this)

        findViewById<Button>(R.id.btnProducao).setOnClickListener {
            setContentView(R.layout.activity_producao)
            recycler = findViewById(R.id.recyclerProdutos)

            findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener {
                voltarParaHome()
            }

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

    private fun voltarParaHome() {
        setContentView(R.layout.activity_home)

        findViewById<Button>(R.id.btnProducao).setOnClickListener {
            setContentView(R.layout.activity_producao)
            recycler = findViewById(R.id.recyclerProdutos)

            findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener {
                voltarParaHome()
            }

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

    private fun voltarParaCategorias() {
        carregarCategorias()
        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener {
            voltarParaHome()
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
        val mapaComProducao = mapaProducao

        val sucesso = ExcelService.salvarProducao(this, mapaComProducao, producaoAtualIndex)

        producaoAtualIndex++

        mapaProducao = mapaComProducao

        CacheManager.salvarProducaoIndex(this, producaoAtualIndex)
        CacheManager.salvarProducao(this, mapaProducao)
        carregarCategorias()
        Toast.makeText(this, "Produção ${producaoAtualIndex-1} salva com sucesso!", Toast.LENGTH_SHORT).show()
    }

    private fun carregarCategorias() {
        findViewById<TextView>(R.id.statusText)?.text = "A Carregar..."

        val categorias = mapaProducao.itens
            .map { it.categoria }
            .filter { it.isNotBlank() }
            .distinct()

        val layoutManager = GridLayoutManager(this, calcularNumeroColunas())
        recycler.layoutManager = layoutManager
        recycler.adapter = CategoriaAdapter(categorias) { categoria ->
            abrirProdutosDaCategoria(categoria)
        }

        val dataAtual = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())

        findViewById<TextView>(R.id.statusText)?.text =
            "Produção $producaoAtualIndex - $dataAtual"
    }

    private fun calcularNumeroColunas(): Int {
        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density

        return if (screenWidthDp < 600) {
            1
        } else {
            2
        }
    }

    private fun abrirProdutosDaCategoria(categoria: String) {
        try {
            categoriaAtual = categoria

            val todosProdutosCategoria = mapaProducao.itens
                .filter { it.categoria == categoria }

            val produtosCategoria = todosProdutosCategoria.toMutableList()

            val layoutManager = GridLayoutManager(this, calcularNumeroColunasProdutos())
            recycler.layoutManager = layoutManager

            findViewById<ImageButton>(R.id.btnVoltar)?.setOnClickListener {
                voltarParaCategorias()
            }

            currentAdapter = ItemProducaoAdapter(
                produtosCategoria,
                producaoAtualIndex,
                onQuantidadeAlterada = { item ->
                    try {
                        val index = mapaProducao.itens.indexOfFirst { it.produto == item.produto && it.categoria == item.categoria }
                        if (index != -1) {
                            val novaLista = mapaProducao.itens.toMutableList()
                            novaLista[index] = item
                            mapaProducao = mapaProducao.copy(itens = novaLista)
                            CacheManager.salvarProducao(this, mapaProducao)
                        }
                        Toast.makeText(this, "${item.produto} atualizado", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                salvarCallback = { itensAtualizados ->
                    try {
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                onCalculadoraRequest = { produtoNome, tipoOperacao, producaoIndex ->
                    try {
                        val bottomSheet = CalculadoraBottomSheet.newInstance(produtoNome, tipoOperacao, producaoIndex)
                        bottomSheet.setListener(object : CalculadoraBottomSheet.CalculadoraListener {
                            override fun onValorConfirmado(produtoNome: String, tipoOperacao: String, valor: Int, producaoIndex: Int) {
                                currentAdapter.atualizarItem(produtoNome, tipoOperacao, valor, producaoIndex)
                            }
                        })
                        bottomSheet.show(supportFragmentManager, "calculadora")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity, "Erro ao abrir calculadora", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            recycler.adapter = currentAdapter
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao carregar produtos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calcularNumeroColunasProdutos(): Int {
        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density

        return when {
            screenWidthDp < 600 -> 1
            screenWidthDp < 900 -> 2
            else -> 3
        }
    }


    override fun onResume() {
        super.onResume()
        producaoAtualIndex = CacheManager.carregarProducaoIndex(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (::recycler.isInitialized) {
            val currentAdapter = recycler.adapter
            if (currentAdapter is CategoriaAdapter) {
                carregarCategorias()
            } else if (currentAdapter is ItemProducaoAdapter) {
                abrirProdutosDaCategoria(categoriaAtual)
            }
        }
    }
}
