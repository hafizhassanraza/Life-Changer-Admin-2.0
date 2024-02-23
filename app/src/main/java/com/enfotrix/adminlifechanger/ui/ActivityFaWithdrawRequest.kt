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
import com.enfotrix.adminlifechanger.Adapters.FaWithdrawViewPagerAdapter
import com.enfotrix.adminlifechanger.Adapters.WithdrawViewPagerAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.AgentWithdrawModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityFaWithdrawRequestBinding
import com.enfotrix.lifechanger.Adapters.AgentTransactionsAdapter
import com.enfotrix.lifechanger.Adapters.TransactionsAdapter
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.material.tabs.TabLayoutMediator

class ActivityFaWithdrawRequest : AppCompatActivity() ,AgentTransactionsAdapter.OnItemClickListener {

    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()
    var constant= Constants()







    private lateinit var binding: ActivityFaWithdrawRequestBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaWithdrawRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //supportActionBar?.title = "Withdraw Request"

        mContext=this@ActivityFaWithdrawRequest
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)



        setTitle("Agent Withdraw")


        binding.rvInvestmentRequests.layoutManager = LinearLayoutManager(mContext)

        getRequests()



    }
    private fun getRequests(){




        val pendingInvestment = sharedPrefManager.getAgentWithdrawList().filter {it.status.equals( constant.TRANSACTION_STATUS_PENDING ) }.sortedByDescending { it.lastWithdrawReqDate }
        binding.rvInvestmentRequests.adapter= AgentTransactionsAdapter(
            pendingInvestment,
            sharedPrefManager.getFAList(),
            this@ActivityFaWithdrawRequest)

    }

    override fun onAgentItemClick(agentWithdrawModel: AgentWithdrawModel, modelFA: ModelFA) {


        //Toast.makeText(mContext, agentWithdrawModel.fa_ID+"      " +modelFA.id, Toast.LENGTH_SHORT).show()
        startActivity(
            Intent(mContext, ActivityAgentWithdrawReqDetails ::class.java)
                .putExtra("transactionModel",agentWithdrawModel.toString())
                .putExtra("User",modelFA.toString())
        )

    }


}