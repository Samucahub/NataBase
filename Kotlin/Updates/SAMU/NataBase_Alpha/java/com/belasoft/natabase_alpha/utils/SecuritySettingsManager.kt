package com.belasoft.natabase_alpha.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

object SecuritySettingsManager {
    private const val PREFS_NAME = "SecuritySettings"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_AUTO_LOCK = "auto_lock_enabled"
    private const val KEY_LOCK_TIMEOUT = "lock_timeout"
    private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
    private const val KEY_LOCK_UNTIL = "lock_until"
    private const val KEY_LAST_ACTIVITY = "last_activity"

    // Tempos de bloqueio progressivo (em minutos)
    private val LOCK_TIMES = longArrayOf(1, 5, 15, 30, 60) // 1 min, 5 min, 15 min, 30 min, 1 hora

    fun isBiometricEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isAutoLockEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_LOCK, true)
    }

    fun setAutoLockEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_LOCK, enabled).apply()
    }

    fun getLockTimeout(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LOCK_TIMEOUT, TimeUnit.MINUTES.toMillis(5)) // 5 minutos padrão
    }

    fun setLockTimeout(context: Context, timeoutMs: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LOCK_TIMEOUT, timeoutMs).apply()
    }

    fun incrementFailedAttempts(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
        val newAttempts = current + 1
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, newAttempts).apply()

        // Bloqueio progressivo baseado no número de tentativas
        if (newAttempts >= 3) {
            val lockIndex = minOf(newAttempts - 3, LOCK_TIMES.size - 1)
            val lockMinutes = LOCK_TIMES[lockIndex]
            val lockUntil = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(lockMinutes)
            prefs.edit().putLong(KEY_LOCK_UNTIL, lockUntil).apply()
        }
    }

    fun resetFailedAttempts(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .remove(KEY_LOCK_UNTIL)
            .apply()
    }

    fun isAccountLocked(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lockUntil = prefs.getLong(KEY_LOCK_UNTIL, 0)
        return System.currentTimeMillis() < lockUntil
    }

    fun getRemainingLockTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lockUntil = prefs.getLong(KEY_LOCK_UNTIL, 0)
        val remaining = lockUntil - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0
    }

    fun updateLastActivity(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis()).apply()
    }

    fun shouldAutoLock(context: Context): Boolean {
        if (!isAutoLockEnabled(context)) return false

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastActivity = prefs.getLong(KEY_LAST_ACTIVITY, 0)
        val timeout = getLockTimeout(context)

        return System.currentTimeMillis() - lastActivity > timeout
    }

    fun getFailedAttemptsCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
    }
}