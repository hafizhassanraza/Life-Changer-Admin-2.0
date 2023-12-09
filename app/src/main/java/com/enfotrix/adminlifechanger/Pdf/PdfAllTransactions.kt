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

class PdfAllTransactions(var transactionsList: List<TransactionModel>, var userList: List<User>) {
    fun generatePdf(outputStream: OutputStream): Boolean {
        val document = Document()

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream)
            document.open()
            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, BaseColor.BLACK)
            var title: Paragraph

            var type: String = ""
            transactionsList.forEach { transaction ->
                type = transaction.type
            }

            title = Paragraph("Approved $type Details", titleFont)
            title.alignment = Element.ALIGN_CENTER
            document.add(title)
            document.add(Paragraph("\n"))

            val table = PdfPTable(6) // Increased to 6 for the additional column for the user's name
            table.widthPercentage = 100f
            val headers = arrayOf(
                Paragraph("Request Date", titleFont),
                Paragraph("User Name", titleFont),
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
            for (transaction in transactionsList) {
                val user = userList.find { it.id == transaction.investorID }
                val userName = user?.firstName ?: ""

                val rdate = SimpleDateFormat(
                    "dd/MM/yy",
                    Locale.getDefault()
                ).format(transaction.createdAt.toDate())
                val cdate = transaction.transactionAt?.toDate()
                    ?.let { SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it) }

                table.addCell(rdate)
                table.addCell(userName)
                table.addCell(transaction.previousBalance)
                table.addCell(transaction.amount)
                table.addCell(transaction.newBalance)
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
