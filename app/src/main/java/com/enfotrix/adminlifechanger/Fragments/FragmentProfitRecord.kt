package com.enfotrix.adminlifechanger.Fragments

import User
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.InvestorTransactionsAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Pdf.PdfTransaction
import com.enfotrix.adminlifechanger.databinding.FragmentProfitRecordBinding
import com.enfotrix.adminlifechanger.databinding.FragmentWithdrawRecordBinding
import com.enfotrix.lifechanger.Adapters.TransactionsAdapter
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import kotlinx.coroutines.launch

class FragmentProfitRecord : Fragment() {

    private var _binding: FragmentProfitRecordBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: User
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var dialog : Dialog
    private val CREATE_PDF_REQUEST_CODE = 123
    val list = ArrayList<TransactionModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfitRecordBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        // Retrieve the User object from arguments
        user = requireArguments().getParcelable("user")!!

        binding.rvProfitTax.layoutManager = LinearLayoutManager(mContext)
        binding.pdfProfitRecord.setOnClickListener { generatePDF() }

        getRequests()
        return root
    }

    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "profit-record.pdf")
        }
        startActivityForResult(intent, CREATE_PDF_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val outputStream = requireContext().contentResolver.openOutputStream(uri)
                if (outputStream != null) {

                    val success =
                        PdfTransaction(list.sortedByDescending { it.createdAt },user).generatePdf(
                            outputStream
                        )
                    outputStream.close()
                    if (success) {
                        Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to save", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    fun getRequests() {
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            investmentViewModel.getApprovedProfitsReq()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        utils.endLoadingAnimation()

                        if (task.result.size() > 0) {


                            for (document in task.result) {
                                var transactionModel =
                                    document.toObject(TransactionModel::class.java)
                                transactionModel.id = document.id
                                if(transactionModel.investorID==user.id)
                                    list.add(transactionModel)
                            }

                            //for (document in task.result) list.add( document.toObject(TransactionModel::class.java))


                            binding.rvProfitTax .adapter = InvestorTransactionsAdapter(
                                constants.FROM_PROFIT,
                                list.sortedByDescending { it.createdAt }
                            )
                            //getAccount()
                        }
                    } else {
                        utils.endLoadingAnimation()

                        Toast.makeText(
                            mContext,
                            constants.SOMETHING_WENT_WRONG_MESSAGE,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
                .addOnFailureListener {
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()

                }


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
