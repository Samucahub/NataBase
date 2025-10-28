package com.belasoft.natabase_alpha

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.belasoft.natabase_alpha.utils.BiometricAuthManager
import com.belasoft.natabase_alpha.utils.CacheManager
import com.belasoft.natabase_alpha.utils.SecureBackupManager
import com.belasoft.natabase_alpha.utils.SettingsManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.io.File

class ConfigActivity : AppCompatActivity() {

    private lateinit var btnVoltar: Button
    private lateinit var btnTrocarConta: Button
    private lateinit var btnAlterarDiretorio: Button
    private lateinit var btnLimparCache: Button
    private lateinit var switchEmailAuto: Switch
    private lateinit var textViewEmailConta: TextView
    private lateinit var textViewDiretorioAtual: TextView

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var switchBiometric: Switch
    private lateinit var btnBackupManual: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricAuthManager = BiometricAuthManager(this)
        }

        initGoogleSignIn()
        initViews()
        setupClickListeners()
        loadCurrentSettings()
    }

    private fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initViews() {
        btnVoltar = findViewById(R.id.btnVoltar)
        btnTrocarConta = findViewById(R.id.btnTrocarConta)
        btnAlterarDiretorio = findViewById(R.id.btnAlterarDiretorio)
        btnLimparCache = findViewById(R.id.btnLimparCache)
        switchEmailAuto = findViewById(R.id.switchEmailAuto)
        textViewEmailConta = findViewById(R.id.textViewEmailConta)
        textViewDiretorioAtual = findViewById(R.id.textViewDiretorioAtual)
        switchBiometric = findViewById(R.id.switchBiometric)
        btnBackupManual = findViewById(R.id.btnBackupManual)
    }

    private fun setupClickListeners() {
        btnVoltar.setOnClickListener {
            finish()
        }

        btnTrocarConta.setOnClickListener {
            showChangeAccountDialog()
        }

        btnAlterarDiretorio.setOnClickListener {
            showDirectorySelectionDialog()
        }

        btnLimparCache.setOnClickListener {
            showClearCacheConfirmation()
        }

        switchEmailAuto.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setAutoEmailEnabled(this, isChecked)
            Toast.makeText(this,
                if (isChecked) "Email automático ativado" else "Email automático desativado",
                Toast.LENGTH_SHORT).show()
        }

        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (biometricAuthManager.isBiometricAvailable()) {
                    biometricAuthManager.enableBiometricAuth()
                    Toast.makeText(this, "Autenticação biométrica ativada", Toast.LENGTH_SHORT).show()
                } else {
                    switchBiometric.isChecked = false
                    Toast.makeText(this, "Autenticação biométrica não disponível neste dispositivo", Toast.LENGTH_LONG).show()
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    biometricAuthManager.disableBiometricAuth { success ->
                        runOnUiThread {
                            if (!success) {
                                // Se a autenticação falhou, mantém o switch marcado
                                switchBiometric.isChecked = true
                                Toast.makeText(this, "Falha na autenticação. Biometria mantida ativa.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Autenticação biométrica desativada", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // Para versões antigas sem biometria
                    val prefs = getSharedPreferences("SecurityPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("biometric_enabled", false).apply()
                    Toast.makeText(this, "Autenticação biométrica desativada", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnBackupManual.setOnClickListener {
            showBackupConfirmationDialog()
        }
    }

    private fun showBackupConfirmationDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricAuthManager.requireBiometricForSensitiveOperations(
                title = "Criar Backup Seguro",
                subtitle = "Autentique-se para criar um backup criptografado dos seus dados"
            ) { authenticated ->
                if (authenticated) {
                    runOnUiThread {
                        criarBackupManual()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Autenticação falhou. Backup cancelado.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // Para dispositivos sem biometria, pedir confirmação
            AlertDialog.Builder(this)
                .setTitle("Criar Backup")
                .setMessage("Deseja criar um backup criptografado dos seus dados?")
                .setPositiveButton("Criar Backup") { _, _ ->
                    criarBackupManual()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun criarBackupManual() {
        try {
            val excelFile = ExcelService.getCurrentExcelFile(this)

            if (!excelFile.exists()) {
                Toast.makeText(this, "Arquivo de dados não encontrado", Toast.LENGTH_SHORT).show()
                return
            }

            // Mostrar diálogo de progresso
            val progressDialog = AlertDialog.Builder(this)
                .setTitle("Criando Backup")
                .setMessage("A criptografar e criar backup dos dados...")
                .setCancelable(false)
                .create()
            progressDialog.show()

            // Executar em background
            Thread {
                try {
                    val backupFile = SecureBackupManager.createEncryptedBackup(this, excelFile)

                    runOnUiThread {
                        progressDialog.dismiss()

                        if (backupFile != null) {
                            val backupSize = String.format("%.2f", backupFile.length() / 1024.0 / 1024.0)
                            AlertDialog.Builder(this)
                                .setTitle("Backup Criado com Sucesso")
                                .setMessage("Backup criptografado criado:\n\n" +
                                        "• Tamanho: $backupSize MB\n" +
                                        "• Local: ${backupFile.parent}\n" +
                                        "• Data: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
                                .setPositiveButton("OK", null)
                                .setNeutralButton("Ver Backups") { _, _ ->
                                    mostrarListaBackups()
                                }
                                .show()
                        } else {
                            Toast.makeText(this, "Erro ao criar backup", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao criar backup: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarListaBackups() {
        val backups = SecureBackupManager.getAvailableBackups(this)

        if (backups.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Backups")
                .setMessage("Nenhum backup encontrado")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val backupItems = backups.map { backup ->
            val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(backup.timestamp))
            val size = String.format("%.2f", backup.encryptedSize / 1024.0 / 1024.0)
            "$date - $size MB - ${backup.fileName}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Backups Disponíveis (${backups.size})")
            .setItems(backupItems) { dialog, which ->
                val backupSelecionado = backups[which]
                mostrarOpcoesBackup(backupSelecionado)
            }
            .setPositiveButton("Fechar", null)
            .show()
    }

    private fun mostrarOpcoesBackup(backup: SecureBackupManager.BackupMetadata) {
        val backupFile = File(File(filesDir, "secure_backups"), "backup_${backup.timestamp}.enc")

        AlertDialog.Builder(this)
            .setTitle("Opções do Backup")
            .setMessage("Backup de ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(backup.timestamp))}")
            .setPositiveButton("Restaurar") { _, _ ->
                restaurarBackup(backupFile)
            }
            .setNegativeButton("Eliminar") { _, _ ->
                eliminarBackup(backupFile)
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun restaurarBackup(backupFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricAuthManager.requireBiometricForSensitiveOperations(
                title = "Restaurar Backup",
                subtitle = "Autentique-se para restaurar dados do backup"
            ) { authenticated ->
                if (authenticated) {
                    runOnUiThread {
                        executarRestauracao(backupFile)
                    }
                }
            }
        } else {
            executarRestauracao(backupFile)
        }
    }

    private fun executarRestauracao(backupFile: File) {
        val progressDialog = AlertDialog.Builder(this)
            .setTitle("Restaurando Backup")
            .setMessage("A restaurar dados do backup...")
            .setCancelable(false)
            .create()
        progressDialog.show()

        Thread {
            try {
                val targetFile = ExcelService.getCurrentExcelFile(this)
                val sucesso = SecureBackupManager.restoreFromBackup(backupFile, this, targetFile)

                runOnUiThread {
                    progressDialog.dismiss()
                    if (sucesso) {
                        Toast.makeText(this, "Backup restaurado com sucesso!", Toast.LENGTH_SHORT).show()
                        // Recarregar dados
                        CacheManager.migrarParaArmazenamentoSeguro(this)
                    } else {
                        Toast.makeText(this, "Falha ao restaurar backup", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Erro na restauração: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun eliminarBackup(backupFile: File) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminação")
            .setMessage("Tem certeza que deseja eliminar este backup?")
            .setPositiveButton("Eliminar") { _, _ ->
                val sucesso = SecureBackupManager.deleteBackup(backupFile, this)
                if (sucesso) {
                    Toast.makeText(this, "Backup eliminado", Toast.LENGTH_SHORT).show()
                    mostrarListaBackups()
                } else {
                    Toast.makeText(this, "Erro ao eliminar backup", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun loadCurrentSettings() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val email = account.email ?: "Conta Google"
            textViewEmailConta.text = email
        } else {
            textViewEmailConta.text = "Não autenticado"
        }

        switchBiometric.isChecked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricAuthManager.isBiometricEnabled()
        } else {
            false
        }

        switchEmailAuto.isChecked = SettingsManager.isAutoEmailEnabled(this)

        updateDirectoryDisplay()
    }

    private fun updateDirectoryDisplay() {
        val currentDir = SettingsManager.getExportDirectory(this)
        val directoryFile = SettingsManager.getExportDirectoryFile(this)

        textViewDiretorioAtual.text = when (currentDir) {
            SettingsManager.KEY_DEFAULT_EXPORT_DIR -> "Diretório da aplicação\n(${directoryFile.absolutePath})"
            SettingsManager.KEY_DOCUMENTS_DIR -> "Documentos públicos\n(${directoryFile.absolutePath})"
            SettingsManager.KEY_DOWNLOADS_DIR -> "Downloads públicos\n(${directoryFile.absolutePath})"
            else -> "Diretório da aplicação\n(${directoryFile.absolutePath})"
        }

        if (!SettingsManager.isDirectoryAvailable(this)) {
            textViewDiretorioAtual.text = "${textViewDiretorioAtual.text}\nDiretório não disponível"
        }
    }

    private fun showChangeAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Trocar Conta Google")
            .setMessage("Deseja sair da conta atual e fazer login com outra conta?")
            .setPositiveButton("Sim") { _, _ ->
                signOutAndChangeAccount()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun signOutAndChangeAccount() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            Toast.makeText(this, "Sessão terminada", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showDirectorySelectionDialog() {
        val directories = arrayOf(
            "Diretório da aplicação (Recomendado)",
            "Documentos públicos",
            "Downloads públicos"
        )

        AlertDialog.Builder(this)
            .setTitle("Selecionar Diretório de Exportação")
            .setItems(directories) { _, which ->
                when (which) {
                    0 -> setExportDirectory(SettingsManager.KEY_DEFAULT_EXPORT_DIR)
                    1 -> setExportDirectory(SettingsManager.KEY_DOCUMENTS_DIR)
                    2 -> setExportDirectory(SettingsManager.KEY_DOWNLOADS_DIR)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setExportDirectory(directory: String) {
        SettingsManager.setExportDirectory(this, directory)

        val directoryFile = SettingsManager.getExportDirectoryFile(this)
        if (!directoryFile.exists()) {
            directoryFile.mkdirs()
        }

        updateDirectoryDisplay()

        val message = when (directory) {
            SettingsManager.KEY_DEFAULT_EXPORT_DIR -> "Diretório alterado para o diretório da aplicação"
            SettingsManager.KEY_DOCUMENTS_DIR -> {
                if (SettingsManager.isDirectoryAvailable(this)) {
                    "Diretório alterado para Documentos públicos"
                } else {
                    "Diretório de Documentos pode não estar disponível. A usar diretório alternativo."
                }
            }
            SettingsManager.KEY_DOWNLOADS_DIR -> {
                if (SettingsManager.isDirectoryAvailable(this)) {
                    "Diretório alterado para Downloads públicos"
                } else {
                    "Diretório de Downloads pode não estar disponível. A usar diretório alternativo."
                }
            }
            else -> "Diretório alterado"
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showClearCacheConfirmation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            biometricAuthManager.isBiometricEnabled()) {

            biometricAuthManager.requireBiometricForSensitiveOperations(
                title = "Confirmar Limpeza de Cache",
                subtitle = "Autentique-se para limpar os dados temporários"
            ) { authenticated ->
                if (authenticated) {
                    runOnUiThread {
                        AlertDialog.Builder(this)
                            .setTitle("Limpar Cache")
                            .setMessage("Tem a certeza que deseja limpar os dados temporários da SUA conta? Esta ação não pode ser desfeita.")
                            .setPositiveButton("Limpar") { _, _ ->
                                clearUserCache()
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Autenticação falhou. Ação cancelada.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            AlertDialog.Builder(this)
                .setTitle("Limpar Cache")
                .setMessage("Tem a certeza que deseja limpar os dados temporários da SUA conta? Esta ação não pode ser desfeita.")
                .setPositiveButton("Limpar") { _, _ ->
                    clearUserCache()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun clearUserCache() {
        CacheManager.clearUserData(this)

        Toast.makeText(this, "Cache da sua conta limpo com sucesso", Toast.LENGTH_SHORT).show()
        loadCurrentSettings()
    }

    override fun onResume() {
        super.onResume()
        loadCurrentSettings()
    }
}