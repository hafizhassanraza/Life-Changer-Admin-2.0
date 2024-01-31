package com.enfotrix.adminlifechanger.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.enfotrix.adminlifechanger.API.FCM
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.AgentTransactionModel
import com.enfotrix.adminlifechanger.Models.AgentWithdrawModel
import com.enfotrix.adminlifechanger.Models.AgentTransactionviewModel
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Models.NotificationModel
import com.enfotrix.adminlifechanger.Models.NotificationViewModel
import com.enfotrix.adminlifechanger.databinding.ActivityAgentWithdrawReqDetailsBinding
import com.enfotrix.adminlifechanger.databinding.ActivityInvestmentReqDetailsBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityAgentWithdrawReqDetails : AppCompatActivity() {


    private val notificationViewModel: NotificationViewModel by viewModels()
    private val db = Firebase.firestore
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    var constant= Constants()


    private lateinit var transactionModel: TransactionModel
    private lateinit var investmentModel: InvestmentModel




    private val agentTransactionviewModel: AgentTransactionviewModel by viewModels()



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
    private lateinit var notificationModel: NotificationModel
    private lateinit var agentwithdrawModel: AgentWithdrawModel


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

            approvedWithdraw()

        }



        binding.btnReject.setOnClickListener {

            rejectWithdraw()

        }
        setData()

    }

    private fun rejectWithdraw() {
        utils.startLoadingAnimation()
        val transactionAmount = agentwithdrawModel?.withdrawBalance?.toInt() ?: 0
        agentwithdrawModel.status= constants.TRANSACTION_STATUS_REJECT
        db.collection(constants.WITHDRAW_COLLECTION).document(agentwithdrawModel.id).set(agentwithdrawModel).addOnCompleteListener {task->
            if(task.isSuccessful){
                utils.endLoadingAnimation()
                Toast.makeText(mContext, "Rejected", Toast.LENGTH_SHORT).show()
                val notificationData = "Dear ${modelFA.firstName}, your request for withdrawal of $transactionAmount PKR has been Rejected"
                addNotification(NotificationModel("",  modelFA.id, getCurrentDateInFormat(), "Withdrawal Rejected ", notificationData))
            }
            utils.endLoadingAnimation()


        }
    }


    private fun getData() {


/*

        agentWithdrawModel = AgentWithdrawModel.fromString(intent.getStringExtra("agentwithdrawmodel").toString())!!
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
*/


    }


    private fun approvedWithdraw() {


        val transactionAmount = agentwithdrawModel?.withdrawBalance?.toInt() ?: 0
        val agentEarning = modelFA?.profit?.toInt() ?: 0

        if(transactionAmount>agentEarning) Toast.makeText(mContext, "insufficient Balance", Toast.LENGTH_SHORT).show()
        else {

            agentwithdrawModel.status= constants.TRANSACTION_STATUS_APPROVED
            agentwithdrawModel.withdrawApprovedDate= Timestamp.now()
            agentwithdrawModel.lastWithdrawBalance = modelFA.profit // set all previous balance
            modelFA.profit=(agentEarning-transactionAmount).toString()
            utils.startLoadingAnimation()
            db.collection(constants.WITHDRAW_COLLECTION).document(agentwithdrawModel.id).set(agentwithdrawModel)
                .addOnSuccessListener {
                    db.collection(constants.FA_COLLECTION).document(modelFA.id).set(modelFA)
                        .addOnSuccessListener {


                            val name = SpannableString(modelFA?.firstName)
                            name.setSpan(StyleSpan(Typeface.BOLD), 0, name.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

                            val withdrawAmount = SpannableString(agentwithdrawModel?.withdrawBalance)
                            withdrawAmount.setSpan(StyleSpan(Typeface.BOLD), 0, withdrawAmount.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

                            val notificationData = "Dear $name, your request of  $withdrawAmount PKR has been approved"
                             addNotification(NotificationModel("",  modelFA.id, getCurrentDateInFormat(), "Withdrawal Approved ", notificationData))





                            utils.endLoadingAnimation()
                            Toast.makeText(mContext, "Withdraw Approved", Toast.LENGTH_SHORT).show()

                        }
                }



        }



        /*if (agentWithdrawModel!!.withdrawBalance.toInt() > agentTransactionModel.newBalance.toInt()) {
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


        }*/
    }

    private fun addNotification(notificationModel: NotificationModel) {
        lifecycleScope.launch {
            try {
                notificationViewModel.setNotification(notificationModel).await()

                FCM().sendFCMNotification(
                    modelFA.devicetoken,
                    notificationModel.notiTitle,
                    notificationModel.notiData
                )

                Toast.makeText(mContext, "Notification sent!!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(mContext, "Failed to send notification", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }


    fun setData() {

        binding.tvHeader22.text = "Withdraw"
        agentwithdrawModel = AgentWithdrawModel.fromString(intent.getStringExtra("transactionModel").toString())!!
        modelFA = ModelFA.fromString(intent.getStringExtra("User").toString())!!

        binding.tvInvestorName.text = "${modelFA?.firstName} ${modelFA?.lastName}"
        binding.tvInvestorCnic.text = "${modelFA?.cnic}"
        binding.investmentDate.text = SimpleDateFormat("hh:mm a dd/MM/yy", Locale.getDefault()).format(agentwithdrawModel?.lastWithdrawReqDate!!.toDate()).toString()
        binding.tvInvestment.text = "${agentwithdrawModel?.withdrawBalance}"
        Glide.with(this)
            .load(modelFA.photo)
            .into(binding.img);

        val account = sharedPrefManager.getAccountList().find { it.docID == agentwithdrawModel.reciverAccountID }
        if (account != null && agentwithdrawModel.reciverAccountID?.isNotEmpty() == true) {

            binding.tvAccountTittle.text=account.account_tittle
            binding.tvBankName.text=account.bank_name
            binding.tvAccountNumber.text=account.account_number
        } else {
            Toast.makeText(mContext, "No account found", Toast.LENGTH_SHORT).show()
        }

    }
    fun getCurrentDateInFormat(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        return dateFormat.format(currentDate)
    }
//    private fun addNotification(notificationModel: NotificationModel) {
//        Toast.makeText(mContext, "debug1", Toast.LENGTH_SHORT).show()
//        utils.startLoadingAnimation()
//        lifecycleScope.launch {
//            try {
//                utils.endLoadingAnimation()
//
//            } catch (e: Exception) {
//                utils.endLoadingAnimation()
//                Toast.makeText(mContext, "Failed to send notification", Toast.LENGTH_SHORT).show()
//                e.printStackTrace()
//            }
//        }
//    }

}