package com.enfotrix.adminlifechanger.Pdf

import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.lifechanger.Models.TransactionModel
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class pdfFA(var List: List<ModelFA>) {
    fun generatePdf(outputStream: OutputStream): Boolean {
        val document = Document()

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream)
            document.open()
            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, BaseColor.BLACK)
            var title: Paragraph

            title = Paragraph("Financial Advisors", titleFont)

            title.alignment = Element.ALIGN_CENTER
            document.add(title)
            document.add(Paragraph("\n"))
            val table = PdfPTable(5)
            table.widthPercentage = 100f
            val headers = arrayOf(
                Paragraph("Name", titleFont),
                Paragraph("Phone", titleFont),
                Paragraph("CNIC", titleFont),
                Paragraph("Profit", titleFont),
                Paragraph("Status", titleFont)
            )
            for (header in headers) {
                val cell = PdfPCell(Phrase(header))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(cell)
            }
            for (item in List) {
                table.addCell(item.firstName+item.lastName)
                table.addCell(item.phone)
                table.addCell(item.cnic)
                table.addCell(item.profit)
                table.addCell(item.status)
            }
            document.add(table)
            document.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

}