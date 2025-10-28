package com.belasoft.natabase_alpha.utils

import android.content.Context
import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.CRC32
import com.google.gson.Gson

object SecureBackupManager {
    private const val BACKUP_DIR = "secure_backups"
    private const val METADATA_EXTENSION = ".meta"
    private const val MAX_BACKUP_FILES = 5

    data class BackupMetadata(
        val fileName: String,
        val originalSize: Long,
        val encryptedSize: Long,
        val checksum: String,
        val timestamp: Long,
        val version: Int = 1
    )

    fun createEncryptedBackup(context: Context, originalFile: File): File? {
        return try {
            // Criar diretório de backups se não existir
            val backupDir = File(context.filesDir, BACKUP_DIR)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Limitar número de backups
            cleanupOldBackups(backupDir)

            val timestamp = System.currentTimeMillis()
            val backupFile = File(backupDir, "backup_${timestamp}.enc")

            // Criptografar arquivo
            val encryptedSuccess = encryptFile(originalFile, backupFile)
            if (!encryptedSuccess) {
                return null
            }

            // Calcular checksum
            val checksum = calculateFileChecksum(backupFile)
            val originalChecksum = calculateFileChecksum(originalFile)

            // Salvar metadados
            val metadata = BackupMetadata(
                fileName = originalFile.name,
                originalSize = originalFile.length(),
                encryptedSize = backupFile.length(),
                checksum = checksum,
                timestamp = timestamp
            )

            saveBackupMetadata(backupFile, metadata, context)

            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun restoreFromBackup(backupFile: File, context: Context, targetFile: File): Boolean {
        return try {
            // Verificar integridade do backup
            if (!verifyBackupIntegrity(backupFile, context)) {
                return false
            }

            // Descriptografar e restaurar
            decryptFile(backupFile, targetFile)

            // Verificar se o arquivo restaurado é válido
            targetFile.exists() && targetFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getAvailableBackups(context: Context): List<BackupMetadata> {
        val backups = mutableListOf<BackupMetadata>()

        try {
            val backupDir = File(context.filesDir, BACKUP_DIR)
            if (!backupDir.exists()) return backups

            backupDir.listFiles { file ->
                file.name.endsWith(".enc")
            }?.forEach { backupFile ->
                val metadata = loadBackupMetadata(backupFile, context)
                metadata?.let { backups.add(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return backups.sortedByDescending { it.timestamp }
    }

    fun deleteBackup(backupFile: File, context: Context): Boolean {
        return try {
            // Deletar arquivo de backup e metadados
            val metadataFile = getMetadataFile(backupFile)
            backupFile.delete()
            metadataFile.delete()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun encryptFile(sourceFile: File, targetFile: File): Boolean {
        return try {
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(targetFile).use { output ->
                    // Criptografar dados (usando SecureStorageManager como base)
                    val data = input.readBytes()
                    val encryptedData = SecureStorageManager.encryptData(String(data, Charsets.UTF_8))
                    output.write(encryptedData.toByteArray(Charsets.UTF_8))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun decryptFile(sourceFile: File, targetFile: File): Boolean {
        return try {
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(targetFile).use { output ->
                    val encryptedData = String(input.readBytes(), Charsets.UTF_8)
                    val decryptedData = SecureStorageManager.decryptData(encryptedData)
                    output.write(decryptedData.toByteArray(Charsets.UTF_8))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun calculateFileChecksum(file: File): String {
        return try {
            FileInputStream(file).use { input ->
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(8192)
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }

                Base64.encodeToString(digest.digest(), Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun saveBackupMetadata(backupFile: File, metadata: BackupMetadata, context: Context) {
        try {
            val metadataFile = getMetadataFile(backupFile)
            val json = Gson().toJson(metadata)
            metadataFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadBackupMetadata(backupFile: File, context: Context): BackupMetadata? {
        return try {
            val metadataFile = getMetadataFile(backupFile)
            if (!metadataFile.exists()) return null

            val json = metadataFile.readText()
            Gson().fromJson(json, BackupMetadata::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun verifyBackupIntegrity(backupFile: File, context: Context): Boolean {
        return try {
            val metadata = loadBackupMetadata(backupFile, context) ?: return false

            // Verificar se o arquivo existe e tem tamanho correto
            if (!backupFile.exists() || backupFile.length() != metadata.encryptedSize) {
                return false
            }

            // Verificar checksum
            val currentChecksum = calculateFileChecksum(backupFile)
            currentChecksum == metadata.checksum
        } catch (e: Exception) {
            false
        }
    }

    private fun getMetadataFile(backupFile: File): File {
        return File(backupFile.parent, "${backupFile.name}$METADATA_EXTENSION")
    }

    private fun cleanupOldBackups(backupDir: File) {
        try {
            val backupFiles = backupDir.listFiles { file ->
                file.name.endsWith(".enc")
            } ?: return

            if (backupFiles.size > MAX_BACKUP_FILES) {
                val sorted = backupFiles.sortedBy { it.lastModified() }
                val toDelete = sorted.take(backupFiles.size - MAX_BACKUP_FILES)

                toDelete.forEach { file ->
                    file.delete()
                    getMetadataFile(file).delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun autoBackupIfNeeded(context: Context, originalFile: File): Boolean {
        // Criar backup automático apenas uma vez por dia
        val prefs = context.getSharedPreferences("BackupPrefs", Context.MODE_PRIVATE)
        val lastBackup = prefs.getLong("last_auto_backup", 0)
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L

        if (now - lastBackup > oneDay && originalFile.exists()) {
            val backupFile = createEncryptedBackup(context, originalFile)
            if (backupFile != null) {
                prefs.edit().putLong("last_auto_backup", now).apply()
                return true
            }
        }

        return false
    }
}