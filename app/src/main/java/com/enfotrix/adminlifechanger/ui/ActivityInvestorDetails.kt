package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityInvestorDetailsBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch

class ActivityInvestorDetails : AppCompatActivity() {

    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()


    private lateinit var investmentModel: InvestmentModel


    private val db  = Firebase.firestore
    private val firebaseStorage = Firebase.storage
    private val storageRef = firebaseStorage.reference

    var constant= Constants()

    private lateinit var binding: ActivityInvestorDetailsBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityInvestorDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityInvestorDetails
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        user= User.fromString( intent.getStringExtra("user").toString())!!

        supportActionBar?.title = user.firstName

        Glide.with(mContext).load(user.photo).centerCrop().placeholder(R.drawable.ic_launcher_background).into(binding.userPhoto);
        binding.tvInvestorName.text = user.firstName
        binding.tvInvestorFatherName.text = user.lastName
        binding.tvInvestorCnic.text = user.cnic
        binding.tvInvestorPhoneNumber.text = user.phone



        binding.layNotification.setOnClickListener { startActivity(Intent(mContext, ActivityNotification::class.java).putExtra("user",user.toString())) }

        binding.tvViewDetailsInvestment.setOnClickListener { startActivity(Intent(mContext, ActivityInvestorDetailsTransactions::class.java).putExtra("user",user.toString())) }
        binding.tvViewDetailsUser.setOnClickListener { startActivity(Intent(mContext, ActivityInvestorDetailsProfile::class.java).putExtra("user",user.toString())) }
        binding.layInvest.setOnClickListener {showAddBalanceDialog() }
        binding.layWithdraw.setOnClickListener {showWithdrawBalanceDialog() }
        binding.layTax.setOnClickListener {showTaxBalanceDialog() }
        binding.layProfit.setOnClickListener {showProfitBalanceDialog() }
        getFA()
        getInvestment()


    }

    private fun getInvestment() {

        db.collection(constants.INVESTMENT_COLLECTION).document(user.id)
            .addSnapshotListener { snapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                snapshot?.let { document ->

                    investmentModel = document.toObject(InvestmentModel::class.java)!!

                    binding.tvBalance.text=investmentModel.investmentBalance
                    binding.tvInActiveInvestment.text=investmentModel.lastInvestment
                    binding.availableProfit.text=investmentModel.lastProfit

                }
            }
    }

    fun getFA() {

        if (!user.fa_id.isNullOrEmpty()){
            db.collection(constants.FA_COLLECTION).document(user.fa_id)
                .addSnapshotListener { snapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    snapshot?.let { document ->
                        val FA = document.toObject(ModelFA::class.java)
                        if (FA != null) {
                            binding.tvFAName.text = FA.firstName
                            binding.tvFADesignation.text = FA.designantion
                            Glide.with(mContext).load(FA.photo).centerCrop().placeholder(R.drawable.ic_launcher_background).into(binding.imgFA);
                        }

                    }
                }

        }
    }



    fun showAddBalanceDialog() {




        var dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_balance)

        val etBalance = dialog.findViewById<EditText>(R.id.etBalance)
        val btnAddBalance = dialog.findViewById<Button>(R.id.btnAddBalance)


        btnAddBalance.setOnClickListener {


            if (etBalance.text.toString().isNullOrEmpty()) Toast.makeText(mContext, "Please Enter Amount", Toast.LENGTH_SHORT).show()
            else {
                val amount = etBalance.text.toString()?.toInt() ?: 0
                if (amount<= 0) Toast.makeText(mContext, "Please Enter Correct Amount", Toast.LENGTH_SHORT).show()
                else{
                    dialog.dismiss()

                    addInvestment(
                        amount,
                        "",
                        investmentModel.investmentBalance,
                        ""
                    )
                }
            }








        }
        dialog.show()



    }
    fun showWithdrawBalanceDialog() {


        var dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_balance)

        val etBalance = dialog.findViewById<EditText>(R.id.etBalance)
        val btnAddBalance = dialog.findViewById<Button>(R.id.btnAddBalance)
        val tvHeaderBank = dialog.findViewById<TextView>(R.id.tvHeaderBank)
        btnAddBalance.text="Withdraw"
        tvHeaderBank.text="Withdraw"

        btnAddBalance.setOnClickListener {


            if (etBalance.text.toString().isNullOrEmpty()) Toast.makeText(mContext, "Please Enter Amount", Toast.LENGTH_SHORT).show()
            else {
                val amount = etBalance.text.toString()?.toInt() ?: 0
                if (amount<= 0) Toast.makeText(mContext, "Please Enter Correct Amount", Toast.LENGTH_SHORT).show()
                else{
                    dialog.dismiss()

                    withdrawInvestment(
                        amount,
                        "",
                        investmentModel.investmentBalance,
                        ""
                    )
                }
            }





        }

        dialog.show()
    }
    fun showProfitBalanceDialog() {


        var dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_balance)

        val etBalance = dialog.findViewById<EditText>(R.id.etBalance)
        val btnAddBalance = dialog.findViewById<Button>(R.id.btnAddBalance)
        val tvHeaderBank = dialog.findViewById<TextView>(R.id.tvHeaderBank)
        btnAddBalance.text="Profit"
        tvHeaderBank.text="Profit"

        btnAddBalance.setOnClickListener {



            if (etBalance.text.toString().isNullOrEmpty()) Toast.makeText(mContext, "Please Enter Amount", Toast.LENGTH_SHORT).show()
            else {
                val amount = etBalance.text.toString()?.toInt() ?: 0
                if (amount<= 0) Toast.makeText(mContext, "Please Enter Correct Amount", Toast.LENGTH_SHORT).show()
                else{
                    dialog.dismiss()

                    addProfit(
                        amount,
                        "",
                        investmentModel.investmentBalance,
                        ""
                    )
                }
            }



        }

        dialog.show()
    }
    fun showTaxBalanceDialog() {


        var dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_balance)

        val etBalance = dialog.findViewById<EditText>(R.id.etBalance)
        val btnAddBalance = dialog.findViewById<Button>(R.id.btnAddBalance)
        val tvHeaderBank = dialog.findViewById<TextView>(R.id.tvHeaderBank)
        btnAddBalance.text="Tax"
        tvHeaderBank.text="Tax"

        btnAddBalance.setOnClickListener {

            if (etBalance.text.toString().isNullOrEmpty()) Toast.makeText(mContext, "Please Enter Amount", Toast.LENGTH_SHORT).show()
            else {
                val amount = etBalance.text.toString()?.toInt() ?: 0
                if (amount<= 0) Toast.makeText(mContext, "Please Enter Correct Amount", Toast.LENGTH_SHORT).show()
                else{
                    dialog.dismiss()

                    addTax(
                        amount,
                        "",
                        investmentModel.investmentBalance,
                        ""
                    )
                }
            }



        }

        dialog.show()
    }



    private fun addInvestment(amount: Int ,receiverAccountID:String ,previousBalance:String ,senderAccountID:String ) {

        val previousTotalBalance = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()



        var newBalance:Int

        newBalance= previousBalance.toInt() // new amount will be added into inactive balance



        var transactionModel= TransactionModel(
            user.id,
            "Investment",
            "Approved",
            amount.toString(),
            receiverAccountID,
            previousTotalBalance.toString(),
            senderAccountID,
            "",
            "",
            Timestamp.now(),
            Timestamp.now()
        )

        //investmentModel.lastInvestment = (investmentModel.lastInvestment?.toIntOrNull() ?: 0 + amount.toInt()?: 0).toString()

        var previousInActiveInvestment= 0
        if(!investmentModel.lastInvestment.isNullOrEmpty()) previousInActiveInvestment= investmentModel.lastInvestment.toInt()
        val newInActiveInvestment_ = previousInActiveInvestment+amount
        investmentModel.lastInvestment = newInActiveInvestment_.toString()


        /*transactionModel.status=constants.TRANSACTION_STATUS_APPROVED
        transactionModel.transactionAt= Timestamp.now()
        val transactionAmount = transactionModel?.amount?.toInt() ?: 0
        if (investmentModel != null) {
            val currentBalance = investmentModel.investmentBalance.toInt()
            val newBalance = currentBalance + transactionAmount
            investmentModel.investmentBalance = newBalance.toString()
            transactionModel?.newBalance= newBalance.toString()
        }*/

        val newTotalBalance = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()
        transactionModel.newBalance=newTotalBalance.toString()

        utils.startLoadingAnimation()

        lifecycleScope.launch{
            investmentViewModel.setInvestment(investmentModel)
                .addOnCompleteListener{task->


                    db.collection(constants.TRANSACTION_REQ_COLLECTION).add(transactionModel)
                        .addOnCompleteListener {
                            utils.endLoadingAnimation()
                            Toast.makeText(mContext, "Investment Added", Toast.LENGTH_SHORT).show()
                            getInvestment()
                            //startActivity(Intent(mContext,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                            //finish()
                        }
                }


        }



    }


    private fun withdrawInvestment(amount: Int, receiverAccountID: String, previousBalance: String, senderAccountID: String) {


        val previousTotalBalance = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()

        Toast.makeText(mContext, ""+ previousBalance.toInt().toString(), Toast.LENGTH_SHORT).show()
        var transactionAmount =amount//var transactionAmount = transactionModel?.amount?.toInt() ?: 0



        val newBalance:Int

        newBalance= previousBalance.toInt()-transactionAmount.toInt()


        var transactionModel= TransactionModel(
            user.id,
            "Withdraw",
            "Approved",
            transactionAmount.toString(),
            receiverAccountID,
            previousTotalBalance.toInt().toString(),
            senderAccountID,
            "",
            "",
            Timestamp.now(),
            Timestamp.now()
        )





        investmentModel?.let {
            var investment = it.investmentBalance.toInt()

            var profit = 0
            var inActiveInv = 0
            if(it.lastProfit!="") profit=it.lastProfit.toInt()
            if(it.lastInvestment!="") inActiveInv=it.lastInvestment.toInt()




            if (transactionAmount <= inActiveInv) {
                inActiveInv -= transactionAmount
            }
            else {
                var sumOfProfit_InActInv= inActiveInv + profit
                if(transactionAmount <= sumOfProfit_InActInv){

                    transactionAmount=transactionAmount-inActiveInv
                    inActiveInv=0
                    profit -= transactionAmount
                }
                else {
                    transactionAmount=transactionAmount-profit-inActiveInv
                    profit = 0
                    inActiveInv=0
                    investment -= transactionAmount
                }
            }


            /*if (transactionAmount <= profit) {
                profit -= transactionAmount
            } else {

                transactionAmount=transactionAmount-profit  //80=100-20
                profit = 0
                investment -= transactionAmount //220=300-80
            }*/

            //




            it.investmentBalance = investment.toString()
            it.lastProfit = profit.toString()
            it.lastInvestment = inActiveInv.toString()

        }






        val newTotalBalance = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()
        transactionModel.newBalance=newTotalBalance.toString()

        utils.startLoadingAnimation()

        lifecycleScope.launch{
            investmentViewModel.setInvestment(investmentModel)
                .addOnCompleteListener{task->


                    db.collection(constants.TRANSACTION_REQ_COLLECTION).add(transactionModel)
                        .addOnCompleteListener {
                            utils.endLoadingAnimation()
                            Toast.makeText(mContext, "Withdraw Successfully", Toast.LENGTH_SHORT).show()
                            getInvestment()

                        }
                }


        }



    }
    private fun addTax(amount: Int, receiverAccountID: String, previousBalance: String, senderAccountID: String)                                                                         {



        val previousTotalBalance = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()


        val newBalance:Int

        newBalance= previousBalance.toInt()-amount.toInt()



        var transactionModel= TransactionModel(
            user.id,
            "Tax",
            "Approved",
            amount.toString(),
            receiverAccountID,
            previousTotalBalance.toString(),
            senderAccountID,
            "",
            "",
            Timestamp.now(),
            Timestamp.now()
        )
        investmentModel.investmentBalance = newBalance.toString()

        val newTotalBalance = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()
        transactionModel.newBalance=newTotalBalance.toString()

        utils.startLoadingAnimation()

        lifecycleScope.launch{
            investmentViewModel.setInvestment(investmentModel)
                .addOnCompleteListener{task->


                    db.collection(constants.TRANSACTION_REQ_COLLECTION).add(transactionModel)
                        .addOnCompleteListener {
                            utils.endLoadingAnimation()
                            Toast.makeText(mContext, "Tax Deduction Successfully", Toast.LENGTH_SHORT).show()
                            getInvestment()
                        }
                }


        }



    }

    private fun addProfit(amount: Int, receiverAccountID: String, previousBalance: String, senderAccountID: String) {




        val newBalance:Int

        newBalance= previousBalance.toInt()

        val previousTotalBalance = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()



        var transactionModel= TransactionModel(
            user.id,
            "Profit",
            "Approved",
            amount.toString(),
            receiverAccountID,
            previousTotalBalance.toString(),
            senderAccountID,
            "",
            "",
            Timestamp.now(),
            Timestamp.now()
        )

        var previousProfit= 0

        if(!investmentModel.lastProfit.isNullOrEmpty()) previousProfit= investmentModel.lastProfit.toInt()

        val newProfit_ = previousProfit+amount
        investmentModel.lastProfit = newProfit_.toString()


        val newTotalBalance = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()
        transactionModel.newBalance=newTotalBalance.toString()

        utils.startLoadingAnimation()

        lifecycleScope.launch{
            investmentViewModel.setInvestment(investmentModel)
                .addOnCompleteListener{task->


                    db.collection(constants.TRANSACTION_REQ_COLLECTION).add(transactionModel)
                        .addOnCompleteListener {
                            utils.endLoadingAnimation()
                            Toast.makeText(mContext, "Profit Added", Toast.LENGTH_SHORT).show()
                            getInvestment()
                        }
                }


        }



    }




    fun getTextFromInvestment(value: String?): String {
        return if (value.isNullOrEmpty()) "0" else value
    }


}