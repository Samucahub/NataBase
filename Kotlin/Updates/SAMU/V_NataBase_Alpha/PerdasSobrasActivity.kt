package com.belasoft.natabase_alpha

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.belasoft.natabase_alpha.utils.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.net.Socket

class PerdasSobrasActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: PerdasSobrasAdapter
    private lateinit var mapaProducao: MapaProducao
    private var producaoAtualIndex: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perdas_sobras)

        mapaProducao = intent.getSerializableExtra("MAPA_PRODUCAO") as MapaProducao
        producaoAtualIndex = intent.getIntExtra("PRODUCAO_INDEX", 1)

        initViews()
        setupRecyclerView()
    }

    private fun initViews() {
        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnConfirmar).setOnClickListener {
            confirmarESalvar()
        }
    }

    private fun setupRecyclerView() {
        recycler = findViewById(R.id.recyclerProdutos)

        val produtosComProducao = mapaProducao.itens.filter { item ->
            item.producoes.any { producao -> producao.quantidade > 0 }
        }.toMutableList()

        adapter = PerdasSobrasAdapter(
            produtosComProducao,
            onCalculadoraRequest = { produtoNome, tipoOperacao ->
                val intent = Intent(this, CalculadoraBottomSheet::class.java).apply {
                    putExtra("PRODUTO_NOME", produtoNome)
                    putExtra("TIPO_OPERACAO", tipoOperacao)
                    putExtra("PRODUCAO_INDEX", 0)
                }
                startActivityForResult(intent, CALCULADORA_REQUEST_CODE)
            }
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun confirmarESalvar() {
        val produtosAtualizados = adapter.getProdutosAtualizados()

        val itensInvalidos = produtosAtualizados.filter { item ->
            val producaoTotal = item.producoes.sumOf { it.quantidade }
            (item.perdas + item.sobras) > producaoTotal
        }

        if (itensInvalidos.isNotEmpty()) {
            val nomesInvalidos = itensInvalidos.joinToString { it.produto }
            Toast.makeText(this, "Erro: Os seguintes produtos têm perdas+sobras maiores que a produção: $nomesInvalidos", Toast.LENGTH_LONG).show()
            return
        }

        val file = File(getExternalFilesDir(null), "mapa_producao.xlsx")
        if (!file.exists()) {
            Toast.makeText(this, "Arquivo Excel não encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            FileInputStream(file).use { fis ->
                val workbook = XSSFWorkbook(fis)
                val sheet = workbook.getSheetAt(0)

                var rowIndex = 5
                while (rowIndex <= sheet.lastRowNum) {
                    val row = sheet.getRow(rowIndex)
                    if (row == null) {
                        rowIndex++
                        continue
                    }

                    val produtoNome = row.getCell(1)?.toString() ?: ""
                    if (produtoNome.isNotBlank()) {
                        val produtoAtualizado = produtosAtualizados.find { it.produto == produtoNome }
                        if (produtoAtualizado != null) {
                            val cellPerdas = row.getCell(13) ?: row.createCell(13)
                            cellPerdas.setCellValue(produtoAtualizado.perdas.toDouble())

                            val cellSobras = row.getCell(14) ?: row.createCell(14)
                            cellSobras.setCellValue(produtoAtualizado.sobras.toDouble())
                        }
                    }
                    rowIndex++
                }

                FileOutputStream(file).use { fos ->
                    workbook.write(fos)
                }
                workbook.close()
            }

            produtosAtualizados.forEach { produtoAtualizado ->
                val index = mapaProducao.itens.indexOfFirst {
                    it.produto == produtoAtualizado.produto && it.categoria == produtoAtualizado.categoria
                }
                if (index != -1) {
                    val novaLista = mapaProducao.itens.toMutableList()
                    val itemExistente = novaLista[index]
                    novaLista[index] = itemExistente.copy(
                        perdas = produtoAtualizado.perdas,
                        sobras = produtoAtualizado.sobras
                    )
                    mapaProducao = mapaProducao.copy(itens = novaLista)
                }
            }
            CacheManager.salvarProducao(this, mapaProducao)

            Toast.makeText(this, "Perdas e sobras salvas com sucesso!", Toast.LENGTH_SHORT).show()

            enviarEmail(file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar perdas e sobras: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private suspend fun testarConectividade(): Boolean = withContext(Dispatchers.IO) {
        try {
            val timeout = 10000
            Socket().use { socket ->
                val socketAddress = InetSocketAddress("smtp.gmail.com", 587)
                socket.connect(socketAddress, timeout)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun enviarEmail(file: File) {
        val dataParaEmail = if (mapaProducao.data.isNotBlank()) {
            mapaProducao.data
        } else {
            val hoje = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
            hoje
        }

        lifecycleScope.launch {
            val podeConectar = testarConectividade()
            if (!podeConectar) {
                Toast.makeText(
                    this@PerdasSobrasActivity,
                    "Não foi possível conectar ao servidor de email",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            withContext(Dispatchers.IO) {
                try {
                    val emailService = EmailService(
                        senderEmail = "relatorioloja012@gmail.com",
                        senderAppPassword = "cwvt qgcg etrd ydzw",
                        toAddresses = listOf("samu.plantaarvores@gmail.com")
                    )
                    emailService.sendExcel(
                        file,
                        "Mapa de Produção - $dataParaEmail",
                        "Segue o mapa de produção em anexo."
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@PerdasSobrasActivity,
                            "Email enviado com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        when (e) {
                            is java.net.UnknownHostException ->
                                Toast.makeText(
                                    this@PerdasSobrasActivity,
                                    "Erro de DNS: Não foi possível encontrar o servidor de email",
                                    Toast.LENGTH_LONG
                                ).show()

                            is java.net.ConnectException ->
                                Toast.makeText(
                                    this@PerdasSobrasActivity,
                                    "Erro de conexão: Verifique sua internet",
                                    Toast.LENGTH_LONG
                                ).show()

                            else ->
                                Toast.makeText(
                                    this@PerdasSobrasActivity,
                                    "Erro ao enviar email: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CALCULADORA_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.let {
                val produtoNome = it.getStringExtra("PRODUTO_NOME") ?: ""
                val tipoOperacao = it.getStringExtra("TIPO_OPERACAO") ?: ""
                val valor = it.getIntExtra("VALOR", 0)

                adapter.atualizarItem(produtoNome, tipoOperacao, valor)
            }
        }
    }

    companion object {
        private const val CALCULADORA_REQUEST_CODE = 1001
    }
}
