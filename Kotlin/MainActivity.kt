package com.exemplo.natabase

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.exemplo.natabase.Produto
import com.exemplo.natabase.SalvarExcel
import com.exemplo.natabase.EmailService
import java.io.File
import java.util.*
import com.example.natabase.utils.SheetsHelper
import com.google.api.services.sheets.v4.Sheets

class MainActivity : AppCompatActivity() {

    private val dicionarioProdutos = mutableMapOf<String, Produto>()
    private lateinit var recycler: RecyclerView

    private lateinit var service: Sheets

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        service = SheetsHelper.getSheetsService(this)

        carregarProdutosDoSheets()

        findViewById<Button>(R.id.btnProducao).setOnClickListener {
            setContentView(R.layout.activity_producao)
            recycler = findViewById(R.id.recyclerProdutos)
            carregarProdutosMock()
        }

        findViewById<Button>(R.id.btnInventario).setOnClickListener {
            setContentView(R.layout.activity_inventario)
        }
    }

    private fun carregarProdutosDoSheets() {
        Thread {
            try {
                val spreadsheetId = "ID_DA_TUA_SHEET"   // aquele na URL
                val range = "Sheet1!A:C"

                val response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute()

                val values = response.getValues()
                if (values != null) {
                    for (row in values) {
                        val nome = row[0] as String
                        val tipo = row[1] as String
                        val qtd = (row[2] as String).toIntOrNull() ?: 0
                        println("Produto: $nome - Tipo: $tipo - Quantidade: $qtd")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun carregarProdutosMock() {
        dicionarioProdutos["Arroz"] = Produto("Arroz", "Grão", 10)
        dicionarioProdutos["Feijão"] = Produto("Feijão", "Grão", 5)
        dicionarioProdutos["Carne"] = Produto("Carne", "Proteína", 20)

        Toast.makeText(this, "Produtos carregados", Toast.LENGTH_SHORT).show()
    }

    private fun salvarExcel() {
        val file = SalvarExcel.salvarProducaoDiaria(
            dicionarioProdutos,
            Date(),
            "Loja012_2025.xlsx",
            getExternalFilesDir(null) ?: filesDir
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

