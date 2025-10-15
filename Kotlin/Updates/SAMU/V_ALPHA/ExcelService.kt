package com.belasoft.natabase_alpha

import android.content.Context
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelService {

    private const val FILE_NAME = "mapa_producao.xlsx"
    private const val MAX_PRODUCAO = 5

    fun carregarMapaProducao(context: Context): MapaProducao {
        val file = File(context.getExternalFilesDir(null), FILE_NAME)

        return if (file.exists()) {
            lerExcelExistente(file)
        } else {
            criarNovoMapa(context)
        }
    }

    private fun lerExcelExistente(file: File): MapaProducao {
        FileInputStream(file).use { fis ->
            val workbook = XSSFWorkbook(fis)
            val sheet = workbook.getSheetAt(0)

            val data = sheet.getRow(0)?.getCell(10)?.toString() ?: ""
            val diaSemana = sheet.getRow(1)?.getCell(10)?.toString() ?: ""

            val itens = mutableListOf<ItemProducao>()
            var currentRow = 5
            var ultimaCategoria = ""

            while (currentRow <= sheet.lastRowNum) {
                val row = sheet.getRow(currentRow)
                if (row == null) {
                    currentRow++
                    continue
                }

                val categoria = row.getCell(0)?.toString() ?: ""
                val produto = row.getCell(1)?.toString() ?: ""

                if (categoria.isNotBlank()) {
                    ultimaCategoria = categoria
                }

                if (produto.isBlank()) {
                    currentRow++
                    continue
                }

                val producoes = listOf(
                    ProducaoDia(
                        quantidade = row.getCell(2)?.toString()?.toIntOrNull() ?: 0, // Coluna C (Qt Produção 1)
                        hora = row.getCell(3)?.toString() ?: "" // Coluna D (Hr Produção 1)
                    ),
                    ProducaoDia(
                        quantidade = row.getCell(4)?.toString()?.toIntOrNull() ?: 0, // Coluna E (Qt Produção 2)
                        hora = row.getCell(5)?.toString() ?: "" // Coluna F (Hr Produção 2)
                    ),
                    ProducaoDia(
                        quantidade = row.getCell(6)?.toString()?.toIntOrNull() ?: 0, // Coluna G (Qt Produção 3)
                        hora = row.getCell(7)?.toString() ?: "" // Coluna H (Hr Produção 3)
                    ),
                    ProducaoDia(
                        quantidade = row.getCell(8)?.toString()?.toIntOrNull() ?: 0, // Coluna I (Qt Produção 4)
                        hora = row.getCell(9)?.toString() ?: "" // Coluna J (Hr Produção 4)
                    ),
                    ProducaoDia(
                        quantidade = row.getCell(10)?.toString()?.toIntOrNull() ?: 0, // Coluna K (Qt Produção 5)
                        hora = row.getCell(11)?.toString() ?: "" // Coluna L (Hr Produção 5)
                    )
                )

                val perdas = row.getCell(12)?.toString()?.toIntOrNull() ?: 0 // Coluna M (PERDAS)
                val sobras = row.getCell(13)?.toString()?.toIntOrNull() ?: 0 // Coluna N (SOBRAS)

                itens.add(ItemProducao(ultimaCategoria, produto, producoes, perdas, sobras))
                currentRow++
            }

            println("ExcelService - Total de itens carregados: ${itens.size}")
            workbook.close()
            return MapaProducao(data, diaSemana, itens)
        }
    }

    private fun criarNovoMapa(context: Context): MapaProducao {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Planilha1")

        criarCabecalho(sheet)
        criarProdutos(sheet)

        val file = File(context.getExternalFilesDir(null), FILE_NAME)
        FileOutputStream(file).use { fos ->
            workbook.write(fos)
        }
        workbook.close()

        return carregarMapaProducao(context)
    }

    private fun criarCabecalho(sheet: Sheet) {
        val headerRow1 = sheet.createRow(0)
        headerRow1.createCell(0).setCellValue("MAPA DE PRODUÇÃO VITRINA DE PASTELARIA")
        headerRow1.createCell(10).setCellValue("Data:")

        val headerRow2 = sheet.createRow(1)
        headerRow2.createCell(10).setCellValue("Dia Semana:")

        sheet.createRow(2)

        val headerRow4 = sheet.createRow(3)
        headerRow4.createCell(0).setCellValue("VITRINA DE PASTELARIA\n(TEMP. 15 a 20ºC)")
        headerRow4.createCell(2).setCellValue("Produção 1")
        headerRow4.createCell(4).setCellValue("Produção 2")
        headerRow4.createCell(6).setCellValue("Produção 3")
        headerRow4.createCell(8).setCellValue("Produção 4")
        headerRow4.createCell(10).setCellValue("Produção 5")
        headerRow4.createCell(12).setCellValue("PERDAS")
        headerRow4.createCell(13).setCellValue("SOBRAS")

        val headerRow5 = sheet.createRow(4)
        headerRow5.createCell(2).setCellValue("Qt") // Coluna C
        headerRow5.createCell(3).setCellValue("Hr") // Coluna D
        headerRow5.createCell(4).setCellValue("Qt") // Coluna E
        headerRow5.createCell(5).setCellValue("Hr") // Coluna F
        headerRow5.createCell(6).setCellValue("Qt") // Coluna G
        headerRow5.createCell(7).setCellValue("Hr") // Coluna H
        headerRow5.createCell(8).setCellValue("Qt") // Coluna I
        headerRow5.createCell(9).setCellValue("Hr") // Coluna J
        headerRow5.createCell(10).setCellValue("Qt") // Coluna K
        headerRow5.createCell(11).setCellValue("Hr") // Coluna L
        headerRow5.createCell(12).setCellValue("Qt") // Coluna M
        headerRow5.createCell(13).setCellValue("Qt") // Coluna N

        sheet.setColumnWidth(0, 15 * 256)
        sheet.setColumnWidth(1, 25 * 256)
        for (i in 2..13) {
            sheet.setColumnWidth(i, 8 * 256)
        }
    }

    private fun criarProdutos(sheet: Sheet) {
        val produtos = listOf(
            // PASTELARIA
            arrayOf("PASTELARIA", "BOLA DE BERLIM"),
            arrayOf("", "BOLO CENOURA FATIA"),
            arrayOf("", "BOLO CHOCOLATE FATIA"),
            arrayOf("", "BOLO DE ARROZ"),
            arrayOf("", "BRIGADEIRO"),
            arrayOf("", "FRIPANUTS CHOCOLATE"),
            arrayOf("", "FRIPANUTS SUGAR"),
            arrayOf("", "MINI CHAUSSON"),
            arrayOf("", "TRANÇA CREME E MAÇÃ"),
            arrayOf("", "MUFFIN CHOCOLATE"),
            arrayOf("", "MUFFIN LIMÃO"),
            arrayOf("", "MUFFIN NOZ"),
            arrayOf("", "PAO DEUS SIMPLES"),
            arrayOf("", "PASTEL DE NATA"),
            arrayOf("", "QUEIJADA DE LEITE"),
            arrayOf("", "QUEIJADA DE MARACUJÁ"),
            arrayOf("", "QUEIJADA LARANJA"),
            arrayOf("", "QUEIJADA FEIJÃO"),
            arrayOf("", "SCONE SIMPLES"),
            arrayOf("", "TARTE MAÇÃ PREMIUM"),

            // CROISSANTS
            arrayOf("CROISSANTS", "CROISSANT CHOC E AVELÃ"),
            arrayOf("", "CROISSANT SIMPLES"),
            arrayOf("", "CROISSANT MULTICEREAIS"),
            arrayOf("", "CROISSANT MULTICEREAIS MISTO"),
            arrayOf("", "CROISSANT MISTO"),
            arrayOf("", "PÃO DE DEUS MISTO"),

            // SALGADOS
            arrayOf("SALGADOS", "CHAMUÇA"),
            arrayOf("", "CROQ CARNE"),
            arrayOf("", "EMPANADA CAPRESE"),
            arrayOf("", "EMPANADA FRANGO"),
            arrayOf("", "EMPANADA Q/F"),
            arrayOf("", "EMPANADAS DE ATUM"),
            arrayOf("", "FOLHADO MISTO CARNE"),
            arrayOf("", "NAPOLITANA MISTA"),
            arrayOf("", "PASTEIS BAC"),
            arrayOf("", "RISSOL CARNE"),
            arrayOf("", "RISSOL MARISCO"),

            // TOSTAS / SANDUÍCHES
            arrayOf("TOSTAS / SANDUÍCHES", "BAGUETE AMERICANA"),
            arrayOf("", "BAGUETE ATUM"),
            arrayOf("", "BAGUETE DELICIAS"),
            arrayOf("", "BAGUETE PRESUNTO QUEI"),
            arrayOf("", "BOLA PANADO DE PORCO"),
            arrayOf("", "PAO QUEIJO FRESCO"),
            arrayOf("", "PAO SALMAO FUMADO"),
            arrayOf("", "SD FRANGO COGUMELOS"),
            arrayOf("", "BAGUETE PRESUNTO"),
            arrayOf("", "BOLA 110 GRS MISTA"),
            arrayOf("", "BOLA PANADO DE PORCO (S/ Alface)"),
            arrayOf("", "TOSTA ATUM SALOIA"),
            arrayOf("", "TOSTA FRANGO SALOIA"),
            arrayOf("", "TOSTA MISTA SALOIA"),
            arrayOf("", "TOSTA PRESUNTO/QUEIJO SALOIA"),
            arrayOf("", "BOLO CACO MISTO"),
            arrayOf("", "SD AMERICANA"),

            // REGIONAIS
            arrayOf("REGIONAIS", "PÃO DE LÓ OVAR PEQ 85 GRS"),
            arrayOf("", "OVOS MOLES UND"),
            arrayOf("", "TARTES DE AMÊNDOA UND"),
            arrayOf("", "TRAVESSEIRO SINTRA"),
            arrayOf("", "PASTEIS TORRES VEDRAS UND"),
            arrayOf("", "PASTEIS AGUEDA UND"),
            arrayOf("", "PASTEIS VOUZELA UND"),
            arrayOf("", "TORTA DE AZEITÃO UND"),
            arrayOf("", "QUEIJADA MADEIRENSE"),
            arrayOf("", "MALASADA CREME (FRESCO)"),
            arrayOf("", "SALAME (FATIA)"),

            // PÃO
            arrayOf("PÃO", "BAGUETE"),
            arrayOf("", "BOLA LENHA"),
            arrayOf("", "PÃO CEREAIS"),
            arrayOf("", "PÃO RUSTICO FATIAS")
        )

        var rowIndex = 5
        for (produto in produtos) {
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(produto[0])
            row.createCell(1).setCellValue(produto[1])

            for (i in 2..13) {
                row.createCell(i)
            }
        }
    }

    fun salvarProducao(context: Context, mapa: MapaProducao, producaoIndex: Int): Boolean {
        if (producaoIndex > MAX_PRODUCAO) {
            return false
        }

        val file = File(context.getExternalFilesDir(null), FILE_NAME)
        FileInputStream(file).use { fis ->
            val workbook = XSSFWorkbook(fis)
            val sheet = workbook.getSheetAt(0)

            val hoje = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val diaSemana = SimpleDateFormat("EEEE", Locale("pt", "PT")).format(Date())

            if (sheet.getRow(0).getCell(10) == null) sheet.getRow(0).createCell(10)
            sheet.getRow(0).getCell(10).setCellValue(hoje)

            if (sheet.getRow(1).getCell(10) == null) sheet.getRow(1).createCell(10)
            sheet.getRow(1).getCell(10).setCellValue(diaSemana.capitalize(Locale.getDefault()))

            val colunaQt = 2 + (producaoIndex - 1) * 2
            val colunaHr = colunaQt + 1

            var rowIndex = 5
            while (rowIndex <= sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex)
                if (row == null) {
                    rowIndex++
                    continue
                }

                val produtoNome = row.getCell(1)?.toString() ?: ""
                if (produtoNome.isNotBlank()) {
                    val item = mapa.itens.find { it.produto == produtoNome }
                    if (item != null) {
                        val horaAtual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        val quantidade = item.producoes[producaoIndex - 1].quantidade

                        if (row.getCell(colunaQt) == null) row.createCell(colunaQt)
                        if (row.getCell(colunaHr) == null) row.createCell(colunaHr)

                        row.getCell(colunaQt).setCellValue(quantidade.toDouble())
                        row.getCell(colunaHr).setCellValue(horaAtual)

                        val colunaPerdas = 12
                        val colunaSobras = 13

                        if (row.getCell(colunaPerdas) == null) row.createCell(colunaPerdas)
                        row.getCell(colunaPerdas).setCellValue(item.perdas.toDouble())

                        if (row.getCell(colunaSobras) == null) row.createCell(colunaSobras)
                        row.getCell(colunaSobras).setCellValue(item.sobras.toDouble())
                    }
                }
                rowIndex++
            }

            FileOutputStream(file).use { fos ->
                workbook.write(fos)
            }
            workbook.close()
        }
        return true
    }

    fun limparProducoes(context: Context) {
        val file = File(context.getExternalFilesDir(null), FILE_NAME)
        FileInputStream(file).use { fis ->
            val workbook = XSSFWorkbook(fis)
            val sheet = workbook.getSheetAt(0)

            var rowIndex = 5
            while (rowIndex <= sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex)
                if (row != null) {
                    for (col in 2..13) {
                        val cell = row.getCell(col)
                        if (cell != null) {
                            cell.setCellValue("")
                        }
                    }
                }
                rowIndex++
            }

            sheet.getRow(0)?.getCell(10)?.setCellValue("")
            sheet.getRow(1)?.getCell(10)?.setCellValue("")

            FileOutputStream(file).use { fos ->
                workbook.write(fos)
            }
            workbook.close()
        }
    }

    fun regenerarExcelCompleto(context: Context): MapaProducao {
        val file = File(context.getExternalFilesDir(null), FILE_NAME)
        if (file.exists()) {
            file.delete()
        }
        return criarNovoMapa(context)
    }
}