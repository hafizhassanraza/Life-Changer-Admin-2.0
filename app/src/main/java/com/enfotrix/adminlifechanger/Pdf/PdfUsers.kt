package com.enfotrix.adminlifechanger.Pdf

import User
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import java.io.OutputStream

class PdfUsers(var List: List<User>) {
    fun generatePdf(outputStream: OutputStream): Boolean {
        val document = Document()

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream)
            document.open()
            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, BaseColor.BLACK)
            var title: Paragraph

            title = Paragraph("Active Users Detail", titleFont)

            title.alignment = Element.ALIGN_CENTER
            document.add(title)
            document.add(Paragraph("\n"))
            val table = PdfPTable(5)
            table.widthPercentage = 100f

            val headers = arrayOf(
                Paragraph("CNIC", titleFont),
                Paragraph("Name", titleFont),
                Paragraph("Address", titleFont),
                Paragraph("Phone", titleFont),
                Paragraph("Status", titleFont)
            )
            for (header in headers) {
                val cell = PdfPCell(Phrase(header))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(cell)
            }
            for (item in List) {
                table.addCell(item.cnic)
                table.addCell(item.firstName)
                table.addCell(item.address)
                table.addCell(item.phone)
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