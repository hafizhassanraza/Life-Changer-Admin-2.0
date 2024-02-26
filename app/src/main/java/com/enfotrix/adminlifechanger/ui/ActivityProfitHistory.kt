package com.enfotrix.adminlifechanger.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.AdapterProfitHistory
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.ProfitModel
import com.enfotrix.adminlifechanger.Pdf.PdfProfitHistory
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityProfitHistoryBinding
import com.enfotrix.adminlifechanger.databinding.DialogDatepickerBinding
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils

class ActivityProfitHistory : AppCompatActivity() {
    private lateinit var binding: ActivityProfitHistoryBinding

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var listProfitHistory: List<ProfitModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfitHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mContext = this@ActivityProfitHistory
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvStatment.layoutManager = LinearLayoutManager(mContext)
        setData()
        listProfitHistory = sharedPrefManager.getProfitHistory().sortedByDescending { it.createdAt }
        binding.pdfEstatment.setOnClickListener {
            dialogWithdrawDetails()
        }
    }

    private fun setData() {

        binding.rvStatment.adapter = AdapterProfitHistory(
            sharedPrefManager.getProfitHistory().sortedByDescending { it.createdAt }, mContext)

    }

    private fun dialogWithdrawDetails() {
        val dialogBinding = DialogDatepickerBinding.inflate(LayoutInflater.from(mContext))
        val dialogDatepicker = Dialog(mContext)
        dialogDatepicker.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogDatepicker.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogDatepicker.setContentView(dialogBinding.root)
        val yearPicker: NumberPicker = dialogBinding.yearPicker
        val monthPicker: NumberPicker = dialogBinding.monthPicker
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        yearPicker.minValue = 2024
        yearPicker.maxValue = 2050
        yearPicker.value = 2024
        val monthNames = resources.getStringArray(R.array.months)
        val monthValues = (1..12).toList().toIntArray()
        monthPicker.minValue = 0
        monthPicker.maxValue = monthNames.size - 1
        monthPicker.displayedValues = monthNames
        monthPicker.setFormatter { index -> monthNames[index] }
        monthPicker.value = java.util.Calendar.JULY - 1
        dialogBinding.btnDownload.setOnClickListener {
            val selectedYear = yearPicker.value
            val selectedMonth = monthValues[monthPicker.value]
            filterEstatmentList(selectedYear, selectedMonth)
            dialogDatepicker.dismiss()
        }
        dialogDatepicker.show()
    }

    private fun filterEstatmentList(year: Int, month: Int) {
        listProfitHistory
            .filter { earning ->
                val taxDate = earning.createdAt
                if (taxDate != null) {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.timeInMillis = taxDate.seconds * 1000
                    return@filter calendar.get(java.util.Calendar.YEAR) == year && calendar.get(java.util.Calendar.MONTH) + 1 == month
                }
                false
            }

        if (listProfitHistory!!.isEmpty()) {
            Toast.makeText(mContext, "No data found", Toast.LENGTH_SHORT).show()
        } else {
            generatePDF()
        }
    }

    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Statement.pdf")
        }
        startActivityForResult(intent, 123)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val outputStream = mContext.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val success = PdfProfitHistory(listProfitHistory!!).generatePdf(
                        outputStream
                    )
                    outputStream.close()
                    if (success == true) {
                        Toast.makeText(mContext, "Saved successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(mContext, "Failed to save", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }
}

