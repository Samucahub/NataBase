package com.exemplo.natabase

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.*
import com.example.natabase.utils.SheetsHelper
import com.google.api.services.sheets.v4.Sheets
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.exemplo.natabase.utils.CacheManager
import android.content.Intent
import com.google.android.material.appbar.MaterialToolbar
import android.os.Environment

class MainActivity : AppCompatActivity() {

    private val dicionarioProdutos = mutableMapOf<String, Produto>()
    private lateinit var recycler: RecyclerView

    private lateinit var service: Sheets

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        service = SheetsHelper.getSheetsService(this)

        findViewById<Button>(R.id.btnProducao).setOnClickListener {
            setContentView(R.layout.activity_producao)
            recycler = findViewById(R.id.recyclerProdutos)
            carregarProdutosDoSheets()

            val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbarProducao)
            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_resumo -> {
                        val intent = Intent(this, ResumoActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
        }

        findViewById<Button>(R.id.btnInventario).setOnClickListener {
            setContentView(R.layout.activity_inventario)
        }

    }

    private fun carregarProdutosDoSheets() {
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
                        val status = findViewById<TextView>(R.id.statusText)
                        status?.text = "Dados carregados (${dicionarioProdutos.size} produtos)"

                        if (::recycler.isInitialized) {
                            atualizarRecyclerCategorias()
                        }
                    }
                } else {
                    runOnUiThread {
                        findViewById<TextView>(R.id.statusText)?.text = "Nenhum dado encontrado"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    val status = findViewById<TextView>(R.id.statusText)
                    status?.text = "Erro: ${e.message}"
                }
            }
        }.start()
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


    private fun salvarExcel() {
        val file = SalvarCsv.salvarProducaoDiaria(
            dicionarioProdutos,
            Date(),
            "Loja012_2025.csv",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        )
        Toast.makeText(this, "Excel salvo em ${file.path}", Toast.LENGTH_LONG).show()
    }

    private fun enviarEmail(file: File) {
        Thread {
            try {
                val emailService = EmailService(
                    senderEmail = "relatorioloja012@gmail.com",
                    senderAppPassword = "SENHA_DE_APP",
                    toAddresses = listOf("destinatario@gmail.com")
                )
                emailService.sendExcel(file, "Produção Loja 012", "Segue em anexo a produção.")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
