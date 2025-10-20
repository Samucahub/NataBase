package com.belasoft.natabase_alpha

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.belasoft.natabase_alpha.utils.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

class ResumoActivity : AppCompatActivity() {

    private lateinit var tableContainer: LinearLayout
    private lateinit var btnEnviarEmail: Button
    private lateinit var mapaProducao: MapaProducao
    private var producaoAtualIndex: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumo)

        tableContainer = findViewById(R.id.tableContainer)
        btnEnviarEmail = findViewById(R.id.btnEnviarEmail)

        mapaProducao = CacheManager.carregarProducao(this)
            ?: ExcelService.carregarMapaProducao(this)

        producaoAtualIndex = CacheManager.carregarProducaoIndex(this)

        mostrarResumo()

        btnEnviarEmail.setOnClickListener {
            val file = File(getExternalFilesDir(null), "mapa_producao.xlsx")
            if (file.exists()) {
                enviarEmail(file)
            } else {
                Toast.makeText(this, "Arquivo não encontrado", Toast.LENGTH_SHORT).show()
            }
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
        if (!isNetworkAvailable(this)) {
            Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_LONG).show()
            return
        }

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
                        toAddresses = listOf("samu.plantaarvores@gmail.com")
                    )
                    emailService.sendExcel(
                        file,
                        "Mapa de Produção - $dataParaEmail",
                        "Segue o mapa de produção em anexo."
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ResumoActivity,
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

    private fun mostrarResumo() {
        tableContainer.removeAllViews()

        val itensPorCategoria = mapaProducao.itens.groupBy { it.categoria }

        for ((categoria, itens) in itensPorCategoria) {
            val tvCategoria = TextView(this).apply {
                text = "--- $categoria ---"
                textSize = 25f
                setTextColor(ContextCompat.getColor(this@ResumoActivity, R.color.white))
                setTypeface(null, Typeface.BOLD)
                setPadding(16, 16, 16, 8)
            }
            tableContainer.addView(tvCategoria)

            for (item in itens) {
                val totalProducoes = item.producoes.sumOf { it.quantidade }
                if (totalProducoes > 0) {
                    val tv = TextView(this).apply {
                        textSize = 20f
                        setTextColor(ContextCompat.getColor(this@ResumoActivity, R.color.white))
                        setTypeface(null, Typeface.BOLD)
                        setPadding(32, 8, 16, 8)
                    }
                    tableContainer.addView(tv)
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
