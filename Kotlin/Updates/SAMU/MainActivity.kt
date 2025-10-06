package com.exemplo.natabase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.natabase.utils.SheetsHelper
import com.exemplo.natabase.utils.AuthManager
import com.exemplo.natabase.utils.CacheManager
import com.exemplo.natabase.utils.NetworkUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.api.services.sheets.v4.Sheets
import java.io.File
import java.util.Date

class MainActivity : AppCompatActivity() {

    private val dicionarioProdutos = mutableMapOf<String, Produto>()
    private lateinit var recycler: RecyclerView
    private lateinit var service: Sheets
    private lateinit var layoutError: LinearLayout
    private lateinit var layoutContent: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var btnRetry: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AuthManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        service = SheetsHelper.getSheetsService(this)

        findViewById<Button>(R.id.btnProducao).setOnClickListener {
            setContentView(R.layout.activity_producao)
            setupProducaoLayout()
            carregarProdutosDoSheets()
        }

        findViewById<Button>(R.id.btnInventario).setOnClickListener {
            setContentView(R.layout.activity_inventario)
        }
    }

    private fun setupProducaoLayout() {
        recycler = findViewById(R.id.recyclerProdutos)
        layoutError = findViewById(R.id.layoutError)
        layoutContent = findViewById(R.id.layoutContent)
        statusText = findViewById(R.id.statusText)
        btnRetry = findViewById(R.id.btnRetry)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarProducao)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_resumo -> {
                    val intent = Intent(this, ResumoActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_logout -> {
                    AuthManager.logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                R.id.action_manage_users -> {
                    if (AuthManager.isAdmin()) {
                        val intent = Intent(this, UserManagementActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Acesso negado", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

        btnRetry.setOnClickListener {
            carregarProdutosDoSheets()
        }
    }

    private fun carregarProdutosDoSheets() {
        if (!NetworkUtils.isInternetAvailable(this)) {
            showErrorState()
            return
        }

        showLoadingState()

        Thread {
            try {
                val spreadsheetId = "1dQ1Ru2hSlB3LWrFq3_LFII63pBuEaJeglcHg38rt4Jo"
                val range = "Produtos!A2:C70"

                val response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute()

                val values = response.getValues()
                if (values != null) {
                    dicionarioProdutos.clear()
                    for (row in values) {
                        if (row.size >= 3) {
                            val nome = row[0] as String
                            val tipo = row[1] as String
                            val qtd = (row[2] as String).toIntOrNull() ?: 0
                            dicionarioProdutos[nome] = Produto(nome, tipo, qtd)
                        }
                    }

                    runOnUiThread {
                        showContentState()
                        statusText.text = "Dados carregados (${dicionarioProdutos.size} produtos)"
                        atualizarRecyclerCategorias()
                    }
                } else {
                    runOnUiThread {
                        showContentState()
                        statusText.text = "Nenhum dado encontrado"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    if (!NetworkUtils.isInternetAvailable(this)) {
                        showErrorState()
                    } else {
                        showContentState()
                        statusText.text = "Erro: ${e.message}"
                    }
                }
            }
        }.start()
    }

    private fun showLoadingState() {
        runOnUiThread {
            layoutError.visibility = LinearLayout.GONE
            layoutContent.visibility = LinearLayout.VISIBLE
            statusText.text = "A carregar..."
        }
    }

    private fun showErrorState() {
        runOnUiThread {
            layoutError.visibility = LinearLayout.VISIBLE
            layoutContent.visibility = LinearLayout.GONE
        }
    }

    private fun showContentState() {
        runOnUiThread {
            layoutError.visibility = LinearLayout.GONE
            layoutContent.visibility = LinearLayout.VISIBLE
        }
    }

    private fun atualizarRecyclerCategorias() {
        recycler.layoutManager = LinearLayoutManager(this)
        val categorias = dicionarioProdutos.values.groupBy { it.tipo }.keys.toList()
        recycler.adapter = CategoriaAdapter(categorias) { categoria ->
            abrirProdutosDaCategoria(categoria)
        }
    }

    private fun abrirProdutosDaCategoria(categoria: String) {
        val produtos = dicionarioProdutos.values.filter { it.tipo == categoria }.toMutableList()
        recycler.adapter = ProdutoAdapter(produtos,
            onQuantidadeAlterada = { produto ->
                Toast.makeText(this, "${produto.nome} atualizado: ${produto.quantidade}", Toast.LENGTH_SHORT).show()
            },
            salvarCallback = { mapa ->
                CacheManager.salvarProdutos(this, mapa)
            }
        )
    }

}
