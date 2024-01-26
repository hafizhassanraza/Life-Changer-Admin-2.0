package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.NotificationModel
import com.enfotrix.adminlifechanger.Models.NotificationViewModel
import com.enfotrix.adminlifechanger.databinding.ActivityAddProfitBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date

class ActivityAddProfit : AppCompatActivity() {

    private val db = Firebase.firestore





    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    var constant= Constants()
    private val notificationViewModel: NotificationViewModel by viewModels()


    private lateinit var binding: ActivityAddProfitBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog
    private lateinit var transactionModel: TransactionModel
    private lateinit var user: User
    private  var listInvestmentModel= ArrayList<InvestmentModel>()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProfitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Investment Details"

        mContext=this@ActivityAddProfit
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        //41//1111


       /* binding.btnAccept.setOnClickListener{

            if(intent.getStringExtra("from").toString().equals(constant.FROM_PENDING_WITHDRAW_REQ)) approvedWithdraw()
            else approved()


        }*/



        getData()

        binding.btnAddProfit.setOnClickListener{

            val percentage = binding.etProfit.text.toString()

            addProfit(percentage.toDouble() / 100,percentage)
        }
        binding.btnConvertProfit.setOnClickListener{


            convertProfit()
        }
        /*binding.btnConvertInvestment.setOnClickListener{


            convertInvestment()
        }*/

    }


    private fun addProfit(percentage: Double, percentage_: String) {

        utils.startLoadingAnimation()

        val totalInvestments = listInvestmentModel.size

        for ((index, investmentModel) in listInvestmentModel.withIndex()) {
            ///for notification
            val User=sharedPrefManager.getUsersList().find { it.id.equals(investmentModel.investorID) }
            val notificationData = "Dear ${User?.firstName}, ${percentage_}% profit has been credited to your account"
            if (User != null) {
                addNotification(
                    NotificationModel(
                        "",
                        User.id,
                        getCurrentDateInFormat(),
                        "Profit Credited",
                        notificationData
                    )
                )
            }


            val previousBalance = investmentModel.investmentBalance
            val previousTotalBalance = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()

            var previousProfit = investmentModel.lastProfit

            if (!previousBalance.isNullOrEmpty()) {



                if(previousProfit.isNullOrEmpty()) previousProfit="0"

                val previousBalance_ = previousBalance.toInt()
                val previousProfit_ = previousProfit.toInt()
                val profit = (previousBalance_ * percentage).toInt()
                val newProfit = previousProfit_ + profit

                investmentModel.lastProfit = newProfit.toString()




                val newTotalBalance = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()


                val profitModel = TransactionModel(
                    investmentModel.investorID,
                    "Profit",
                    "Approved",
                    profit.toString(),  // Current (weekly) Profit
                    "",
                    previousTotalBalance.toString(), // Previous Total (Investment + profit + inactiveInvestment)
                    "",
                    "",
                    newTotalBalance.toString(),  //  New Total (Investment + profit + inactiveInvestment)
                    Timestamp.now(),
                    Timestamp.now()
                )



                lifecycleScope.launch {
                    val setInvestmentTask = investmentViewModel.setInvestment(investmentModel)
                    val addTransactionTask = db.collection(constants.TRANSACTION_REQ_COLLECTION).add(profitModel)

                    Tasks.whenAllComplete(setInvestmentTask, addTransactionTask)
                        .addOnCompleteListener {

                            if (index == totalInvestments - 1) {
                           utils.endLoadingAnimation()
                                Toast.makeText(mContext, "Profit Added Successfully!", Toast.LENGTH_SHORT).show()
                            }

                        }
                }
            }
        }
    }
    fun getTextFromInvestment(value: String?): String {
        return if (value.isNullOrEmpty()) "0" else value
    }
    private fun addNotification(notificationModel: NotificationModel) {
        lifecycleScope.launch {
            try {
                notificationViewModel.setNotification(notificationModel).await()
            } catch (e: Exception) {
                Toast.makeText(mContext, "Failed to send notification", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    fun getCurrentDateInFormat(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        return dateFormat.format(currentDate)
    }


    private fun convertProfit() {
        utils.startLoadingAnimation()

        val totalInvestments = listInvestmentModel.size

        for ((index, investmentModel) in listInvestmentModel.withIndex()) {






            val previousBalance = investmentModel.investmentBalance
            val previousProfit = investmentModel.lastProfit

            if (!previousProfit.isNullOrEmpty()) {


                var previousBalance_ = previousBalance.toInt()
                val previousProfit_ = previousProfit.toInt()

                var newBalance = previousBalance_+ previousProfit_

                investmentModel.lastProfit = "0"
                investmentModel.investmentBalance= (newBalance.toInt()).toString()



                lifecycleScope.launch {
                    val setInvestmentTask = investmentViewModel.setInvestment(investmentModel)

                    Tasks.whenAllComplete(setInvestmentTask)
                        .addOnCompleteListener {

                            if (index == totalInvestments - 1) {
                                utils.endLoadingAnimation()
                                Toast.makeText(mContext, "Profit Converted Successfully!", Toast.LENGTH_SHORT).show()
                            }

                        }
                }

            }
        }
    }
    private fun convertInvestment() {
        utils.startLoadingAnimation()

        val totalInvestments = listInvestmentModel.size

        for ((index, investmentModel) in listInvestmentModel.withIndex()) {

            var inActiveInvestment = "0"
            if (!investmentModel.lastInvestment.isNullOrEmpty()) inActiveInvestment = investmentModel.lastInvestment
            val activeInvestment = investmentModel.investmentBalance


            val inActiveInvestment_ = inActiveInvestment?.toInt() ?: 0
            val activeInvestment_ = activeInvestment?.toInt() ?: 0
            val newBalance = inActiveInvestment_ + activeInvestment_

            investmentModel.lastInvestment="0"
            investmentModel.investmentBalance = newBalance.toString()

            lifecycleScope.launch {
                val setInvestmentTask = investmentViewModel.setInvestment(investmentModel)

                setInvestmentTask.addOnCompleteListener {
                    if (index == totalInvestments - 1) {
                        utils.endLoadingAnimation()
                        Toast.makeText(mContext, "Investment Converted Successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

/*    private fun addProfit(percentage: Double) {


        utils.startLoadingAnimation()

        val totalInvestments = listInvestmentModel.size


        for ((index, investmentModel) in listInvestmentModel.withIndex()){


            var previousBalance = investmentModel.investmentBalance
            var previousProfit = investmentModel.lastProfit

            if (previousBalance != null && previousBalance != "") {
                var profit: Double=0.0

                var previousBalance_ = previousBalance.toInt()
                var previousProfit_ = previousProfit.toInt()
                profit = previousBalance_ * percentage

                //var newBalance = previousBalance_ + profit.toInt()
                var newProfit = previousProfit_ + profit.toInt()



                investmentModel.lastProfit = (newProfit.toInt()).toString()


                var profit_=profit.toInt()

                var transactionModel=TransactionModel(
                    investmentModel.investorID,
                    "Profit",
                    "Approved",
                    profit_.toString(), // current (weekly) Profit
                    "",
                    previousProfit_.toString(), //  previous profit
                    "",
                    "",
                    (newProfit.toInt()).toString(),// new balance -> new profit
                    Timestamp.now(),
                    Timestamp.now()
                )

                lifecycleScope.launch{
                    investmentViewModel.setInvestment(investmentModel)
                        .addOnCompleteListener{task->


                            db.collection(constants.TRANSACTION_REQ_COLLECTION).add(transactionModel)
                                .addOnCompleteListener {
                                    if (index == totalInvestments - 1) {
                                        utils.endLoadingAnimation()
                                        Toast.makeText(mContext, "Profit Added Successfully!", Toast.LENGTH_SHORT).show()

                                    }
                                }
                        }


                }


            }
        }


    }*/

    private fun getData() {
        db.collection(constants.INVESTMENT_COLLECTION).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    utils.endLoadingAnimation()

                    if(task.result.size()>0){

                        var balance:Int=0
                        for (document in task.result){
                            var investmentModel=document.toObject(InvestmentModel::class.java)
                            listInvestmentModel.add( document.toObject(InvestmentModel::class.java))
                            if (investmentModel!=null) balance=balance+ investmentModel.investmentBalance.toInt()
                        }



                    }
                }
                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{
                Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

            }
    }
}