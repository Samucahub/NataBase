package com.exemplo.natabase

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.exemplo.natabase.utils.CacheManager
import java.io.File
import java.util.*
import android.os.Environment

class ResumoActivity : AppCompatActivity() {

    private lateinit var tableContainer: LinearLayout
    private lateinit var btnEnviarEmail: Button
    private var produtos: MutableMap<String, Produto> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumo)

        tableContainer = findViewById(R.id.tableContainer)
        btnEnviarEmail = findViewById(R.id.btnEnviarEmail)

        produtos = CacheManager.carregarProdutos(this)

        mostrarResumo()

        btnEnviarEmail.setOnClickListener {
            val file = SalvarCsv.salvarProducaoDiaria(
                produtos,
                Date(),
                "Resumo_${Date().time}.csv",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            )
            enviarEmail(file)
        }
    }

    private fun mostrarResumo() {
        tableContainer.removeAllViews()
        for (produto in produtos.values) {
            val tv = TextView(this).apply {
                text = "${produto.nome} — ${produto.quantidade}"
                textSize = 18f
                setPadding(16, 8, 16, 8)
            }
            tableContainer.addView(tv)
        }
    }

    private fun enviarEmail(file: File) {
        Thread {
            try {
                val emailService = EmailService(
                    senderEmail = "relatorioloja012@gmail.com",
                    senderAppPassword = "cwvt qgcg etrd ydzw",
                    toAddresses = listOf("...@gmail.com")
                )
                emailService.sendExcel(file, "Resumo da Produção", "Segue o resumo em anexo.")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
