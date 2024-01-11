package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.InvestmentViewPagerAdapter
import com.enfotrix.adminlifechanger.Adapters.InvestorViewPagerAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.databinding.ActivityInvestmentRequestBinding
import com.enfotrix.lifechanger.Adapters.TransactionsAdapter
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class ActivityInvestmentRequest : AppCompatActivity() ,  TransactionsAdapter.OnItemClickListener{
    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()
    var constant= Constants()







    private lateinit var binding: ActivityInvestmentRequestBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Investment Request"

        mContext=this@ActivityInvestmentRequest
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)



        setTitle("Investments")


        binding.rvInvestmentRequests.layoutManager = LinearLayoutManager(mContext)

        getRequests()





    }
    private fun getRequests(){


        val pendingInvestment = sharedPrefManager.getTransactionList().filter { it.type == constant.TRANSACTION_TYPE_INVESTMENT && it.status == constant.TRANSACTION_STATUS_PENDING }.sortedByDescending { it.createdAt }

        binding.rvInvestmentRequests.adapter=TransactionsAdapter(
            constant.FROM_PENDING_INVESTMENT_REQ,
            pendingInvestment,
            sharedPrefManager.getUsersList(),
            sharedPrefManager.getFAList(),
            this@ActivityInvestmentRequest)
    }


    override fun onItemClick(transactionModel: TransactionModel, user: User) {


        startActivity(
            Intent(mContext, ActivityInvestmentReqDetails ::class.java)
                .putExtra("transactionModel",transactionModel.toString())
                .putExtra("User",user.toString())
                .putExtra("from",constant.FROM_PENDING_INVESTMENT_REQ)
        )




    }

    override fun onDeleteClick(transactionModel: TransactionModel) {
    }


}