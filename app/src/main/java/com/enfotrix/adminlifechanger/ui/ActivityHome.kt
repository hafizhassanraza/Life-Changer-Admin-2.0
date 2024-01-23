package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.enfotrix.adminlifechanger.ActivityAnnouncement
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.ModelEarning
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.databinding.ActivityHomeBinding
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ActivityHome : AppCompatActivity() {
    private val db = Firebase.firestore

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding : ActivityHomeBinding

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog
    private lateinit var announcement : String





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext=this@ActivityHome
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        binding.btnInvestmentReq.setOnClickListener { startActivity(Intent(mContext,ActivityInvestmentRequest::class.java)) }
        binding.btnWithdrawReq.setOnClickListener { startActivity(Intent(mContext,ActivityWithdrawRequest::class.java)) }
        binding.btnNewInvestors.setOnClickListener { startActivity(Intent(mContext,ActivityNewInvestorReq::class.java)) }


        binding.layInActiveInv.setOnClickListener { startActivity(Intent(mContext,ActivityInActiveInvestment::class.java)) }
        binding.layInvestorsManager.setOnClickListener { startActivity(Intent(mContext, ActivityInvestors::class.java)) }
        binding.layProfitManager.setOnClickListener { startActivity(Intent(mContext,ActivityAddProfit::class.java)) }
        binding.layAgentManager.setOnClickListener { startActivity(Intent(mContext,ActivityFA::class.java)) }
        binding.layAnnouncementManager.setOnClickListener { startActivity(Intent(mContext, ActivityAnnouncement::class.java)) }
        binding.layAdminAccount.setOnClickListener { startActivity(Intent(mContext, ActivityAccounts::class.java)) }
        binding.layInvStatment.setOnClickListener { startActivity(Intent(mContext, ActivityStatment::class.java)) }

        getData()


    }



    private fun getData() {


        db.collection("Admin Announcement")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    val latestAnnouncement = querySnapshot.documents[0].getString("announcement")
                    binding.tvAnnouncement.text = latestAnnouncement
                }
            }

        val collections = listOf(
            constants.ACCOUNTS_COLLECTION,
            constants.INVESTOR_COLLECTION,
            constants.INVESTMENT_COLLECTION,
            constants.TRANSACTION_REQ_COLLECTION,
            constants.FA_COLLECTION,
            constants.NOMINEE_COLLECTION,
            constants.ANNOUNCEMENT_COLLECTION,
            constants.AGENT_EARNING_COLLECTION,
        )
        utils.startLoadingAnimation()
        collections.forEach { collection ->
            db.collection(collection)
                .addSnapshotListener { snapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    snapshot?.let { task ->
                        when (collection) {
                            constants.ACCOUNTS_COLLECTION -> sharedPrefManager.putAccountList(task.documents.mapNotNull { document ->
                                document.toObject(ModelBankAccount::class.java)?.apply { docID = document.id }
                            })
                            constants.INVESTOR_COLLECTION -> sharedPrefManager.putUserList(task.documents.mapNotNull { document ->
                                document.toObject(User::class.java)?.apply { id = document.id }
                            })
                            constants.INVESTMENT_COLLECTION -> sharedPrefManager.putInvestmentList(
                                task.documents.mapNotNull { document ->
                                    document.toObject(InvestmentModel::class.java)
                                }
                            )
                            constants.TRANSACTION_REQ_COLLECTION -> sharedPrefManager.putTransactionList(
                                task.documents.mapNotNull { document ->
                                    document.toObject(TransactionModel::class.java)?.apply { id = document.id }
                                }
                            )
                            constants.FA_COLLECTION -> sharedPrefManager.putFAList(task.documents.mapNotNull { document ->
                                document.toObject(ModelFA::class.java)?.apply { id = document.id }
                            })
                            constants.NOMINEE_COLLECTION -> sharedPrefManager.putNomineeList(task.documents.mapNotNull { document ->
                                document.toObject(ModelNominee::class.java)?.apply { docID = document.id }
                            })
                            constants.AGENT_EARNING_COLLECTION -> sharedPrefManager.putAgentEarningList(task.documents.mapNotNull { document ->
                                document.toObject(ModelEarning::class.java)?.apply { docID = document.id }
                            })
                            constants.ANNOUNCEMENT_COLLECTION -> task.documents.mapNotNull { document -> announcement= document.getString("announcement").toString() }

                        }
                        // Call endLoading after each snapshot listener completes
                        utils.endLoadingAnimation()
                        setData()

                    }
                }
        }








    }
    private fun setData() {


        //utils.startLoadingAnimation()
        //Thread.sleep(50)

        var listInvestmentModel= sharedPrefManager.getInvestmentList()
        var listTransaction= sharedPrefManager.getTransactionList()
        var newInvestorsCounter= sharedPrefManager.getUsersList().count{it.status.equals(constants.INVESTOR_STATUS_PENDING)}
        val pendingInvestmentCounter = listTransaction.count { it.type == constants.TRANSACTION_TYPE_INVESTMENT && it.status == constants.TRANSACTION_STATUS_PENDING }?.toInt() ?: 0
        val pendingWithdrawCounter = listTransaction.count { it.type == constants.TRANSACTION_TYPE_WITHDRAW && it.status == constants.TRANSACTION_STATUS_PENDING }?.toInt() ?: 0
        val InActiveInvestCounter = sharedPrefManager.getInvestmentList().filter { investment ->
            val inActiveInvestment = investment.lastInvestment.takeIf { !it.isNullOrEmpty() } ?: "0"
            val inActiveInvestment_ = inActiveInvestment.toIntOrNull() ?: 0
            inActiveInvestment_ > 0 }.count()

        var ActiveInvestment= listInvestmentModel.sumOf { it.investmentBalance.takeIf { it.isNotBlank() }?.toInt() ?: 0 }.toInt()
        var Profit=listInvestmentModel.sumOf { it.lastProfit.takeIf { it.isNotBlank() }?.toInt() ?: 0 }.toInt()
        var InActiveInvestment=listInvestmentModel.sumOf { it.lastInvestment.takeIf { it.isNotBlank() }?.toInt() ?: 0 }.toInt()
        var totalSum= ActiveInvestment+InActiveInvestment+Profit

        var AgentCounter = sharedPrefManager.getFAList().count()
        var AgentWithdrawReqCounter = sharedPrefManager.getAgentEarningList().count()
        var ActiveInvestorCounter = sharedPrefManager.getUsersList().count{it.status.equals(constants.INVESTOR_STATUS_ACTIVE)}


        binding.tvActiveInvestment.text= ActiveInvestment.toString()
        binding.tvProfit.text= Profit.toString()
        binding.tvInActiveInvestment.text= InActiveInvestment.toString()
        binding.tvExpectedSum.text= totalSum.toString()

        binding.tvActiveInvCounter.text= pendingInvestmentCounter.toString()
        binding.tvWithdrawCounter.text= pendingWithdrawCounter.toString()
        binding.tvNewInvestorCounter.text= newInvestorsCounter.toString()

        binding.tvInActiveCounter.text= InActiveInvestCounter.toString()
        binding.tvInvestorCounter.text= ActiveInvestorCounter.toString()
        binding.tvAgentCounter.text= AgentCounter.toString()
        binding.tvAgentWithdrawReqCounter.text= AgentCounter.toString()





        //utils.endLoadingAnimation()




    }
}

