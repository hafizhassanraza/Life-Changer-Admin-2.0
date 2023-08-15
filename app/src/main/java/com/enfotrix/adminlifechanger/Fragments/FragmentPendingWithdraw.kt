package com.enfotrix.adminlifechanger.Fragments

import User
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
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.FragmentPendingInvestmentsBinding
import com.enfotrix.adminlifechanger.databinding.FragmentPendingWithdrawBinding
import com.enfotrix.adminlifechanger.ui.ActivityInvestmentReqDetails
import com.enfotrix.lifechanger.Adapters.TransactionsAdapter
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import kotlinx.coroutines.launch


class FragmentPendingWithdraw : Fragment(),  TransactionsAdapter.OnItemClickListener {


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


    private var _binding: FragmentPendingWithdrawBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPendingWithdrawBinding.inflate(inflater, container, false)
        val root: View = binding.root


        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        binding.rvInvestmentRequests.layoutManager = LinearLayoutManager(mContext)

        getRequests()

        return root
    }


    fun getRequests(){
        utils.startLoadingAnimation()
        lifecycleScope.launch{
            investmentViewModel.getPendingWithdrawsReq()
                .addOnCompleteListener{task ->
                    if (task.isSuccessful) {
                        utils.endLoadingAnimation()
                        val list = ArrayList<TransactionModel>()
                        if(task.result.size()>0){
                            for (document in task.result) {
                                var transactionModel= document.toObject(TransactionModel::class.java)
                                transactionModel.id=document.id
                                list.add(transactionModel)
                            }
                            binding.rvInvestmentRequests.adapter= TransactionsAdapter(
                                constant.FROM_PENDING_WITHDRAW_REQ,
                                list.sortedByDescending { it.createdAt },
                                sharedPrefManager.getUsersList(),
                                sharedPrefManager.getFAList(),
                                this@FragmentPendingWithdraw)
                            getAccount()
                        }
                    }
                    else {
                        utils.endLoadingAnimation()
                        Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                    }

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
                    if (task.isSuccessful) {
                        val list = ArrayList<ModelBankAccount>()
                        if(task.result.size()>0){
                            for (document in task.result)list.add(document.toObject(ModelBankAccount::class.java).apply { docID = document.id })
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


        //Toast.makeText(mContext, "debug1", Toast.LENGTH_SHORT).show()

        sharedPrefManager.getFAList().find { it.id.equals(user.fa_id)}?.let {

            /*Toast.makeText(mContext, transactionModel.receiverAccountID, Toast.LENGTH_SHORT).show()
            Toast.makeText(mContext, .receiverAccountID, Toast.LENGTH_SHORT).show()*/

            startActivity(
                Intent(mContext, ActivityInvestmentReqDetails ::class.java)
                    .putExtra("transactionModel",transactionModel.toString())
                    .putExtra("User",user.toString())
                    .putExtra("from",constant.FROM_PENDING_WITHDRAW_REQ)
                    .putExtra("FA",it.toString())
            )

        }
    }

    override fun onDeleteClick(transactionModel: TransactionModel) {
    }


}