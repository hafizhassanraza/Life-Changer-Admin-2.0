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
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityAddProfitBinding
import com.enfotrix.adminlifechanger.databinding.ActivityAddTaxBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ActivityAddTax : AppCompatActivity() {

    private val db = Firebase.firestore





    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    var constant= Constants()



    private lateinit var binding: ActivityAddTaxBinding
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
        binding = ActivityAddTaxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Investment Details"

        mContext=this@ActivityAddTax
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        //41//1111


        /* binding.btnAccept.setOnClickListener{

             if(intent.getStringExtra("from").toString().equals(constant.FROM_PENDING_WITHDRAW_REQ)) approvedWithdraw()
             else approved()


         }*/



       /* binding.btnAddProfit.setOnClickListener{

            val percentage = binding.etProfit.text.toString()

            addProfit(percentage.toDouble() / 100)
        }*/
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

    private fun addProfit(percentage: Double) {


        utils.startLoadingAnimation()
        val totalInvestments = listInvestmentModel.size


        for ((index, investmentModel) in listInvestmentModel.withIndex()){


            var previousBalance = investmentModel.investmentBalance

            if (previousBalance != null && previousBalance != "") {
                var profit: Double = 0.0

                var previousBalance_ = previousBalance.toInt()
                profit = previousBalance_ * percentage
                var newBalance = previousBalance_ - profit.toInt()
                investmentModel.investmentBalance = newBalance.toString()


                var profit_ = profit.toInt()

                var transactionModel = TransactionModel(
                    investmentModel.investorID,
                    "Tax",
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

                lifecycleScope.launch {
                    investmentViewModel.setInvestment(investmentModel)
                        .addOnCompleteListener { task ->


                            db.collection(constants.TRANSACTION_REQ_COLLECTION)
                                .add(transactionModel)
                                .addOnCompleteListener {
                                    if (index == totalInvestments - 1) {
                                        utils.endLoadingAnimation()

                                        Toast.makeText(mContext, "Tax Added Successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }


                }


            }
        }

    }

}