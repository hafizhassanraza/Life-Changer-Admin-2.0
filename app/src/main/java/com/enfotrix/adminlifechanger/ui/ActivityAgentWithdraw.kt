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
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.FaWithdrawAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.AgentWithdrawModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Pdf.PdfWithdrawHistory
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityAgentWithdrawBinding
import com.enfotrix.adminlifechanger.databinding.DialogDatepickerBinding
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import java.util.Calendar

class ActivityAgentWithdraw : AppCompatActivity() {

    private lateinit var binding: ActivityAgentWithdrawBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManagar: SharedPrefManager
    private lateinit var modelFA: ModelFA
    private var Position=1
    private var filteredWithdrawList: List<AgentWithdrawModel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding=ActivityAgentWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext=this@ActivityAgentWithdraw
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManagar= SharedPrefManager(mContext)

        binding.imgBack.setOnClickListener{finish()}
        binding.pdfAllWithdrawHistory.setOnClickListener{dialogWithdrawDetails()}

        modelFA = ModelFA.fromString(intent.getStringExtra("Fa").toString())!!

        val spinner = binding.spWithdraws

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.withdraw_options, // Replace with your array of items
            R.layout.item_investment_selection_spiner // Use the custom layout
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        binding.rvWithdraws.layoutManager = LinearLayoutManager(mContext)

        binding.spWithdraws.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {

                when (position) {
                    0 -> {
                        Position=1
                        binding.rvWithdraws.adapter= FaWithdrawAdapter(constants.FROM_APPROVED_WITHDRAW_REQ,
                            sharedPrefManagar.getAgentWithdrawList().filter { it.fa_ID.equals(modelFA.id) && it.status == constants.TRANSACTION_STATUS_APPROVED })
                    }
                    1 -> {
                        Position=2
                        binding.rvWithdraws.adapter= FaWithdrawAdapter(constants.FROM_PENDING_WITHDRAW_REQ,
                        sharedPrefManagar.getAgentWithdrawList().filter { it.fa_ID.equals(modelFA.id) && it.status == constants.TRANSACTION_STATUS_PENDING })
                    }
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing if nothing is selected
            }
        }

    }


////////////


    private fun dialogWithdrawDetails() {
        val dialogBinding = DialogDatepickerBinding.inflate(LayoutInflater.from(mContext))
        val DialogDatepicker = Dialog(mContext)
        DialogDatepicker.requestWindowFeature(Window.FEATURE_NO_TITLE)
        DialogDatepicker.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        DialogDatepicker.setContentView(dialogBinding.root)
        val yearPicker: NumberPicker = dialogBinding.yearPicker
        val monthPicker: NumberPicker = dialogBinding.monthPicker

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = 2024
        yearPicker.maxValue = 2050
        yearPicker.value = 2024

        val monthNames = resources.getStringArray(R.array.months)
        val monthValues = (1..12).toList().toIntArray()
        monthPicker.minValue = 0
        monthPicker.maxValue = monthNames.size - 1
        monthPicker.displayedValues = monthNames
        monthPicker.setFormatter { index -> monthNames[index] }
        monthPicker.value = Calendar.JULY - 1

        dialogBinding.btnDownload.setOnClickListener {
            val selectedYear = yearPicker.value
            val selectedMonth = monthValues[monthPicker.value]
            filterwithdrawList(selectedYear, selectedMonth)
            DialogDatepicker.dismiss()
        }

        DialogDatepicker.show()
    }

    private fun filterwithdrawList(year: Int, month: Int) {
        if (Position == 1) {
            Toast.makeText(mContext, "Debug - Position 1", Toast.LENGTH_SHORT).show()
            filteredWithdrawList = sharedPrefManagar.getAgentWithdrawList()
                .filter { it.fa_ID == modelFA.id && it.status == constants.TRANSACTION_STATUS_APPROVED }
                .filter { earning ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = earning.withdrawApprovedDate!!.seconds * 1000
                    val earningYear = calendar.get(Calendar.YEAR)
                    val earningMonth = calendar.get(Calendar.MONTH) + 1
                    earningYear == year && earningMonth == month
                }

            if (filteredWithdrawList!!.isEmpty()) {
                Toast.makeText(mContext, "No data found", Toast.LENGTH_SHORT).show()
            } else {
                generatePDF()
            }
        } else if (Position == 2) {
            Toast.makeText(mContext, "Debug - Position 2", Toast.LENGTH_SHORT).show()
            filteredWithdrawList = sharedPrefManagar.getAgentWithdrawList()
                .filter { it.fa_ID == modelFA.id && it.status == constants.TRANSACTION_STATUS_PENDING }
                .filter { earning ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = earning.lastWithdrawReqDate.seconds * 1000
                    val earningYear = calendar.get(Calendar.YEAR)
                    val earningMonth = calendar.get(Calendar.MONTH) + 1
                     earningYear == year && earningMonth == month
                }

            if (filteredWithdrawList!!.isEmpty()) {
                Toast.makeText(mContext, "No data found", Toast.LENGTH_SHORT).show()
            } else {
                generatePDF()
            }
        }
    }


    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Withdraw History.pdf")
        }
        startActivityForResult(intent, 123)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val outputStream = mContext.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val success =
                        PdfWithdrawHistory(filteredWithdrawList!!).generatePdf(
                            outputStream
                        )
                    outputStream.close()
                    if (success) {
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