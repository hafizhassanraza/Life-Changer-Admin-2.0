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
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.Pdf.PdfAllTransactions
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.FragmentApprovedWithdrawBinding
import com.enfotrix.adminlifechanger.databinding.FragmentPendingWithdrawBinding
import com.enfotrix.adminlifechanger.ui.ActivityInvestmentReqDetails
import com.enfotrix.lifechanger.Adapters.TransactionsAdapter
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import kotlinx.coroutines.launch


class FragmentApprovedWithdraw : Fragment() , TransactionsAdapter.OnItemClickListener{

    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()
    var constant= Constants()



    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog


    private var _binding: FragmentApprovedWithdrawBinding? = null
    private val CREATE_PDF_REQUEST_CODE = 123
    val list = ArrayList<TransactionModel>()


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        _binding = FragmentApprovedWithdrawBinding.inflate(inflater, container, false)
        val root: View = binding.root


        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        binding.rvInvestmentRequests.layoutManager = LinearLayoutManager(mContext)

        binding.pdfWithDraw.setOnClickListener { generatePDF() }

        getRequests()

        return root
    }

    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "All WithDraw.pdf")
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
                        PdfAllTransactions(list.sortedByDescending { it.createdAt },
                            sharedPrefManager.getUsersList()).generatePdf(
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

    fun getRequests(){
        utils.startLoadingAnimation()
        lifecycleScope.launch{
            investmentViewModel.getApprovedWithdrawsReq()
                .addOnCompleteListener{task ->
                    if (task.isSuccessful) {

                        utils.endLoadingAnimation()
                        if(task.result.size()>0){
                            for (document in task.result) {
                                var transactionModel= document.toObject(TransactionModel::class.java)
                                transactionModel.id=document.id
                                list.add(transactionModel)
                            }
                            binding.rvInvestmentRequests.adapter= TransactionsAdapter(
                                constant.FROM_APPROVED_WITHDRAW_REQ,
                                list.sortedByDescending { it.createdAt },
                                sharedPrefManager.getUsersList(),
                                sharedPrefManager.getFAList(),
                                this@FragmentApprovedWithdraw)
                            //getAccount()
                        }
                    }
                    else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener{
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                }


        }
    }

    fun getAccount(){
        lifecycleScope.launch{
            userViewModel.getAccounts()
                .addOnCompleteListener{task ->
                    utils.endLoadingAnimation()

                    Toast.makeText(mContext, "d2", Toast.LENGTH_SHORT).show()
                    if (task.isSuccessful) {
                        val list = ArrayList<ModelBankAccount>()
                        if(task.result.size()>0){
                            for (document in task.result)list.add( document.toObject(
                                ModelBankAccount::class.java).apply { docID = document.id })
                            sharedPrefManager.putAccountList(list)
                            //getNominees()


                        }
                    }
                    else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener{
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                }
        }
    }




    override fun onItemClick(transactionModel: TransactionModel, user: User) {








    }

    override fun onDeleteClick(transactionModel: TransactionModel) {
    }


}