package com.belasoft.natabase_alpha

import android.content.Context
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.util.CellRangeAddress
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelService {

    private const val FILE_NAME = "mapa_producao.xlsx"
    private const val MAX_PRODUCAO = 5

    private val COR_TEXTO_CASTANHO = IndexedColors.BROWN.index
    private val COR_FUNDO_BRANCO = IndexedColors.WHITE.index
    private val COR_FUNDO_CASTANHO = IndexedColors.BROWN.index
    private val COR_FUNDO_CINZA_CLARO = IndexedColors.GREY_25_PERCENT.index
    private val COR_FUNDO_AMARELO = IndexedColors.LIGHT_YELLOW.index
    private val COR_BORDA = IndexedColors.BLACK.index

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

            val data = sheet.getRow(0)?.getCell(13)?.toString() ?: ""
            val diaSemana = sheet.getRow(1)?.getCell(13)?.toString() ?: ""

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

                val validade = row.getCell(2)?.toString() ?: ""

                val producoes = listOf(
                    ProducaoDia(
                        quantidade = row.getCell(3)?.toString()?.toIntOrNull() ?: 0,
                        hora = row.getCell(4)?.toString() ?: ""
                    ),
                    ProducaoDia(
                        quantidade = row.getCell(5)?.toString()?.toIntOrNull() ?: 0,
                        hora = row.getCell(6)?.toString() ?: ""
                    ),
                    ProducaoDia(
                        quantidade = row.getCell(7)?.toString()?.toIntOrNull() ?: 0,
                        hora = row.getCell(8)?.toString() ?: ""
                    ),
                    ProducaoDia(
                        quantidade = row.getCell(9)?.toString()?.toIntOrNull() ?: 0,
                        hora = row.getCell(10)?.toString() ?: ""
                    ),
                    ProducaoDia(
                        quantidade = row.getCell(11)?.toString()?.toIntOrNull() ?: 0,
                        hora = row.getCell(12)?.toString() ?: ""
                    )
                )

                val perdas = row.getCell(13)?.toString()?.toIntOrNull() ?: 0
                val sobras = row.getCell(14)?.toString()?.toIntOrNull() ?: 0

                itens.add(ItemProducao(ultimaCategoria, produto, producoes, perdas, sobras))
                currentRow++
            }

            workbook.close()
            return MapaProducao(data, diaSemana, itens)
        }
    }

    private fun criarNovoMapa(context: Context): MapaProducao {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Planilha1")

        criarEstruturaCompleta(workbook, sheet)

        val file = File(context.getExternalFilesDir(null), FILE_NAME)
        FileOutputStream(file).use { fos ->
            workbook.write(fos)
        }
        workbook.close()

        return carregarMapaProducao(context)
    }

    private fun criarEstruturaCompleta(workbook: XSSFWorkbook, sheet: Sheet) {
        sheet.setColumnWidth(0, 25 * 256)
        sheet.setColumnWidth(1, 30 * 256)
        sheet.setColumnWidth(2, 12 * 256)

        for (i in 3..12) {
            sheet.setColumnWidth(i, 8 * 256)
        }

        sheet.setColumnWidth(13, 10 * 256)
        sheet.setColumnWidth(14, 10 * 256)

        val row1 = sheet.createRow(0)
        val cellA1 = criarCelulaComEstilo(workbook, row1, 0, COR_FUNDO_BRANCO, true, 14, COR_TEXTO_CASTANHO)
        cellA1.setCellValue("MAPA DE PRODUÇÃO VITRINA DE PASTELARIA")
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 10))

        val cellL1 = criarCelulaComEstilo(workbook, row1, 11, COR_FUNDO_CASTANHO, false, 11, COR_FUNDO_BRANCO)
        cellL1.setCellValue("Data:")
        val cellM1 = criarCelulaComEstilo(workbook, row1, 12, COR_FUNDO_CASTANHO, false, 11, COR_FUNDO_BRANCO)
        sheet.addMergedRegion(CellRangeAddress(0, 0, 11, 12))

        criarCelulaComEstilo(workbook, row1, 13, COR_FUNDO_BRANCO, false, 11)
        criarCelulaComEstilo(workbook, row1, 14, COR_FUNDO_BRANCO, false, 11)

        val row2 = sheet.createRow(1)
        val cellL2 = criarCelulaComEstilo(workbook, row2, 11, COR_FUNDO_CASTANHO, false, 11, COR_FUNDO_BRANCO)
        cellL2.setCellValue("Dia Semana:")
        val cellM2 = criarCelulaComEstilo(workbook, row2, 12, COR_FUNDO_CASTANHO, false, 11, COR_FUNDO_BRANCO)
        sheet.addMergedRegion(CellRangeAddress(1, 1, 11, 12))

        criarCelulaComEstilo(workbook, row2, 13, COR_FUNDO_BRANCO, false, 11)
        criarCelulaComEstilo(workbook, row2, 14, COR_FUNDO_BRANCO, false, 11)

        sheet.createRow(2)

        val row4 = sheet.createRow(3)

        val cellA4 = criarCelulaComEstilo(workbook, row4, 0, COR_FUNDO_CASTANHO, true, 11, COR_FUNDO_BRANCO, false, true, true, true)
        cellA4.setCellValue("VITRINA DE PASTELARIA\n(TEMP. 15 a 20ºC)")
        sheet.addMergedRegion(CellRangeAddress(3, 3, 0, 1))

        val cellC4 = criarCelulaComEstilo(workbook, row4, 2, COR_FUNDO_CASTANHO, true, 11, COR_FUNDO_BRANCO, true, true, false, false)
        cellC4.setCellValue("Validade\nExposição")

        val producoesHeaders = listOf("Produção 1", "Produção 2", "Produção 3", "Produção 4", "Produção 5")
        for (i in producoesHeaders.indices) {
            val coluna = 3 + (i * 2)
            val cell = criarCelulaComEstilo(workbook, row4, coluna, COR_FUNDO_CASTANHO, true, 11, COR_FUNDO_BRANCO, true, true, false, false)
            cell.setCellValue(producoesHeaders[i])
            sheet.addMergedRegion(CellRangeAddress(3, 3, coluna, coluna + 1))
        }

        val cellPerdas = criarCelulaComEstilo(workbook, row4, 13, COR_FUNDO_CASTANHO, true, 11, COR_FUNDO_BRANCO, true, false, true, true)
        cellPerdas.setCellValue("PERDAS")

        val cellSobras = criarCelulaComEstilo(workbook, row4, 14, COR_FUNDO_CASTANHO, true, 11, COR_FUNDO_BRANCO, true, false, true, true)
        cellSobras.setCellValue("SOBRAS")

        val row5 = sheet.createRow(4)

        criarCelulaComEstilo(workbook, row5, 0, COR_FUNDO_CASTANHO, false, 10, COR_FUNDO_BRANCO, false, true, true, true)
        criarCelulaComEstilo(workbook, row5, 1, COR_FUNDO_CASTANHO, false, 10, COR_FUNDO_BRANCO, false, true, true, true)

        criarCelulaComEstilo(workbook, row5, 2, COR_FUNDO_CASTANHO, false, 10, COR_FUNDO_BRANCO, true, true, false, false)

        for (i in 0 until 5) {
            val colunaBase = 3 + (i * 2)
            val cellQt = criarCelulaComEstilo(workbook, row5, colunaBase, COR_FUNDO_CASTANHO, true, 10, COR_FUNDO_BRANCO, true, true, false, false)
            cellQt.setCellValue("Qt")
            val cellHr = criarCelulaComEstilo(workbook, row5, colunaBase + 1, COR_FUNDO_CASTANHO, true, 10, COR_FUNDO_BRANCO, true, true, false, false)
            cellHr.setCellValue("Hr")
        }

        criarCelulaComEstilo(workbook, row5, 13, COR_FUNDO_CASTANHO, false, 10, COR_FUNDO_BRANCO, true, false, true, true)
        criarCelulaComEstilo(workbook, row5, 14, COR_FUNDO_CASTANHO, false, 10, COR_FUNDO_BRANCO, true, false, true, true)

        criarProdutos(workbook, sheet)
    }

    private fun criarProdutos(workbook: XSSFWorkbook, sheet: Sheet) {
        val produtos = listOf(
            // PASTELARIA
            arrayOf("PASTELARIA", "BOLA DE BERLIM", "2 D"),
            arrayOf("", "BOLO CENOURA FATIA", "2 D"),
            arrayOf("", "BOLO CHOCOLATE FATIA", "2 D"),
            arrayOf("", "BOLO DE ARROZ", "24 Hrs"),
            arrayOf("", "BRIGADEIRO", "5 D"),
            arrayOf("", "FRIPANUTS CHOCOLATE", "24 Hrs"),
            arrayOf("", "FRIPANUTS SUGAR", "24 Hrs"),
            arrayOf("", "MINI CHAUSSON", "Próprio Dia*"),
            arrayOf("", "TRANÇA CREME E MAÇÃ", "Próprio Dia*"),
            arrayOf("", "MUFFIN CHOCOLATE", "2 D"),
            arrayOf("", "MUFFIN LIMÃO", "2 D"),
            arrayOf("", "MUFFIN NOZ", "2 D"),
            arrayOf("", "PAO DEUS SIMPLES", "24 Hrs"),
            arrayOf("", "PASTEL DE NATA", "Próprio Dia*"),
            arrayOf("", "QUEIJADA DE LEITE", "3 D"),
            arrayOf("", "QUEIJADA DE MARACUJÁ", "3 D"),
            arrayOf("", "QUEIJADA LARANJA", "3 D"),
            arrayOf("", "QUEIJADA FEIJÃO", "3 D"),
            arrayOf("", "SCONE SIMPLES", "24 Hrs"),
            arrayOf("", "TARTE MAÇÃ PREMIUM", "24 Hrs"),

            // CROISSANTS
            arrayOf("CROISSANTS", "CROISSANT CHOC E AVELÃ", "Próprio Dia*"),
            arrayOf("", "CROISSANT SIMPLES", "Próprio Dia*"),
            arrayOf("", "CROISSANT MULTICEREAIS", "Próprio Dia*"),
            arrayOf("", "CROISSANT MULTICEREAIS MISTO", "Próprio Dia*"),
            arrayOf("", "CROISSANT MISTO", "4 Hrs"),
            arrayOf("", "PÃO DE DEUS MISTO", "4 Hrs"),

            // SALGADOS
            arrayOf("SALGADOS", "CHAMUÇA", "12 Hrs"),
            arrayOf("", "CROQ CARNE", "12 Hrs"),
            arrayOf("", "EMPANADA CAPRESE", "Próprio Dia*"),
            arrayOf("", "EMPANADA FRANGO", "Próprio Dia*"),
            arrayOf("", "EMPANADA Q/F", "Próprio Dia*"),
            arrayOf("", "EMPANADAS DE ATUM", "Próprio Dia*"),
            arrayOf("", "FOLHADO MISTO CARNE", "Próprio Dia*"),
            arrayOf("", "NAPOLITANA MISTA", "Próprio Dia*"),
            arrayOf("", "PASTEIS BAC", "12 Hrs"),
            arrayOf("", "RISSOL CARNE", "12 Hrs"),
            arrayOf("", "RISSOL MARISCO", "12 Hrs"),

            // TOSTAS / SANDUÍCHES
            arrayOf("TOSTAS / SANDUÍCHES", "BAGUETE AMERICANA", "4 Hrs"),
            arrayOf("", "BAGUETE ATUM", "4 Hrs"),
            arrayOf("", "BAGUETE DELICIAS", "4 Hrs"),
            arrayOf("", "BAGUETE PRESUNTO QUEI", "4 Hrs"),
            arrayOf("", "BOLA PANADO DE PORCO", "4 Hrs"),
            arrayOf("", "PAO QUEIJO FRESCO", "4 Hrs"),
            arrayOf("", "PAO SALMAO FUMADO", "4 Hrs"),
            arrayOf("", "SD FRANGO COGUMELOS", "4 Hrs"),
            arrayOf("", "BAGUETE PRESUNTO", "4 Hrs"),
            arrayOf("", "BOLA 110 GRS MISTA", "4 Hrs"),
            arrayOf("", "BOLA PANADO DE PORCO (S/ Alface)", "12 Hrs"),
            arrayOf("", "TOSTA ATUM SALOIA", "4 Hrs"),
            arrayOf("", "TOSTA FRANGO SALOIA", "4 Hrs"),
            arrayOf("", "TOSTA MISTA SALOIA", "4 Hrs"),
            arrayOf("", "TOSTA PRESUNTO/QUEIJO SALOIA", "4 Hrs"),
            arrayOf("", "BOLO CACO MISTO", "4 Hrs"),
            arrayOf("", "SD AMERICANA", "4 Hrs"),

            // REGIONAIS
            arrayOf("REGIONAIS", "PÃO DE LÓ OVAR PEQ 85 GRS", "5 D"),
            arrayOf("", "OVOS MOLES UND", "10 D"),
            arrayOf("", "TARTES DE AMÊNDOA UND", "10 D"),
            arrayOf("", "TRAVESSEIRO SINTRA", "Primária"),
            arrayOf("", "PASTEIS TORRES VEDRAS UND", "10 D"),
            arrayOf("", "PASTEIS AGUEDA UND", "10 D"),
            arrayOf("", "PASTEIS VOUZELA UND", "7 D"),
            arrayOf("", "TORTA DE AZEITÃO UND", "24 Hrs"),
            arrayOf("", "QUEIJADA MADEIRENSE", "24 Hrs"),
            arrayOf("", "MALASADA CREME (FRESCO)", "Próprio Dia"),
            arrayOf("", "SALAME (FATIA)", "Próprio Dia"),

            // PÃO
            arrayOf("PÃO", "BAGUETE", "Próprio Dia"),
            arrayOf("", "BOLA LENHA", "Próprio Dia"),
            arrayOf("", "PÃO CEREAIS", "Próprio Dia"),
            arrayOf("", "PÃO RUSTICO FATIAS", "24 Hrs")
        )

        var rowIndex = 5
        var categoriaInicio = 5
        var categoriaAtual = ""

        for (produto in produtos) {
            val row = sheet.createRow(rowIndex)

            val categoria = produto[0]
            val nomeProduto = produto[1]
            val validade = produto[2]

            if (categoria.isNotBlank() && categoria != categoriaAtual) {
                if (categoriaAtual.isNotBlank() && rowIndex > categoriaInicio) {
                    sheet.addMergedRegion(CellRangeAddress(categoriaInicio, rowIndex - 1, 0, 0))
                }
                categoriaAtual = categoria
                categoriaInicio = rowIndex
            }

            val cellCategoria = criarCelulaComEstilo(workbook, row, 0, COR_FUNDO_BRANCO, false, 11)
            if (categoria.isNotBlank()) {
                cellCategoria.setCellValue(categoria)
            }

            val cellProduto = criarCelulaComEstilo(workbook, row, 1, COR_FUNDO_BRANCO, false, 11)
            cellProduto.setCellValue(nomeProduto)

            val cellValidade = criarCelulaComEstilo(workbook, row, 2, COR_FUNDO_CINZA_CLARO, false, 11)
            cellValidade.setCellValue(validade)
            for (i in 3..12) {
                val corFundo = if (i % 2 == 1) {
                    COR_FUNDO_CINZA_CLARO
                } else {
                    COR_FUNDO_BRANCO
                }
                criarCelulaComEstilo(workbook, row, i, corFundo, false, 10)
            }

            val cellPerdas = criarCelulaComEstilo(workbook, row, 13, COR_FUNDO_AMARELO, false, 10)

            val cellSobras = criarCelulaComEstilo(workbook, row, 14, COR_FUNDO_BRANCO, false, 10)

            rowIndex++
        }

        if (categoriaAtual.isNotBlank() && rowIndex > categoriaInicio) {
            sheet.addMergedRegion(CellRangeAddress(categoriaInicio, rowIndex - 1, 0, 0))
        }
    }

    private fun criarCelulaComEstilo(
        workbook: XSSFWorkbook,
        row: Row,
        colIndex: Int,
        corFundo: Short,
        negrito: Boolean,
        tamanhoFonte: Int,
        corTexto: Short? = null,
        bordaEsquerda: Boolean = true,
        bordaDireita: Boolean = true,
        bordaTopo: Boolean = true,
        bordaBase: Boolean = true
    ): Cell {
        val cell = row.createCell(colIndex)
        val estilo = workbook.createCellStyle()

        if (bordaEsquerda) {
            estilo.borderLeft = BorderStyle.THIN
            estilo.leftBorderColor = COR_BORDA
        }
        if (bordaDireita) {
            estilo.borderRight = BorderStyle.THIN
            estilo.rightBorderColor = COR_BORDA
        }
        if (bordaTopo) {
            estilo.borderTop = BorderStyle.THIN
            estilo.topBorderColor = COR_BORDA
        }
        if (bordaBase) {
            estilo.borderBottom = BorderStyle.THIN
            estilo.bottomBorderColor = COR_BORDA
        }

        estilo.fillForegroundColor = corFundo
        estilo.fillPattern = FillPatternType.SOLID_FOREGROUND

        estilo.alignment = HorizontalAlignment.CENTER
        estilo.verticalAlignment = VerticalAlignment.CENTER

        estilo.wrapText = true

        val fonte = workbook.createFont()
        fonte.bold = negrito
        fonte.fontHeightInPoints = tamanhoFonte.toShort()

        if (corTexto != null) {
            fonte.color = corTexto
        }

        estilo.setFont(fonte)

        cell.cellStyle = estilo
        return cell
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

            val cellDataN = sheet.getRow(0).getCell(13) ?: sheet.getRow(0).createCell(13)
            cellDataN.setCellValue(hoje)

            val mergedRegionData = CellRangeAddress(0, 0, 13, 14)
            if (!isRegionMerged(sheet, mergedRegionData)) {
                sheet.addMergedRegion(mergedRegionData)
            }

            val cellDiaN = sheet.getRow(1).getCell(13) ?: sheet.getRow(1).createCell(13)
            cellDiaN.setCellValue(diaSemana.capitalize(Locale.getDefault()))

            val mergedRegionDia = CellRangeAddress(1, 1, 13, 14)
            if (!isRegionMerged(sheet, mergedRegionDia)) {
                sheet.addMergedRegion(mergedRegionDia)
            }

            val colunaQt = 3 + (producaoIndex - 1) * 2
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

                        val cellQt = row.getCell(colunaQt) ?: row.createCell(colunaQt)
                        cellQt.setCellValue(quantidade.toDouble())

                        val cellHr = row.getCell(colunaHr) ?: row.createCell(colunaHr)
                        cellHr.setCellValue(horaAtual)

                        val cellPerdas = row.getCell(13) ?: row.createCell(13)
                        if (item.perdas > 0) {
                            cellPerdas.setCellValue(item.perdas.toDouble())
                        }

                        val cellSobras = row.getCell(14) ?: row.createCell(14)
                        if (item.sobras > 0) {
                            cellSobras.setCellValue(item.sobras.toDouble())
                        }
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

    private fun isRegionMerged(sheet: Sheet, region: CellRangeAddress): Boolean {
        for (i in 0 until sheet.numMergedRegions) {
            val existingRegion = sheet.getMergedRegion(i)
            if (existingRegion.firstRow == region.firstRow &&
                existingRegion.lastRow == region.lastRow &&
                existingRegion.firstColumn == region.firstColumn &&
                existingRegion.lastColumn == region.lastColumn) {
                return true
            }
        }
        return false
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
                    for (col in 3..14) {
                        val cell = row.getCell(col)
                        if (cell != null) {
                            cell.setCellValue("")
                        }
                    }
                }
                rowIndex++
            }

            sheet.getRow(0)?.getCell(13)?.setCellValue("")
            sheet.getRow(0)?.getCell(14)?.setCellValue("")
            sheet.getRow(1)?.getCell(13)?.setCellValue("")
            sheet.getRow(1)?.getCell(14)?.setCellValue("")

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
