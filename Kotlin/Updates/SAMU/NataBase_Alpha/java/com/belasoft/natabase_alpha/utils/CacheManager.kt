package com.belasoft.natabase_alpha.utils

import android.content.Context
import com.belasoft.natabase_alpha.MapaProducao
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CacheManager {
    private const val PREFS_NAME = "ProducaoPrefs"
    private const val KEY_MAPA_PREFIX = "mapa_producao_"
    private const val KEY_DATA_PREFIX = "data_"
    private const val KEY_PRODUCAO_INDEX_PREFIX = "producao_index_"

    private fun getUserId(context: Context): String {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account?.id ?: "anonymous"
    }

    private fun getMapaKey(context: Context): String {
        return KEY_MAPA_PREFIX + getUserId(context)
    }

    private fun getDataKey(context: Context): String {
        return KEY_DATA_PREFIX + getUserId(context)
    }

    private fun getProducaoIndexKey(context: Context): String {
        return KEY_PRODUCAO_INDEX_PREFIX + getUserId(context)
    }

    fun salvarProducao(context: Context, mapa: MapaProducao) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val json = Gson().toJson(mapa)
        editor.putString(getMapaKey(context), json)

        val hoje = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        editor.putString(getDataKey(context), hoje)

        editor.apply()
    }

    fun carregarProducao(context: Context): MapaProducao? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val hoje = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val dataSalva = prefs.getString(getDataKey(context), "")

        return if (dataSalva == hoje) {
            val json = prefs.getString(getMapaKey(context), null)
            if (json != null) {
                try {
                    // Primeiro tenta desserializar normalmente (dados não criptografados)
                    val type = object : TypeToken<MapaProducao>() {}.type
                    Gson().fromJson(json, type)
                } catch (e: com.google.gson.JsonSyntaxException) {
                    // Se falhar, pode ser dados criptografados - tenta descriptografar
                    try {
                        val decryptedJson = SecureStorageManager.decryptData(json)
                        val type = object : TypeToken<MapaProducao>() {}.type
                        Gson().fromJson(decryptedJson, type)
                    } catch (e2: Exception) {
                        // Se ainda falhar, retorna null
                        null
                    }
                }
            } else {
                null
            }
        } else {
            prefs.edit()
                .remove(getMapaKey(context))
                .remove(getProducaoIndexKey(context))
                .apply()
            null
        }
    }

    fun salvarProducaoSegura(context: Context, mapa: MapaProducao) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val json = Gson().toJson(mapa)

        // Criptografar antes de salvar
        val encryptedJson = SecureStorageManager.encryptData(json)
        editor.putString(getMapaKey(context), encryptedJson)

        val hoje = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        editor.putString(getDataKey(context), hoje)

        editor.apply()
    }

    fun carregarProducaoSegura(context: Context): MapaProducao? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val hoje = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val dataSalva = prefs.getString(getDataKey(context), "")

        return if (dataSalva == hoje) {
            val encryptedJson = prefs.getString(getMapaKey(context), null)
            if (encryptedJson != null) {
                try {
                    // Tentar descriptografar
                    val json = SecureStorageManager.decryptData(encryptedJson)
                    val type = object : TypeToken<MapaProducao>() {}.type
                    Gson().fromJson(json, type)
                } catch (e: Exception) {
                    // Fallback para dados não criptografados
                    try {
                        val json = encryptedJson
                        val type = object : TypeToken<MapaProducao>() {}.type
                        Gson().fromJson(json, type)
                    } catch (e2: Exception) {
                        null
                    }
                }
            } else {
                null
            }
        } else {
            prefs.edit()
                .remove(getMapaKey(context))
                .remove(getProducaoIndexKey(context))
                .apply()
            null
        }
    }

    fun migrarParaArmazenamentoSeguro(context: Context): Boolean {
        return try {
            val mapaAntigo = carregarProducao(context)
            if (mapaAntigo != null) {
                salvarProducaoSegura(context, mapaAntigo)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun salvarProducaoIndex(context: Context, index: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(getProducaoIndexKey(context), index).apply()
    }

    fun carregarProducaoIndex(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val hoje = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val dataSalva = prefs.getString(getDataKey(context), "")

        return if (dataSalva == hoje) {
            prefs.getInt(getProducaoIndexKey(context), 1)
        } else {
            prefs.edit().putInt(getProducaoIndexKey(context), 1).apply()
            1
        }
    }

    fun clearUserData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(getMapaKey(context))
            .remove(getDataKey(context))
            .remove(getProducaoIndexKey(context))
            .apply()
    }

    fun clearAllData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}