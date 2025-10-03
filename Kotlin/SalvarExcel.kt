package com.exemplo.natabase

import android.util.Log
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object SalvarExcel {
    private val TIPO_CORES = listOf("FFC7CE", "C6EFCE", "FFEB9C", "BDD7EE", "F4CCCC")
    private val LINHA_ALTERNADA = listOf("FFFFFF", "F2F2F2")

    fun salvarProducaoDiaria(
        dicionarioProdutos: Map<String, Produto>,
        data: Date = Date(),
        nomeArquivo: String = "Loja012_2025.xlsx",
        pastaDestino: File
    ): File {
        val dataStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(data)
        val arquivoExcel = File(pastaDestino, nomeArquivo)

        val wb: Workbook = if (arquivoExcel.exists()) {
            XSSFWorkbook(arquivoExcel.inputStream())
        } else {
            XSSFWorkbook()
        }

        val ws = wb.getSheet(dataStr) ?: wb.createSheet(dataStr)

        val existentes = mutableMapOf<String, Pair<String, Int>>()
        for (produto in dicionarioProdutos.values) {
            val nome = produto.getNome()
            val tipo = produto.getTipo()
            val qtd = produto.getQuantidade()
            val atual = existentes[nome]
            if (atual != null) {
                existentes[nome] = tipo to (atual.second + qtd)
            } else {
                existentes[nome] = tipo to qtd
            }
        }

        val porTipo = existentes.entries.groupBy { it.value.first }
            .mapValues { entry ->
                entry.value.map { it.key to it.value.second }
                    .sortedByDescending { it.second }
            }

        val startRow = 2
        val startCol = 4
        val headers = listOf("Nome", "Tipo", "Quantidade")

        val headerStyle = wb.createCellStyle().apply {
            setFont(wb.createFont().apply {
                bold = true
                color = IndexedColors.WHITE.index
                fontHeightInPoints = 11
            })
            fillForegroundColor = IndexedColors.BLUE_GREY.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }

        val rowHeader = ws.createRow(startRow)
        headers.forEachIndexed { i, h ->
            val cell = rowHeader.createCell(startCol + i)
            cell.setCellValue(h)
            cell.cellStyle = headerStyle
            ws.setColumnWidth(startCol + i, listOf(20, 15, 12)[i] * 256)
        }

        var currentRow = startRow + 1
        var totalGeral = 0
        var corIndex = 0

        porTipo.toSortedMap().forEach { (tipo, lista) ->
            var totalTipo = 0
            lista.forEachIndexed { i, (nome, qtd) ->
                val row = ws.createRow(currentRow)
                listOf(nome, tipo, qtd).forEachIndexed { j, valor ->
                    val cell = row.createCell(startCol + j)
                    when (valor) {
                        is String -> cell.setCellValue(valor)
                        is Int -> cell.setCellValue(valor.toDouble())
                    }
                    val fillColor = if (i % 2 == 0) LINHA_ALTERNADA[0] else LINHA_ALTERNADA[1]
                    val style = wb.createCellStyle().apply {
                        fillForegroundColor = IndexedColors.valueOf(fillColor)?.index ?: IndexedColors.WHITE.index
                        fillPattern = FillPatternType.SOLID_FOREGROUND
                        borderBottom = BorderStyle.THIN
                        borderTop = BorderStyle.THIN
                        borderLeft = BorderStyle.THIN
                        borderRight = BorderStyle.THIN
                        alignment = HorizontalAlignment.CENTER
                        verticalAlignment = VerticalAlignment.CENTER
                    }
                    cell.cellStyle = style
                }
                totalTipo += qtd
                totalGeral += qtd
                currentRow++
            }

            val rowTotal = ws.createRow(currentRow)
            rowTotal.createCell(startCol).setCellValue("Total $tipo")
            rowTotal.createCell(startCol + 2).setCellValue(totalTipo.toDouble())
            currentRow++
            corIndex++
        }

        val rowTotalGeral = ws.createRow(currentRow)
        rowTotalGeral.createCell(startCol).setCellValue("TOTAL GERAL")
        rowTotalGeral.createCell(startCol + 2).setCellValue(totalGeral.toDouble())

        FileOutputStream(arquivoExcel).use { wb.write(it) }
        wb.close()

        Log.i("SalvarExcel", "Planilha atualizada em ${arquivoExcel.absolutePath}")
        return arquivoExcel
    }
}
