package com.belasoft.natabase_alpha.utils

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

class BiometricAuthManager(private val context: Context) {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private var cancellationSignal: android.os.CancellationSignal? = null

    companion object {
        private const val KEY_NAME = "NatabaseBiometricKey"
        private const val TAG = "BiometricAuthManager"
    }

    init {
        initializeBiometric()
    }

    private fun initializeBiometric() {
        executor = ContextCompat.getMainExecutor(context)

        // Usando a biblioteca androidx.biometric para melhor compatibilidade
        biometricPrompt = BiometricPrompt(context as FragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.d(TAG, "Authentication error: $errorCode - $errString")
                    onAuthenticationResult?.invoke(false, errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Authentication succeeded")
                    onAuthenticationResult?.invoke(true, null)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.d(TAG, "Authentication failed")
                    onAuthenticationResult?.invoke(false, "Autenticação falhou")
                }
            })
    }

    private var onAuthenticationResult: ((Boolean, String?) -> Unit)? = null

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.w(TAG, "Biometric available but not enrolled")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.w(TAG, "Biometric hardware unavailable")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.w(TAG, "No biometric hardware available")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Log.w(TAG, "Biometric security update required")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Log.w(TAG, "Biometric unsupported")
                false
            }
            else -> false
        }
    }

    fun isBiometricEnrolled(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun enableBiometricAuth(): Boolean {
        if (isBiometricAvailable() && isBiometricEnrolled()) {
            // Salvar preferência do usuário
            val prefs = context.getSharedPreferences("SecurityPrefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("biometric_enabled", true).apply()
            return true
        }
        return false
    }

    // CORREÇÃO: Agora a desativação também requer autenticação biométrica
    fun disableBiometricAuth(callback: ((Boolean) -> Unit)? = null) {
        if (!isBiometricEnabled()) {
            // Se já não está habilitado, simplesmente atualiza as preferências
            val prefs = context.getSharedPreferences("SecurityPrefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("biometric_enabled", false).apply()
            callback?.invoke(true)
            return
        }

        // Se está habilitado, requer autenticação para desativar
        requireBiometricForSensitiveOperations(
            title = "Desativar Autenticação Biométrica",
            subtitle = "Autentique-se para desativar a segurança biométrica"
        ) { authenticated ->
            if (authenticated) {
                val prefs = context.getSharedPreferences("SecurityPrefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("biometric_enabled", false).apply()
                Log.d(TAG, "Biometric authentication disabled after successful authentication")
                callback?.invoke(true)
            } else {
                Log.d(TAG, "Biometric authentication failed - biometric remains enabled")
                callback?.invoke(false)
            }
        }
    }

    fun isBiometricEnabled(): Boolean {
        val prefs = context.getSharedPreferences("SecurityPrefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("biometric_enabled", false) && isBiometricAvailable() && isBiometricEnrolled()
    }

    fun requireBiometricForSensitiveOperations(
        title: String = "Autenticação Biométrica Necessária",
        subtitle: String = "Confirme sua identidade para continuar",
        callback: (Boolean) -> Unit
    ) {
        if (!isBiometricAvailable() || !isBiometricEnrolled()) {
            callback(true) // Se biométrica não está disponível, permite a ação
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancelar")
            .setConfirmationRequired(true)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        onAuthenticationResult = { success, errorMessage ->
            callback(success)
        }

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Biometric authentication failed", e)
            showFallbackDialog(callback)
        }
    }

    fun requireBiometricWithPasswordFallback(
        title: String = "Autenticação Necessária",
        subtitle: String = "Confirme sua identidade para continuar",
        callback: (Boolean) -> Unit
    ) {
        if (!isBiometricAvailable() || !isBiometricEnrolled()) {
            callback(true)
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Usar Senha do Dispositivo")
            .setConfirmationRequired(true)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        onAuthenticationResult = { success, errorMessage ->
            if (success) {
                callback(true)
            } else {
                // Se o usuário clicou em "Usar Senha do Dispositivo", considere como sucesso
                // ou mostre um diálogo alternativo
                if (errorMessage?.contains("canceled") == true) {
                    showPasswordFallbackDialog(callback)
                } else {
                    callback(false)
                }
            }
        }

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Biometric authentication failed", e)
            showPasswordFallbackDialog(callback)
        }
    }

    private fun showPasswordFallbackDialog(callback: (Boolean) -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Autenticação Alternativa")
            .setMessage("Deseja usar a senha do dispositivo para autenticar?")
            .setPositiveButton("Sim") { _, _ ->
                callback(true)
            }
            .setNegativeButton("Cancelar") { _, _ ->
                callback(false)
            }
            .show()
    }

    private fun showFallbackDialog(callback: (Boolean) -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Autenticação Necessária")
            .setMessage("A autenticação biométrica falhou. Deseja desativar a segurança biométrica para esta ação?")
            .setPositiveButton("Desativar e Continuar") { _, _ ->
                // CORREÇÃO: Agora desativar também requer callback consistente
                val prefs = context.getSharedPreferences("SecurityPrefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("biometric_enabled", false).apply()
                callback(true)
            }
            .setNegativeButton("Cancelar") { _, _ ->
                callback(false)
            }
            .show()
    }

    fun cancelAuthentication() {
        cancellationSignal?.cancel()
        cancellationSignal = null
    }

    // Método para verificar se uma ação específica requer autenticação biométrica
    fun requiresBiometricForAction(action: SensitiveAction): Boolean {
        return when (action) {
            SensitiveAction.CLEAR_CACHE -> true
            SensitiveAction.REGENERATE_EXCEL -> true
            SensitiveAction.SEND_EMAIL -> true
            SensitiveAction.CHANGE_ACCOUNT -> true
            SensitiveAction.EXPORT_DATA -> true
            SensitiveAction.DELETE_DATA -> true
            SensitiveAction.DISABLE_BIOMETRIC -> true // CORREÇÃO: Desativar biometria também é sensível
            else -> false
        }
    }

    // Método para obter informações sobre o status da biometria
    fun getBiometricStatus(): BiometricStatus {
        return when {
            !isBiometricAvailable() -> BiometricStatus.NOT_AVAILABLE
            !isBiometricEnrolled() -> BiometricStatus.NOT_ENROLLED
            isBiometricEnabled() -> BiometricStatus.ENABLED
            else -> BiometricStatus.DISABLED
        }
    }
}

enum class SensitiveAction {
    CLEAR_CACHE,
    REGENERATE_EXCEL,
    SEND_EMAIL,
    CHANGE_ACCOUNT,
    EXPORT_DATA,
    DELETE_DATA,
    DISABLE_BIOMETRIC // CORREÇÃO: Nova ação para desativar biometria
}

enum class BiometricStatus {
    NOT_AVAILABLE,    // Hardware não disponível
    NOT_ENROLLED,     // Hardware disponível mas nenhuma biometria cadastrada
    ENABLED,          // Biometria disponível e habilitada
    DISABLED          // Biometria disponível mas desabilitada pelo usuário
}