package com.enfotrix.adminlifechanger.Fragments

import User
import android.app.Dialog
import android.content.Context
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
import com.enfotrix.adminlifechanger.databinding.FragmentAllRecordBinding
import com.enfotrix.adminlifechanger.databinding.FragmentInvestRecordBinding
import com.enfotrix.adminlifechanger.databinding.FragmentTaxRecordBinding
import com.enfotrix.lifechanger.Adapters.TransactionsAdapter
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import kotlinx.coroutines.launch

class FragmentAllRecord : Fragment() {

    private var _binding: FragmentAllRecordBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: User
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var dialog : Dialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllRecordBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        // Retrieve the User object from arguments
        user = requireArguments().getParcelable("user")!!

        binding.rvProfitTax.layoutManager = LinearLayoutManager(mContext)
        Toast.makeText(mContext, "Available Soon!!", Toast.LENGTH_SHORT).show()
       getRequests()
        return root
    }


    fun getRequests() {
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            investmentViewModel.getApprovedTaxReq()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        utils.endLoadingAnimation()

                        val list = ArrayList<TransactionModel>()
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
                                constants.FROM_TAX,
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
