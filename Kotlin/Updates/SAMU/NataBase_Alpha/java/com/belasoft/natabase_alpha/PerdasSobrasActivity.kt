package com.belasoft.natabase_alpha

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.belasoft.natabase_alpha.utils.CacheManager
import com.belasoft.natabase_alpha.utils.SettingsManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.net.Socket

class PerdasSobrasActivity : AppCompatActivity(), CalculadoraFragment.CalculadoraListener {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: PerdasSobrasAdapter
    private lateinit var mapaProducao: MapaProducao
    private var producaoAtualIndex: Int = 1
    private var isTabletMode: Boolean = false
    private var calculadoraAberta: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isTabletMode = isTabletLandscape()
        if (isTabletMode) {
            setContentView(R.layout.activity_perdas_sobras_tablet)
        } else {
            setContentView(R.layout.activity_perdas_sobras)
        }

        mapaProducao = intent.getSerializableExtra("MAPA_PRODUCAO") as MapaProducao
        producaoAtualIndex = intent.getIntExtra("PRODUCAO_INDEX", 1)

        initViews()
        setupRecyclerView()
    }

    private fun initViews() {
        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)
        btnVoltar?.setOnClickListener {
            finish()
        }

        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        btnConfirmar?.setOnClickListener {
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
                try {
                    if (isTabletMode) {
                        abrirCalculadoraTablet(produtoNome, tipoOperacao, 0)
                    } else {
                        val bottomSheet = CalculadoraBottomSheet.newInstance(produtoNome, tipoOperacao, 0)
                        bottomSheet.setListener(object : CalculadoraBottomSheet.CalculadoraListener {
                            override fun onValorConfirmado(produtoNome: String, tipoOperacao: String, valor: Int, producaoIndex: Int) {
                                adapter.atualizarItem(produtoNome, tipoOperacao, valor)
                            }
                        })
                        bottomSheet.show(supportFragmentManager, "calculadora")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@PerdasSobrasActivity, "Erro ao abrir calculadora", Toast.LENGTH_SHORT).show()
                }
            }
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun abrirCalculadoraTablet(produtoNome: String, tipoOperacao: String, producaoIndex: Int) {
        val calculadoraContainer = findViewById<FrameLayout>(R.id.calculadoraContainer)
        val mainContainer = findViewById<LinearLayout>(R.id.mainContainer)

        if (!calculadoraAberta) {
            val calculadoraFragment = CalculadoraFragment.newInstance(produtoNome, tipoOperacao, producaoIndex)
            calculadoraFragment.setListener(this)

            supportFragmentManager.beginTransaction()
                .replace(R.id.calculadoraContainer, calculadoraFragment)
                .commit()

            calculadoraContainer.visibility = View.VISIBLE
            calculadoraContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left))

            val layoutParams = calculadoraContainer.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = 0.4f
            calculadoraContainer.layoutParams = layoutParams

            val mainLayoutParams = mainContainer.layoutParams as LinearLayout.LayoutParams
            mainLayoutParams.weight = 0.6f
            mainContainer.layoutParams = mainLayoutParams

            calculadoraAberta = true
        } else {
            val existingFragment = supportFragmentManager.findFragmentById(R.id.calculadoraContainer) as? CalculadoraFragment
            existingFragment?.let {
                it.updateProductData(produtoNome, tipoOperacao, producaoIndex)
            }
        }
    }

    private fun fecharCalculadoraTablet() {
        if (calculadoraAberta) {
            val calculadoraContainer = findViewById<FrameLayout>(R.id.calculadoraContainer)
            val mainContainer = findViewById<LinearLayout>(R.id.mainContainer)

            calculadoraContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left))
            calculadoraContainer.postDelayed({
                val fragment = supportFragmentManager.findFragmentById(R.id.calculadoraContainer)
                fragment?.let {
                    supportFragmentManager.beginTransaction()
                        .remove(it)
                        .commit()
                }

                val layoutParams = calculadoraContainer.layoutParams as LinearLayout.LayoutParams
                layoutParams.weight = 0f
                calculadoraContainer.layoutParams = layoutParams

                val mainLayoutParams = mainContainer.layoutParams as LinearLayout.LayoutParams
                mainLayoutParams.weight = 1f
                mainContainer.layoutParams = mainLayoutParams

                calculadoraContainer.visibility = View.GONE
                calculadoraAberta = false
            }, 300)
        }
    }

    override fun onValorConfirmado(produtoNome: String, tipoOperacao: String, valor: Int, producaoIndex: Int) {
        adapter.atualizarItem(produtoNome, tipoOperacao, valor)
        fecharCalculadoraTablet()
    }

    override fun onCalculadoraFechada() {
        fecharCalculadoraTablet()
    }

    private fun getExcelFile(context: Context): File {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val userId = account?.id ?: "anonymous"
        val fileName = "mapa_producao_${userId}.xlsx"

        // Primeiro verifica no diretório de exportação configurado
        val exportDirectory = SettingsManager.getExportDirectoryFile(context)
        val exportFile = File(exportDirectory, fileName)

        if (exportFile.exists()) {
            Log.d("PerdasSobras", "Arquivo encontrado em: ${exportFile.absolutePath}")
            return exportFile
        }

        // Se não encontrar, verifica no diretório interno da aplicação
        val internalFile = File(context.getExternalFilesDir(null), fileName)
        if (internalFile.exists()) {
            Log.d("PerdasSobras", "Arquivo encontrado em: ${internalFile.absolutePath}")
            return internalFile
        }

        // Se ainda não encontrar, verifica no diretório de arquivos internos
        val filesDirFile = File(context.filesDir, fileName)
        if (filesDirFile.exists()) {
            Log.d("PerdasSobras", "Arquivo encontrado em: ${filesDirFile.absolutePath}")
            return filesDirFile
        }

        Log.e("PerdasSobras", "Arquivo não encontrado em nenhum local:")
        Log.e("PerdasSobras", "1. ${exportFile.absolutePath}")
        Log.e("PerdasSobras", "2. ${internalFile.absolutePath}")
        Log.e("PerdasSobras", "3. ${filesDirFile.absolutePath}")

        return exportFile // Retorna o arquivo do diretório de exportação mesmo se não existir
    }

    private fun verificarECriarArquivo(): File {
        val file = getExcelFile(this)

        if (!file.exists()) {
            Log.w("PerdasSobras", "Arquivo não existe, criando novo...")

            // Tenta carregar o mapa de produção para forçar a criação do arquivo
            try {
                val novoMapa = ExcelService.carregarMapaProducao(this)
                mapaProducao = novoMapa
                CacheManager.salvarProducao(this, novoMapa)

                // Verifica novamente se o arquivo foi criado
                if (file.exists()) {
                    Log.d("PerdasSobras", "Arquivo criado com sucesso em: ${file.absolutePath}")
                    Toast.makeText(this, "Novo arquivo Excel criado", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("PerdasSobras", "Falha ao criar arquivo Excel")
                    Toast.makeText(this, "Erro ao criar arquivo Excel", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("PerdasSobras", "Erro ao criar arquivo: ${e.message}")
                e.printStackTrace()
            }
        } else {
            Log.d("PerdasSobras", "Arquivo encontrado: ${file.absolutePath}")
            Log.d("PerdasSobras", "Tamanho do arquivo: ${file.length()} bytes")
        }

        return file
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

        val file = verificarECriarArquivo()

        if (!file.exists() || file.length() == 0L) {
            Toast.makeText(this,
                "Arquivo Excel não encontrado ou vazio. Verifique se há produções registradas.",
                Toast.LENGTH_LONG).show()
            Log.e("PerdasSobras", "Arquivo não existe ou está vazio: ${file.absolutePath}")
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

            if (EmailService.shouldSendAutoEmail(this)) {
                enviarEmail(file)
            } else {
                Toast.makeText(this, "Perdas e sobras salvas (email automático desativado)", Toast.LENGTH_SHORT).show()
                finish()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar perdas e sobras: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun isTabletLandscape(): Boolean {
        val configuration = resources.configuration
        return (configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE &&
                configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
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
        Log.d("PerdasSobras", "Tentando enviar email com arquivo: ${file.absolutePath}")
        Log.d("PerdasSobras", "Arquivo existe: ${file.exists()}")
        Log.d("PerdasSobras", "Tamanho do arquivo: ${file.length()} bytes")

        if (!file.exists() || file.length() == 0L) {
            Toast.makeText(this,
                "Arquivo não encontrado. Verifique se há produções registradas.",
                Toast.LENGTH_LONG).show()
            return
        }

        val account = GoogleSignIn.getLastSignedInAccount(this)
        val emailDestinatario = account?.email ?: "example@gmail.com"

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
                        toAddresses = listOf(emailDestinatario)
                    )
                    emailService.sendExcel(
                        file,
                        "Mapa de Produção - $dataParaEmail",
                        "Segue o mapa de produção em anexo."
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@PerdasSobrasActivity,
                            "Email enviado com sucesso para $emailDestinatario!",
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }

    // Método de debug temporário - remova depois de resolver o problema
    private fun adicionarBotaoDebug() {
        val debugButton = Button(this).apply {
            text = "Debug Arquivo"
            setOnClickListener {
                val file = getExcelFile(this@PerdasSobrasActivity)
                val alertMessage = """
                    Caminho: ${file.absolutePath}
                    Existe: ${file.exists()}
                    Tamanho: ${file.length()} bytes
                    Pode ler: ${file.canRead()}
                    Pode escrever: ${file.canWrite()}
                    
                    Diretório existe: ${file.parentFile?.exists()}
                    Diretório pode escrever: ${file.parentFile?.canWrite()}
                    
                    Mapa tem produções: ${mapaProducao.itens.any { it.producoes.any { p -> p.quantidade > 0 }}}
                """.trimIndent()

                android.app.AlertDialog.Builder(this@PerdasSobrasActivity)
                    .setTitle("Informações do Arquivo - Perdas/Sobras")
                    .setMessage(alertMessage)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        // Adiciona o botão ao layout (remova depois dos testes)
        val layout = findViewById<LinearLayout>(R.id.mainContainer)
        layout?.addView(debugButton)
    }

    // Chame este método no onCreate para debug
    override fun onResume() {
        super.onResume()
        adicionarBotaoDebug() // Remova esta linha depois de resolver o problema
    }
}