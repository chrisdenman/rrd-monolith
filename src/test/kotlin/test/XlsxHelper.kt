package uk.co.ceilingcat.rrd.monolith.test

import org.apache.poi.openxml4j.opc.internal.FileHelper.copyFile
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory.create
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.DAY_HENCE_NOTIFIED_OF
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.EXPECTED_XLSX_DATE_FORMAT
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.EXTENSION_XLSX
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.WORKSHEETS
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.WORKSHEETS.HISTORIC
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.worksheetFile
import java.io.File
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

data class SheetAddress(val name: String)
data class CellAddress(val sheet: SheetAddress, val textValue: String)
data class CellData(val address: CellAddress, val textValue: String)

class XlsxHelper {

    companion object {

        fun withEmpty(directory: File? = null): File = withSheet(HISTORIC, directory = directory)

        fun withSheet(
            worksheet: WORKSHEETS,
            cellData: List<CellData> = emptyList(),
            directory: File? = null
        ): File =
            createModifiedTempXlsx(
                worksheetFile(worksheet),
                cellData,
                directory
            )

        fun withRefuseDaysHence(
            daysHence: Long = DAY_HENCE_NOTIFIED_OF,
            directory: File? = null
        ): File = withSheet(
            HISTORIC,
            listOf(
                CellData(
                    CellAddress(
                        SheetAddress("Friday Wk2"),
                        "B3"
                    ),
                    LocalDate.now().plusDays(daysHence).format(
                        DateTimeFormatter.ofPattern(EXPECTED_XLSX_DATE_FORMAT)
                    )
                )
            ),
            directory
        )

        private fun createModifiedTempXlsx(input: File, cellData: List<CellData>, directory: File? = null): File =
            (directory ?: createTempDirectory()).also { worksheetsSearchDirectory ->
                createTemporaryFile(
                    UUID.randomUUID().toString(),
                    EXTENSION_XLSX,
                    worksheetsSearchDirectory
                ).also { tempFile ->
                    copyFile(input, tempFile)

                    loadWorkbook(tempFile, false).use { workbook ->
                        workbook
                            .sheetIterator().forEach { sheet ->
                                cellData.filter { (address) ->
                                    address.sheet.name == sheet.sheetName
                                }.forEach { (address, textValue) ->
                                    getCell(sheet, address)?.setCellValue(textValue)
                                }
                            }
                        workbook.write(
                            object : OutputStream() {
                                override fun write(b: Int) {} // https://stackoverflow.com/questions/52389798/java-io-eofexception-unexpected-end-of-zlib-input-stream-using-apache-poi/52389913
                            }
                        )
                    }
                }
            }

        private fun loadWorkbook(input: File, readOnly: Boolean = true): Workbook =
            create(input, null, readOnly)!!

        private fun getCell(sheet: Sheet, cellAddress: CellAddress): Cell? {
            for (row in sheet.rowIterator()) {
                for (cell in row.cellIterator()) {
                    if (cell.address.formatAsString() == cellAddress.textValue)
                        return cell
                }
            }
            return null
        }
    }
}
