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
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityInvestmentReqDetailsBinding
import com.enfotrix.adminlifechanger.databinding.ActivityInvestmentRequestBinding
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ActivityInvestmentReqDetails : AppCompatActivity() {




    private val db = Firebase.firestore





    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    var constant= Constants()



    private lateinit var binding: ActivityInvestmentReqDetailsBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog


    private lateinit var transactionModel:TransactionModel
    private lateinit var investmentModel:InvestmentModel
    private lateinit var user: User



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentReqDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Investment Details"

        mContext=this@ActivityInvestmentReqDetails
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)



        binding.btnAccept.setOnClickListener{
            approved()
        }
        setData()


        getData()
    }

    private fun getData() {

        transactionModel= TransactionModel.fromString( intent.getStringExtra("transactionModel").toString())!!
         user=User.fromString( intent.getStringExtra("User").toString())!!

        Toast.makeText(mContext, user.id, Toast.LENGTH_SHORT).show()
        utils.startLoadingAnimation()


        db.collection(constants.INVESTMENT_COLLECTION).document(user.id).get()
            .addOnCompleteListener{task ->
                utils.endLoadingAnimation()
                if(task.result.exists()) investmentModel = task.result.toObject(InvestmentModel::class.java)!!

            }

    }

    private fun approved() {



        transactionModel.status=constants.TRANSACTION_STATUS_APPROVED
        transactionModel.transactionAt= Timestamp.now()
        val transactionAmount = transactionModel?.amount?.toInt() ?: 0
        if (investmentModel != null) {
            val currentBalance = investmentModel.investmentBalance.toInt()
            val newBalance = currentBalance + transactionAmount
            investmentModel.investmentBalance = newBalance.toString()
            transactionModel?.newBalance= newBalance.toString()
        }







        utils.startLoadingAnimation()

        lifecycleScope.launch{

            investmentViewModel.setInvestment(investmentModel)
                .addOnCompleteListener{task->


                    Toast.makeText(mContext, transactionModel.id, Toast.LENGTH_SHORT).show()



                    db.collection(constants.TRANSACTION_REQ_COLLECTION).document(transactionModel.id).set(transactionModel)
                        .addOnCompleteListener {
                            utils.endLoadingAnimation()
                            Toast.makeText(mContext, "Investment Approved", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(mContext,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                            finish()
                        }
                }


        }



    }



    fun setData(){


        var transactionModel=TransactionModel.fromString( intent.getStringExtra("transactionModel").toString())
        var user=User.fromString( intent.getStringExtra("User").toString())
        var modelFA=ModelFA.fromString( intent.getStringExtra("FA").toString())

        var senderBankAccount = sharedPrefManager.getAccountList().find { it.docID.equals(transactionModel?.senderAccountID) }
        var receiverBankAccount = sharedPrefManager.getAccountList().find { it.docID.equals(transactionModel?.receiverAccountID) }


        binding.tvInvestorName.text="${user?.firstName} ${user?.lastName}"
        binding.tvInvestorCnic.text="${user?.cnic}"
        binding.investmentDate.text="${SimpleDateFormat( "hh:mm a dd/MM/yy", Locale.getDefault()).format(transactionModel?.createdAt!!.toDate()).toString()}"
        binding.tvInvestment.text="${transactionModel?.amount}"

        binding.tvInvestorBankName.text="${senderBankAccount?.bank_name}"
        binding.tvInvestorAccountNumber.text="${senderBankAccount?.account_number}"
        binding.tvInvestorAccountTittle.text="${senderBankAccount?.account_tittle}"

        binding.tvBankName.text="${receiverBankAccount?.bank_name}"
        binding.tvAccountNumber.text="${receiverBankAccount?.account_number}"
        binding.tvAccountTittle.text="${receiverBankAccount?.account_tittle}"

        binding.tvFAName.text="${modelFA?.firstName} ${modelFA?.lastName}"
        binding.tvDesignation.text="${modelFA?.designantion}"

    }





}