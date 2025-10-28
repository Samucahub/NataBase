package com.belasoft.natabase_alpha

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.belasoft.natabase_alpha.utils.BiometricAuthManager
import com.belasoft.natabase_alpha.utils.CacheManager
import com.belasoft.natabase_alpha.utils.SettingsManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*

class ResumoActivity : AppCompatActivity() {

    private lateinit var tableContainer: LinearLayout
    private lateinit var btnEnviarEmail: Button
    private lateinit var btnVoltar: ImageButton
    private lateinit var mapaProducao: MapaProducao
    private var producaoAtualIndex: Int = 1
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var biometricAuthManager: BiometricAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumo)

        initViews()
        setupData()
        mostrarResumo()
        setupDrawer()
        setupButtons()
        setupEmailButton()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricAuthManager = BiometricAuthManager(this)
        }

        btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        atualizarHeaderUtilizador()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_producao -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("open_production", true)
                    startActivity(intent)
                    finish()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_resumo -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_inventario -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("open_inventory", true)
                    startActivity(intent)
                    finish()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_configuracoes -> {
                    val intent = Intent(this, ConfigActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    // CORREÇÃO: Implementar logout
                    signOut()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupEmailButton() {
        btnEnviarEmail.setOnClickListener {
            val file = getExcelFile(this)
            if (file.exists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                    biometricAuthManager.isBiometricEnabled()) {

                    biometricAuthManager.requireBiometricForSensitiveOperations(
                        title = "Confirmar Envio de Email",
                        subtitle = "Autentique-se para enviar o relatório por email"
                    ) { authenticated ->
                        if (authenticated) {
                            runOnUiThread {
                                enviarEmail(file)
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this, "Autenticação falhou. Envio cancelado.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    enviarEmail(file)
                }
            } else {
                Toast.makeText(this, "Arquivo não encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // CORREÇÃO: Adicionar método para atualizar header
    private fun atualizarHeaderUtilizador() {
        val headerView = navigationView.getHeaderView(0)
        val txtUserName = headerView.findViewById<TextView>(R.id.txtUserName)
        val txtUserEmail = headerView.findViewById<TextView>(R.id.txtUserEmail)

        val account = GoogleSignIn.getLastSignedInAccount(this)
        account?.let {
            val nomeUsuario = it.displayName ?: it.email?.split("@")?.get(0) ?: "Utilizador"
            txtUserName.text = nomeUsuario
            txtUserEmail.text = it.email ?: ""
        } ?: run {
            txtUserName.text = "Utilizador"
            txtUserEmail.text = "Não autenticado"
        }
    }

    private fun signOut() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInClient.signOut().addOnCompleteListener(this) {
            Toast.makeText(this, "Sessão terminada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnEnviarEmail).setOnClickListener {
        }
    }

    private fun initViews() {
        tableContainer = findViewById(R.id.tableContainer)
        btnEnviarEmail = findViewById(R.id.btnEnviarEmail)
        btnVoltar = findViewById(R.id.btnVoltar)
    }

    private fun setupData() {
        try {
            // Tenta carregar dados seguros primeiro, depois fallback para normal
            mapaProducao = CacheManager.carregarProducaoSegura(this)
                ?: CacheManager.carregarProducao(this)
                        ?: ExcelService.carregarMapaProducao(this)

            producaoAtualIndex = CacheManager.carregarProducaoIndex(this)
        } catch (e: Exception) {
            // Fallback completo em caso de erro
            mapaProducao = ExcelService.carregarMapaProducao(this)
            producaoAtualIndex = 1
        }
    }

    private fun getExcelFile(context: Context): File {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val userId = account?.id ?: "anonymous"
        val fileName = "mapa_producao_${userId}.xlsx"

        // Primeiro verifica no diretório de exportação configurado
        val exportDirectory = SettingsManager.getExportDirectoryFile(context)
        val exportFile = File(exportDirectory, fileName)

        if (exportFile.exists()) {
            Log.d("ResumoActivity", "Arquivo encontrado em: ${exportFile.absolutePath}")
            return exportFile
        }

        // Se não encontrar, verifica no diretório interno da aplicação
        val internalFile = File(context.getExternalFilesDir(null), fileName)
        if (internalFile.exists()) {
            Log.d("ResumoActivity", "Arquivo encontrado em: ${internalFile.absolutePath}")
            return internalFile
        }

        // Se ainda não encontrar, verifica no diretório de arquivos internos
        val filesDirFile = File(context.filesDir, fileName)
        if (filesDirFile.exists()) {
            Log.d("ResumoActivity", "Arquivo encontrado em: ${filesDirFile.absolutePath}")
            return filesDirFile
        }

        return exportFile // Retorna o arquivo do diretório de exportação mesmo se não existir
    }

    private fun mostrarResumo() {
        tableContainer.removeAllViews()

        val headerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(this@ResumoActivity, R.drawable.rounded_button_brown)
            setPadding(24, 16, 24, 16)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 20)
            layoutParams = params
        }

        val dataAtual = if (mapaProducao.data.isNotBlank()) {
            mapaProducao.data
        } else {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        }

        val diaSemana = if (mapaProducao.diaSemana.isNotBlank()) {
            mapaProducao.diaSemana
        } else {
            SimpleDateFormat("EEEE", Locale("pt", "PT")).format(Date()).capitalize(Locale.getDefault())
        }

        val tvTitulo = TextView(this).apply {
            text = "Resumo de Produção"
            textSize = 24f
            setTextColor(ContextCompat.getColor(this@ResumoActivity, R.color.white))
            setTypeface(null, Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }

        headerContainer.addView(tvTitulo)
        tableContainer.addView(headerContainer)

        val itensPorCategoria = mapaProducao.itens
            .filter { item ->
                item.producoes.isNotEmpty() || item.perdas > 0 || item.sobras > 0
            }
            .groupBy { it.categoria }

        for ((categoria, itens) in itensPorCategoria) {
            val categoriaContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = ContextCompat.getDrawable(this@ResumoActivity, R.drawable.rounded_button_brown)
                setPadding(0, 0, 0, 0)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 20, 0, 0)
                layoutParams = params
            }

            val header = TextView(this).apply {
                text = categoria
                textSize = 22f
                setTextColor(ContextCompat.getColor(this@ResumoActivity, R.color.caramel))
                setTypeface(null, Typeface.BOLD)
                gravity = android.view.Gravity.CENTER
                setPadding(24, 16, 24, 16)
            }

            val linha = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
                )
                setBackgroundColor(ContextCompat.getColor(this@ResumoActivity, R.color.caramel))
            }

            val itensContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 16, 24, 16)
                setBackgroundColor(ContextCompat.getColor(this@ResumoActivity, R.color.caramel))
            }

            categoriaContainer.addView(header)
            categoriaContainer.addView(linha)
            categoriaContainer.addView(itensContainer)

            itens.forEach { item ->
                val producoesComQuantidade = item.producoes.filter { it.quantidade > 0 }
                val total = item.producoes.sumOf { it.quantidade }

                if (total > 0 || item.perdas > 0 || item.sobras > 0) {
                    val produtoContainer = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(0, 8, 0, 16)
                    }

                    val produtoHeader = TextView(this).apply {
                        text = "${item.produto} - Total: $total"
                        textSize = 18f
                        setTextColor(ContextCompat.getColor(this@ResumoActivity, R.color.white))
                        setTypeface(null, Typeface.BOLD)
                    }
                    produtoContainer.addView(produtoHeader)

                    producoesComQuantidade.forEachIndexed { index, producao ->
                        if (producao.quantidade > 0) {
                            val numeroProducao = index + 1
                            val detalheProducao = TextView(this).apply {
                                text = "  Produção $numeroProducao: ${producao.quantidade} unidades (${producao.hora})"
                                textSize = 14f
                                setTextColor(ContextCompat.getColor(this@ResumoActivity, R.color.white))
                                setPadding(16, 2, 0, 2)
                            }
                            produtoContainer.addView(detalheProducao)
                        }
                    }

                    if (item.perdas > 0) {
                        val perdasText = TextView(this).apply {
                            text = "  Perdas: ${item.perdas} unidades"
                            textSize = 14f
                            setTextColor(ContextCompat.getColor(this@ResumoActivity, R.color.red))
                            setPadding(16, 2, 0, 2)
                        }
                        produtoContainer.addView(perdasText)
                    }

                    if (item.sobras > 0) {
                        val sobrasText = TextView(this).apply {
                            text = "  Sobras: ${item.sobras} unidades"
                            textSize = 14f
                            setTextColor(ContextCompat.getColor(this@ResumoActivity, R.color.green))
                            setPadding(16, 2, 0, 2)
                        }
                        produtoContainer.addView(sobrasText)
                    }

                    val saldo = total - (item.perdas + item.sobras)
                    val saldoText = TextView(this).apply {
                        text = "  Saldo Final: $saldo unidades"
                        textSize = 16f
                        setTextColor(ContextCompat.getColor(this@ResumoActivity,
                            if (saldo >= 0) R.color.white else R.color.red))
                        setTypeface(null, Typeface.BOLD)
                        setPadding(16, 8, 0, 0)
                    }
                    produtoContainer.addView(saldoText)

                    itensContainer.addView(produtoContainer)
                }
            }

            tableContainer.addView(categoriaContainer)

            val espaco = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    20
                )
            }
            tableContainer.addView(espaco)
        }

        if (itensPorCategoria.isEmpty()) {
            val tvVazio = TextView(this).apply {
                text = "Nenhuma produção registada"
                textSize = 18f
                setTextColor(ContextCompat.getColor(this@ResumoActivity, R.color.deep_brown))
                setTypeface(null, Typeface.BOLD)
                gravity = android.view.Gravity.CENTER
                setPadding(0, 32, 0, 32)
            }
            tableContainer.addView(tvVazio)
        }
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
        AlertDialog.Builder(this)
            .setTitle("Adicionar Perdas e Sobras")
            .setMessage("Deseja adicionar perdas e sobras antes de enviar o email?")
            .setPositiveButton("Sim") { _, _ ->
                val intent = Intent(this, PerdasSobrasActivity::class.java).apply {
                    putExtra("MAPA_PRODUCAO", mapaProducao)
                    putExtra("PRODUCAO_INDEX", producaoAtualIndex)
                }
                startActivity(intent)
            }
            .setNegativeButton("Não, Enviar Agora") { _, _ ->
                enviarEmailDiretamente(file)
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun enviarEmailDiretamente(file: File) {
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
                    this@ResumoActivity,
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
                            this@ResumoActivity,
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
                                    this@ResumoActivity,
                                    "Erro de DNS: Não foi possível encontrar o servidor de email",
                                    Toast.LENGTH_LONG
                                ).show()

                            is java.net.ConnectException ->
                                Toast.makeText(
                                    this@ResumoActivity,
                                    "Erro de conexão: Verifique sua internet",
                                    Toast.LENGTH_LONG
                                ).show()

                            else ->
                                Toast.makeText(
                                    this@ResumoActivity,
                                    "Erro ao enviar email: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                        }
                    }
                }
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = cm.activeNetwork ?: return false
        val actNw = cm.getNetworkCapabilities(nw) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}