package com.exemplo.natabase

import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object SalvarCsv {

    fun salvarProducaoDiaria(
        dicionarioProdutos: Map<String, Produto>,
        data: Date = Date(),
        nomeArquivo: String = "Producao_Loja012.csv",
        pastaDestino: File
    ): File {
        val dataStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(data)
        val arquivoCsv = File(pastaDestino, nomeArquivo)

        val agrupadoPorTipo = dicionarioProdutos.values.groupBy { it.tipo }

        FileWriter(arquivoCsv, false).use { writer ->
            writer.append("Relatório de Produção - $dataStr\n")
            writer.append("Loja 012\n\n")

            writer.append("Tipo,Nome,Quantidade\n")

            var totalGeral = 0
            for ((tipo, produtos) in agrupadoPorTipo.toSortedMap()) {
                var totalTipo = 0
                for (produto in produtos) {
                    writer.append("${tipo},${produto.nome},${produto.quantidade}\n")
                    totalTipo += produto.quantidade
                    totalGeral += produto.quantidade
                }
                writer.append("TOTAL ${tipo}, ,${totalTipo}\n\n")
            }

            writer.append("TOTAL GERAL, ,${totalGeral}\n")
        }

        Log.i("SalvarCsv", "CSV salvo em: ${arquivoCsv.absolutePath}")
        return arquivoCsv
    }
}
