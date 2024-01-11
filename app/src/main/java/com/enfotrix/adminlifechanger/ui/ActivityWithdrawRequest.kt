package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.InvestmentViewPagerAdapter
import com.enfotrix.adminlifechanger.Adapters.WithdrawViewPagerAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityInvestmentRequestBinding
import com.enfotrix.adminlifechanger.databinding.ActivityWithdrawRequestBinding
import com.enfotrix.lifechanger.Adapters.TransactionsAdapter
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.material.tabs.TabLayoutMediator

class ActivityWithdrawRequest : AppCompatActivity() ,  TransactionsAdapter.OnItemClickListener{


    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()
    var constant= Constants()



    private lateinit var binding: ActivityWithdrawRequestBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Withdraw Request"

        mContext=this@ActivityWithdrawRequest
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)



        setTitle("WithDraw")

        binding.rvInvestmentRequests.layoutManager = LinearLayoutManager(mContext)

        getRequests()





    }

    private fun getRequests(){


        val pendingInvestment = sharedPrefManager.getTransactionList().filter { it.type == constant.TRANSACTION_TYPE_WITHDRAW && it.status == constant.TRANSACTION_STATUS_PENDING }.sortedByDescending { it.createdAt }

        binding.rvInvestmentRequests.adapter= TransactionsAdapter(
            constant.FROM_PENDING_WITHDRAW_REQ,
            pendingInvestment,
            sharedPrefManager.getUsersList(),
            sharedPrefManager.getFAList(),
            this@ActivityWithdrawRequest)
    }


    override fun onItemClick(transactionModel: TransactionModel, user: User) {


        startActivity(
            Intent(mContext, ActivityInvestmentReqDetails ::class.java)
                .putExtra("transactionModel",transactionModel.toString())
                .putExtra("User",user.toString())
                .putExtra("from",constant.FROM_PENDING_WITHDRAW_REQ)
        )






    }

    override fun onDeleteClick(transactionModel: TransactionModel) {
    }

}