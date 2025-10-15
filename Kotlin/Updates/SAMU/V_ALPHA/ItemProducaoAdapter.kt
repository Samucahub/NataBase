package com.belasoft.natabase_alpha


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemProducaoAdapter(
    private val lista: MutableList<ItemProducao>,
    private val producaoIndex: Int,
    private val onQuantidadeAlterada: (ItemProducao) -> Unit,
    private val salvarCallback: (List<ItemProducao>) -> Unit,
    private val onCalculadoraRequest: (String, String, Int) -> Unit
) : RecyclerView.Adapter<ItemProducaoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.produtoNome)
        val btnProducao: Button = view.findViewById(R.id.btnProducao)
        val btnSobras: Button = view.findViewById(R.id.btnSobras)
        val btnPerdas: Button = view.findViewById(R.id.btnPerdas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.nome.text = item.produto

        holder.btnProducao.setOnClickListener {
            onCalculadoraRequest(item.produto, "Produção", producaoIndex)
        }

        holder.btnSobras.setOnClickListener {
            onCalculadoraRequest(item.produto, "Sobras", producaoIndex)
        }

        holder.btnPerdas.setOnClickListener {
            onCalculadoraRequest(item.produto, "Perdas", producaoIndex)
        }
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    fun atualizarItem(produtoNome: String, tipoOperacao: String, valor: Int, producaoIndex: Int) {
        val itemIndex = lista.indexOfFirst { it.produto == produtoNome }
        if (itemIndex != -1) {
            val item = lista[itemIndex]
            val updatedItem = when (tipoOperacao) {
                "Produção" -> {
                    val updatedProducoes = item.producoes.toMutableList()
                    updatedProducoes[producaoIndex - 1] = updatedProducoes[producaoIndex - 1].copy(
                        quantidade = valor
                    )
                    item.copy(producoes = updatedProducoes)
                }
                "Sobras" -> item.copy(sobras = valor)
                "Perdas" -> item.copy(perdas = valor)
                else -> item
            }

            lista[itemIndex] = updatedItem
            onQuantidadeAlterada(updatedItem)
            salvarCallback(lista)
            notifyItemChanged(itemIndex)
        }
    }
}