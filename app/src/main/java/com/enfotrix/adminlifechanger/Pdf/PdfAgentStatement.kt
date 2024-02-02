package com.enfotrix.adminlifechanger.Pdf


import com.enfotrix.adminlifechanger.Models.ModelEarning
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

class PdfAgentStatement(val filteredApprovedInvesmentList: List<ModelEarning>, val firstName: String) {
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

            val table = PdfPTable(4)
            table.widthPercentage = 100f
            val headers = arrayOf(
                Paragraph("Old Balance", titleFont),
                Paragraph("Earning Amount", titleFont),
                Paragraph("Date", titleFont),
                Paragraph("Remarks", titleFont),
            )
            for (header in headers) {
                val cell = PdfPCell(Phrase(header))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(cell)
            }
            for (item in filteredApprovedInvesmentList) {
                table.addCell(item.balance)
                table.addCell(item.amount)
                table.addCell(
                    SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(item.createdAt?.toDate())
                )
                table.addCell(item.disc)
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
