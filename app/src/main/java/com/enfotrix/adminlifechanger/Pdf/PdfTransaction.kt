package com.enfotrix.adminlifechanger.Pdf

import User
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

class PdfTransaction(var List: List<TransactionModel>, var user: User) {
    fun generatePdf(outputStream: OutputStream): Boolean {
        val document = Document()

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream)
            document.open()
            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, BaseColor.BLACK)
            var title: Paragraph

            var type:String = ""
            List.forEach { TransactionModel->
                type=TransactionModel.type
            }

            title = Paragraph("Approved $type Details", titleFont)

            title.alignment = Element.ALIGN_CENTER
            document.add(title)
            document.add(Paragraph("\n"))

            // Add subheading with name and contact
            val subheadingFont = FontFactory.getFont(FontFactory.HELVETICA, 12f, BaseColor.BLACK)
            val subheading = Paragraph(
                "Name: ${user.firstName}\nContact: ${user.phone}",
                subheadingFont
            )
            subheading.alignment = Element.ALIGN_LEFT
            document.add(subheading)
            document.add(Paragraph("\n"))

            val table = PdfPTable(5)
            table.widthPercentage = 100f
            val headers = arrayOf(
                Paragraph("Request Date", titleFont),
                Paragraph("Old Balance", titleFont),
                Paragraph("$type", titleFont),
                Paragraph("New Balance", titleFont),
                Paragraph("Clear Date", titleFont)
            )
            for (header in headers) {
                val cell = PdfPCell(Phrase(header))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(cell)
            }
            for (item in List) {
                val rdate = SimpleDateFormat(
                    "dd/MM/yy",
                    Locale.getDefault()
                ).format(item.createdAt.toDate())
                val cdate = item.transactionAt?.toDate()
                    ?.let { SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it) }
                table.addCell(rdate)
                table.addCell(item.previousBalance)
                table.addCell(item.amount)
                table.addCell(item.newBalance)
                table.addCell(cdate)
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