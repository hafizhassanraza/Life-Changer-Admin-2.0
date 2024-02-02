package com.enfotrix.adminlifechanger.Pdf


import com.enfotrix.adminlifechanger.Models.ModelEarning
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

class PdfEstatmentHistory(val filteredApprovedInvesmentList: List<TransactionModel>, val firstName: String) {
    fun generatePdf(outputStream: OutputStream): Boolean {
        val document = Document()

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream)
            document.open()
            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, BaseColor.BLACK)

            val title: Paragraph = Paragraph("$firstName E-statement ", titleFont)


            title.alignment = Element.ALIGN_CENTER
            document.add(title)
            document.add(Paragraph("\n"))

            val table = PdfPTable(5)
            table.widthPercentage = 100f
            val headers = arrayOf(
                Paragraph("Old Balance", titleFont),
                Paragraph("Req Balance", titleFont),
                Paragraph("Req Type", titleFont),
                Paragraph("New Balance", titleFont),
                Paragraph("Req Date", titleFont)
            )
            for (header in headers) {
                val cell = PdfPCell(Phrase(header))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(cell)
            }
            for (item in filteredApprovedInvesmentList) {
                table.addCell(item.previousBalance)
                table.addCell(item.amount)
                table.addCell(item.type)
                table.addCell(item.newBalance)
                table.addCell(SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(item.createdAt.toDate()))
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
