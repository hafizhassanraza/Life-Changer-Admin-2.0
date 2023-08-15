package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.databinding.ActivityAddProfitBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ActivityAddProfit : AppCompatActivity() {

    private val db = Firebase.firestore





    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    var constant= Constants()



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

            addProfit(percentage.toDouble() / 100)
        }

    }

    private fun addProfit(percentage: Double) {


        utils.startLoadingAnimation()

        val totalInvestments = listInvestmentModel.size


        for ((index, investmentModel) in listInvestmentModel.withIndex()){


            var previousBalance = investmentModel.investmentBalance

            if (previousBalance != null && previousBalance != "") {
                var profit: Double=0.0

                var previousBalance_ = previousBalance.toInt()
                profit = previousBalance_ * percentage
                var newBalance = previousBalance_ + profit.toInt()
                investmentModel.investmentBalance = newBalance.toString()


                var profit_=profit.toInt()

                var transactionModel=TransactionModel(
                    investmentModel.investorID,
                    "Profit",
                    "Approved",
                    profit_.toString(),
                    "",
                    previousBalance_.toString(),
                    "",
                    "",
                    newBalance.toString(),
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

                /*Toast.makeText(
                    mContext,
                    previousBalance_.toString() + " " + newBalance.toString(),
                    Toast.LENGTH_SHORT
                ).show()*/
            }
        }



/*

            var amount//profit amount



            val newBalance:Int

            newBalance= amount.toInt()+previousBalance.toInt()

            var transactionModel=TransactionModel(
                user.id,
                "Profit",
                "Approved",
                amount,
                "",
                previousBalance,
                "",
                "",
                newBalance.toString(),
                Timestamp.now(),
                Timestamp.now()
            )
            investmentModel.investmentBalance = newBalance.toString()



        }


        etBalance.text.toString(),
        "",
        investmentModel.investmentBalance,
        ""









        */
/*transactionModel.status=constants.TRANSACTION_STATUS_APPROVED
        transactionModel.transactionAt= Timestamp.now()
        val transactionAmount = transactionModel?.amount?.toInt() ?: 0
        if (investmentModel != null) {
            val currentBalance = investmentModel.investmentBalance.toInt()
            val newBalance = currentBalance + transactionAmount
            investmentModel.investmentBalance = newBalance.toString()
            transactionModel?.newBalance= newBalance.toString()
        }*//*


        utils.startLoadingAnimation()

        lifecycleScope.launch{
            investmentViewModel.setInvestment(investmentModel)
                .addOnCompleteListener{task->


                    db.collection(constants.TRANSACTION_REQ_COLLECTION).add(transactionModel)
                        .addOnCompleteListener {
                            utils.endLoadingAnimation()
                            Toast.makeText(mContext, "Profit Added", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(mContext,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                            finish()
                        }
                }


        }
*/




    }

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