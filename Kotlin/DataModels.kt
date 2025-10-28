package com.belasoft.natabase_alpha

import java.io.Serializable

data class MapaProducao(
    val data: String,
    val diaSemana: String,
    val itens: List<ItemProducao>
) : Serializable

data class ItemProducao(
    val categoria: String,
    val produto: String,
    val producoes: List<ProducaoDia> = emptyList(),
    val perdas: Int = 0,
    val sobras: Int = 0
) : Serializable {
    val quantidade: Int
        get() = producoes.sumOf { it.quantidade }
}

data class ProducaoDia(
    val quantidade: Int = 0,
    val hora: String = ""
) : Serializable

data class Produto(
    val nome: String,
    val tipo: String,
    var quantidade: Int = 0
) : Serializable