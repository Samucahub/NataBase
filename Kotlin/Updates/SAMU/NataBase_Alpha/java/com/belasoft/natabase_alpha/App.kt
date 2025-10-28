package com.belasoft.natabase_alpha

import android.app.Application
import android.util.Log

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Migrar dados antigos para formato seguro (em background seguro)
        Thread {
            try {
                // Verifica se a criptografia está disponível antes de migrar
                val encryptionAvailable = try {
                    com.belasoft.natabase_alpha.utils.SecureStorageManager.isEncryptionAvailable()
                } catch (e: Exception) {
                    false
                }

                if (encryptionAvailable) {
                    val migrado = com.belasoft.natabase_alpha.utils.CacheManager.migrarParaArmazenamentoSeguro(this)
                    if (migrado) {
                        Log.d("App", "Dados migrados para armazenamento seguro")
                    }
                }

                // Criar backup automático se necessário
                try {
                    val excelFile = ExcelService.getCurrentExcelFile(this)
                    if (excelFile.exists()) {
                        val backupCriado = com.belasoft.natabase_alpha.utils.SecureBackupManager.autoBackupIfNeeded(this, excelFile)
                        if (backupCriado) {
                            Log.d("App", "Backup automático criado")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("App", "Erro no backup automático", e)
                }
            } catch (e: Exception) {
                Log.e("App", "Erro na inicialização segura", e)
            }
        }.start()
    }
}