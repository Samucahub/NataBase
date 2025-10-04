package com.exemplo.natabase.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.exemplo.natabase.Produto
import java.text.SimpleDateFormat
import java.util.*

object CacheManager {
    private const val PREFS_NAME = "ProducaoPrefs"
    private const val KEY_PRODUTOS = "produtos"
    private const val KEY_DATA = "data"

    fun salvarProdutos(context: Context, produtos: Map<String, Produto>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val json = Gson().toJson(produtos)
        editor.putString(KEY_PRODUTOS, json)

        val hoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        editor.putString(KEY_DATA, hoje)

        editor.apply()
    }

    fun carregarProdutos(context: Context): MutableMap<String, Produto> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val hoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dataSalva = prefs.getString(KEY_DATA, "")

        return if (dataSalva == hoje) {
            val json = prefs.getString(KEY_PRODUTOS, null)
            if (json != null) {
                val type = object : TypeToken<MutableMap<String, Produto>>() {}.type
                Gson().fromJson(json, type)
            } else mutableMapOf()
        } else {
            mutableMapOf()
        }
    }

    fun limpar(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
