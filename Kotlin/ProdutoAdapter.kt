package com.exemplo.natabase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class ProdutoAdapter(
    private val lista: MutableList<Produto>,
    private val onQuantidadeAlterada: (Produto) -> Unit,
    private val salvarCallback: (Map<String, Produto>) -> Unit
) : RecyclerView.Adapter<ProdutoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.produtoNome)
        val inputQuantidade: EditText = view.findViewById(R.id.inputQuantidade)
        val btnAdd: Button = view.findViewById(R.id.btnAdicionar)
        val btnAdd10: Button = view.findViewById(R.id.btnAdicionar10)
        val btnRemove: Button = view.findViewById(R.id.btnRemover)
        val btnRemove10: Button = view.findViewById(R.id.btnRemover10)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val produto = lista[position]

        holder.nome.text = produto.nome
        holder.inputQuantidade.setText(produto.quantidade.toString())

        holder.inputQuantidade.setOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus) {
                val novaQuantidade = holder.inputQuantidade.text.toString().toIntOrNull() ?: 0
                produto.quantidade = novaQuantidade
                onQuantidadeAlterada(produto)
                salvarCallback(lista.associateBy { it.nome })
            }
        }

        holder.inputQuantidade.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val novaQuantidade = holder.inputQuantidade.text.toString().toIntOrNull() ?: 0
                produto.quantidade = novaQuantidade
                onQuantidadeAlterada(produto)
                salvarCallback(lista.associateBy { it.nome })
                true
            } else {
                false
            }
        }

        holder.btnAdd.setOnClickListener {
            alterarQuantidade(produto, 1, holder)
        }

        holder.btnAdd10.setOnClickListener {
            alterarQuantidade(produto, 10, holder)
        }

        holder.btnRemove.setOnClickListener {
            alterarQuantidade(produto, -1, holder)
        }

        holder.btnRemove10.setOnClickListener {
            alterarQuantidade(produto, -10, holder)
        }
    }

    private fun alterarQuantidade(produto: Produto, valor: Int, holder: ViewHolder) {
        val novaQuantidade = max(0, produto.quantidade + valor)
        produto.quantidade = novaQuantidade
        holder.inputQuantidade.setText(novaQuantidade.toString())
        onQuantidadeAlterada(produto)
        salvarCallback(lista.associateBy { it.nome })
    }

    override fun getItemCount() = lista.size
}
