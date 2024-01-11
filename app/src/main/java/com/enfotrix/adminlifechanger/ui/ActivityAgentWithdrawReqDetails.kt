package com.enfotrix.adminlifechanger.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.AgentTransactionModel
import com.enfotrix.adminlifechanger.Models.AgentWithdrawModel
import com.enfotrix.adminlifechanger.Models.AgentTransactionviewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.databinding.ActivityAgentWithdrawReqDetailsBinding
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class ActivityAgentWithdrawReqDetails : AppCompatActivity() {


    private val db = Firebase.firestore


    private val agentTransactionviewModel: AgentTransactionviewModel by viewModels()

    var constant = Constants()


    private lateinit var binding: ActivityAgentWithdrawReqDetailsBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog


    private lateinit var agentTransactionModel: AgentTransactionModel
    private lateinit var model2: AgentTransactionModel
    private var agentWithdrawModel: AgentWithdrawModel? = null
    private lateinit var modelFA: ModelFA


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgentWithdrawReqDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Investment Details"

        mContext = this@ActivityAgentWithdrawReqDetails
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        agentTransactionModel = AgentTransactionModel()
        model2 = AgentTransactionModel()





        binding.btnAccept.setOnClickListener {

            if (intent.getStringExtra("from").toString()
                    .equals(constant.FROM_PENDING_WITHDRAW_REQ)
            ) approvedWithdraw()
            else
                Toast.makeText(
                    mContext,
                    "Already Approved,Might Be error from Developer Side",
                    Toast.LENGTH_SHORT
                ).show()

        }
        setData()
        getData()

    }


    private fun getData() {

        agentWithdrawModel =
            AgentWithdrawModel.fromString(intent.getStringExtra("agentwithdrawmodel").toString())!!
        modelFA = ModelFA.fromString(intent.getStringExtra("FA").toString())!!






        lifecycleScope.launch {
            agentTransactionviewModel.getAgentTransaction()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<AgentTransactionModel>()

                        for (document in task.result) {
                            val transactionModel =
                                document.toObject(AgentTransactionModel::class.java)
                            if (transactionModel.fa_id == modelFA.id && transactionModel.type == constants.PROFIT_TYPE) {
                                list.add(transactionModel)
                            }
                        }

                        val sortedList = list.sortedByDescending { it.transactionAt }

                        if (sortedList.isNotEmpty()) {
                            agentTransactionModel = sortedList[0]
                            Toast.makeText(mContext, "List is not empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        mContext,
                        constant.SOMETHING_WENT_WRONG_MESSAGE,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }


    }

    @SuppressLint("SuspiciousIndentation")

    private fun approvedWithdraw() {
        if (agentWithdrawModel!!.withdrawBalance.toInt() > agentTransactionModel.newBalance.toInt()) {
            Toast.makeText(mContext, "Insufficient Balance", Toast.LENGTH_SHORT).show()
        } else {

            agentWithdrawModel!!.lastWithdrawBalance = agentTransactionModel.newBalance
            agentWithdrawModel!!.totalWithdrawBalance =
                (agentTransactionModel.newBalance.toInt() - agentWithdrawModel!!.withdrawBalance.toInt()).toString()
            agentWithdrawModel?.withdrawApprovedDate = Timestamp.now()
            agentWithdrawModel?.status = constants.TRANSACTION_STATUS_APPROVED


            agentTransactionModel.previousBalance = agentTransactionModel.newBalance
            agentTransactionModel.newBalance =
                (agentTransactionModel.newBalance.toInt() - agentWithdrawModel!!.withdrawBalance.toInt()).toString()
            agentTransactionModel.status = ""
            agentTransactionModel.type = constants.PROFIT_TYPE
            agentTransactionModel.salary = agentTransactionModel.salary
            agentTransactionModel.transactionAt = Timestamp.now()



            lifecycleScope.launch {

                agentTransactionviewModel.setAgentWithdraw(agentWithdrawModel!!)
                    .addOnCompleteListener { task ->

                        Toast.makeText(
                            mContext,
                            "WithDrawApproved for witjdraw model",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                lifecycleScope.launch {

                    agentTransactionviewModel.setAgentTransaction(agentTransactionModel)
                        .addOnCompleteListener { task ->

                            Toast.makeText(mContext, "Transaction Approved", Toast.LENGTH_SHORT)
                                .show()

                        }


                }
                startActivity(
                    Intent(
                        this@ActivityAgentWithdrawReqDetails,
                        ActivityHome::class.java
                    )
                )
            }


        }
    }

    fun setData() {
        if (intent.getStringExtra("from").toString().equals(constant.FROM_PENDING_WITHDRAW_REQ)) {
            binding.tvHeader22.text = "Withdraw"
            supportActionBar?.title = "Withdraw Details"
        }
        var agentwithdrawModel =
            AgentWithdrawModel.fromString(intent.getStringExtra("agentwithdrawmodel").toString())
        var modelFA = ModelFA.fromString(intent.getStringExtra("FA").toString())
        binding.tvInvestorName.text = "${modelFA?.firstName} ${modelFA?.lastName}"
        binding.tvInvestorCnic.text = "${modelFA?.cnic}"
        binding.investmentDate.text = SimpleDateFormat(
            "hh:mm a dd/MM/yy",
            Locale.getDefault()
        ).format(agentwithdrawModel?.lastWithdrawReqDate!!.toDate()).toString()
        binding.tvInvestment.text = "${agentwithdrawModel?.withdrawBalance}"

        /*  binding.tvInvestorBankName.text="${senderBankAccount?.bank_name}"
          binding.tvInvestorAccountNumber.text="${senderBankAccount?.account_number}"
          binding.tvInvestorAccountTittle.text="${senderBankAccount?.account_tittle}"

          binding.tvBankName.text="${receiverBankAccount?.bank_name}"
          binding.tvAccountNumber.text="${receiverBankAccount?.account_number}"
          binding.tvAccountTittle.text="${receiverBankAccount?.account_tittle}"*/

    }


}