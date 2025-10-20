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
        val tvQuantidadeAtual: TextView = view.findViewById(R.id.tvQuantidadeAtual)
        val btnMenos10: Button = view.findViewById(R.id.btnMenos10)
        val btnMenos1: Button = view.findViewById(R.id.btnMenos1)
        val btnMais1: Button = view.findViewById(R.id.btnMais1)
        val btnMais10: Button = view.findViewById(R.id.btnMais10)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.nome.text = item.produto

        val producaoQuantidade = item.producoes.getOrNull(producaoIndex - 1)?.quantidade ?: 0
        holder.tvQuantidadeAtual.text = producaoQuantidade.toString()


        holder.btnMenos10.setOnClickListener {
            ajustarQuantidade(item, -10, holder)
        }

        holder.btnMenos1.setOnClickListener {
            ajustarQuantidade(item, -1, holder)
        }

        holder.btnMais1.setOnClickListener {
            ajustarQuantidade(item, 1, holder)
        }

        holder.btnMais10.setOnClickListener {
            ajustarQuantidade(item, 10, holder)
        }

        holder.tvQuantidadeAtual.setOnClickListener {
            onCalculadoraRequest(item.produto, "Produção", producaoIndex)
        }
    }

    private fun ajustarQuantidade(item: ItemProducao, ajuste: Int, holder: ViewHolder) {
        val itemIndex = lista.indexOfFirst { it.produto == item.produto }
        if (itemIndex != -1) {
            val currentItem = lista[itemIndex]
            val currentQuantidade = currentItem.producoes.getOrNull(producaoIndex - 1)?.quantidade ?: 0
            val novaQuantidade = maxOf(0, currentQuantidade + ajuste)

            val updatedProducoes = currentItem.producoes.toMutableList()
            while (updatedProducoes.size <= producaoIndex - 1) {
                updatedProducoes.add(ProducaoDia())
            }
            updatedProducoes[producaoIndex - 1] = updatedProducoes[producaoIndex - 1].copy(
                quantidade = novaQuantidade
            )

            val updatedItem = currentItem.copy(producoes = updatedProducoes)
            lista[itemIndex] = updatedItem

            holder.tvQuantidadeAtual.text = novaQuantidade.toString()

            onQuantidadeAlterada(updatedItem)
            salvarCallback(lista)
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
                    while (updatedProducoes.size <= producaoIndex - 1) {
                        updatedProducoes.add(ProducaoDia())
                    }
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
