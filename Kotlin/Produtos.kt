package com.exemplo.natabase

class Produto(
    private val nome: String,
    private val tipo: String,
    quantidade: Int
) {
    private var quantidade: Int = if (quantidade >= 0) quantidade else 0

    fun getNome(): String = nome
    fun getTipo(): String = tipo
    fun getQuantidade(): Int = quantidade

    fun aumentarQuantidade(valor: Int) {
        val v = if (valor > 0) valor else 0
        quantidade += v
    }

    fun diminuirQuantidade(valor: Int) {
        val v = if (valor > 0) valor else 0
        quantidade = (quantidade - v).coerceAtLeast(0)
    }

    fun exibirDetalhes(): String {
        return "Nome: $nome\nTipo: $tipo\nQuantidade: $quantidade\n"
    }

    fun zerar() {
        quantidade = 0
    }
}
