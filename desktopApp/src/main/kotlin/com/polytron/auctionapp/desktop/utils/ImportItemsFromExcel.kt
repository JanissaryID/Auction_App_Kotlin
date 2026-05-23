package com.polytron.auctionapp.desktop.utils

import com.polytron.auctionapp.domain.model.ItemResponse
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.math.roundToLong

data class ExcelImportRow(
    val rowNumber: Int,
    val item: ItemResponse
)

data class ExcelImportResult(
    val filePath: String,
    val rows: List<ExcelImportRow>,
    val skippedRows: List<String>
)

private enum class ImportColumn {
    Name,
    Code,
    BasePrice,
    MaxPrice,
    AuctionPrice
}

private val headerAliases = mapOf(
    ImportColumn.Name to setOf("nama", "name", "namabarang", "nameitem"),
    ImportColumn.Code to setOf("kode", "code", "kodebarang", "codeitem"),
    ImportColumn.BasePrice to setOf("hargadasar", "baseprice", "hargaawal"),
    ImportColumn.MaxPrice to setOf("hargamaks", "hargamaksimal", "maxprice", "maximumprice"),
    ImportColumn.AuctionPrice to setOf("hargalelang", "price", "auctionprice")
)

fun importItemsFromExcelDesktop(): Result<ExcelImportResult> {
    return try {
        val file = chooseExcelFile()
            ?: return Result.failure(Exception("Import dibatalkan"))

        importItemsFromExcelFile(file)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

fun importItemsFromExcelFile(file: File): Result<ExcelImportResult> {
    return try {
        XSSFWorkbook(file.inputStream()).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            val formatter = DataFormatter()
            val evaluator = workbook.creationHelper.createFormulaEvaluator()
            val headerRow = (0..sheet.lastRowNum)
                .asSequence()
                .mapNotNull { sheet.getRow(it) }
                .firstOrNull { row ->
                    val columns = readHeaderColumns(row.cellIterator().asSequence().toList(), formatter, evaluator)
                    columns.containsKey(ImportColumn.Name) &&
                        columns.containsKey(ImportColumn.Code) &&
                        columns.containsKey(ImportColumn.BasePrice) &&
                        columns.containsKey(ImportColumn.MaxPrice)
                }
                ?: return Result.failure(Exception("Format Excel tidak dikenali. Gunakan export daftar barang sebagai template."))

            val columns = readHeaderColumns(headerRow.cellIterator().asSequence().toList(), formatter, evaluator)
            val rows = mutableListOf<ExcelImportRow>()
            val skippedRows = mutableListOf<String>()

            for (rowIndex in (headerRow.rowNum + 1)..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue
                val rowNumber = rowIndex + 1
                val name = textAt(row.getCell(columns.getValue(ImportColumn.Name)), formatter, evaluator)
                val code = textAt(row.getCell(columns.getValue(ImportColumn.Code)), formatter, evaluator)

                if (name.isBlank() && code.isBlank()) continue
                if (isSummaryRow(name)) continue

                val basePrice = priceAt(row.getCell(columns.getValue(ImportColumn.BasePrice)), formatter, evaluator)
                val maxPrice = priceAt(row.getCell(columns.getValue(ImportColumn.MaxPrice)), formatter, evaluator)
                val auctionPrice = columns[ImportColumn.AuctionPrice]
                    ?.let { priceAt(row.getCell(it), formatter, evaluator) }
                    .orEmpty()

                val missingFields = buildList {
                    if (name.isBlank()) add("Nama")
                    if (code.isBlank()) add("Kode")
                    if (basePrice.isBlank()) add("Harga Dasar")
                    if (maxPrice.isBlank()) add("Harga Maks")
                }

                if (missingFields.isNotEmpty()) {
                    skippedRows += "Baris $rowNumber dilewati: ${missingFields.joinToString(", ")} kosong"
                    continue
                }

                rows += ExcelImportRow(
                    rowNumber = rowNumber,
                    item = ItemResponse(
                        nameItem = name,
                        codeItem = code,
                        basePrice = basePrice,
                        maxPrice = maxPrice,
                        price = auctionPrice.ifBlank { null },
                        status = 0,
                        admin = "admin"
                    )
                )
            }

            Result.success(
                ExcelImportResult(
                    filePath = file.absolutePath,
                    rows = rows,
                    skippedRows = skippedRows
                )
            )
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun chooseExcelFile(): File? {
    var selectedFile: File? = null
    val choose = Runnable {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Pilih File Excel Barang"
            fileFilter = FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx")
        }

        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.selectedFile
        }
    }

    if (SwingUtilities.isEventDispatchThread()) {
        choose.run()
    } else {
        SwingUtilities.invokeAndWait(choose)
    }

    return selectedFile
}

private fun readHeaderColumns(
    cells: List<Cell>,
    formatter: DataFormatter,
    evaluator: FormulaEvaluator
): Map<ImportColumn, Int> {
    val columns = mutableMapOf<ImportColumn, Int>()
    cells.forEach { cell ->
        val normalizedHeader = normalizeHeader(textAt(cell, formatter, evaluator))
        headerAliases.forEach { (column, aliases) ->
            if (normalizedHeader in aliases && column !in columns) {
                columns[column] = cell.columnIndex
            }
        }
    }
    return columns
}

private fun textAt(
    cell: Cell?,
    formatter: DataFormatter,
    evaluator: FormulaEvaluator
): String {
    if (cell == null) return ""
    return formatter.formatCellValue(cell, evaluator).trim()
}

private fun priceAt(
    cell: Cell?,
    formatter: DataFormatter,
    evaluator: FormulaEvaluator
): String {
    if (cell == null) return ""

    return when (cell.cellType) {
        CellType.NUMERIC -> cell.numericCellValue.roundToLong().toString()
        CellType.FORMULA -> {
            val value = runCatching { evaluator.evaluate(cell) }.getOrNull()
            if (value?.cellType == CellType.NUMERIC) {
                value.numberValue.roundToLong().toString()
            } else {
                normalizePrice(textAt(cell, formatter, evaluator))
            }
        }
        else -> normalizePrice(textAt(cell, formatter, evaluator))
    }
}

private fun normalizePrice(value: String): String {
    val clean = value
        .trim()
        .replace("Rp", "", ignoreCase = true)
        .replace(Regex("\\s+"), "")

    if (clean.isBlank()) return ""

    val decimalZero = Regex("^\\d+[,.]0{1,2}$")
    if (decimalZero.matches(clean)) {
        return clean.substringBefore('.').substringBefore(',')
    }

    return clean.filter { it.isDigit() }.trimStart('0').ifBlank { "0" }
}

private fun normalizeHeader(value: String): String {
    return value.lowercase().replace(Regex("[^a-z0-9]"), "")
}

private fun isSummaryRow(name: String): Boolean {
    val normalized = normalizeHeader(name)
    return normalized == "total" || normalized.startsWith("jumlah")
}
