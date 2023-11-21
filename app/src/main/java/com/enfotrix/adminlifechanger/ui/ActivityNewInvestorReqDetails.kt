package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.Adapters.AdapterFA
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityInvestmentReqDetailsBinding
import com.enfotrix.adminlifechanger.databinding.ActivityNewInvestorReqDetailsBinding
import com.enfotrix.adminlifechanger.databinding.ActivityNewInvestorsReqBinding
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch

class ActivityNewInvestorReqDetails : AppCompatActivity(),AdapterFA.OnItemClickListener {

    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel:FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()


    private lateinit var investmentModel:InvestmentModel








    private val db = Firebase.firestore
    private val firebaseStorage = Firebase.storage
    private val storageRef = firebaseStorage.reference

















    var constant= Constants()



    private lateinit var binding: ActivityNewInvestorReqDetailsBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewInvestorReqDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mContext=this@ActivityNewInvestorReqDetails
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        //Toast.makeText(mContext, sharedPrefManager.getNomineeList().size.toString(), Toast.LENGTH_SHORT).show()
        //Toast.makeText(mContext, sharedPrefManager.getAccountList().size.toString(), Toast.LENGTH_SHORT).show()
        //Toast.makeText(mContext, sharedPrefManager.getFAList().size.toString(), Toast.LENGTH_SHORT).show()

        user= User.fromString( intent.getStringExtra("user").toString())!!


        binding.cd7.visibility=View.GONE
        binding.cd8.visibility=View.GONE
        binding.cd9.visibility=View.GONE
        binding.cd10.visibility=View.GONE

        if(intent.getStringExtra("from").equals("active")){

            binding.cd6.visibility=View.GONE
            binding.btnApprove.visibility=View.GONE


            binding.cd7.visibility=View.VISIBLE
            binding.cd8.visibility=View.VISIBLE
            binding.cd9.visibility=View.VISIBLE
            binding.cd10.visibility=View.VISIBLE
            getInvestment()

        }


        supportActionBar?.title = user.firstName


        binding.btnFAAssigned.setOnClickListener { showFADialog() }



        binding.btnApprove.setOnClickListener { approve() }
        binding.btnAddInvestment.setOnClickListener {showAddBalanceDialog() }
        binding.btnWithdraw.setOnClickListener {showWithdrawBalanceDialog() }
        binding.btnTax.setOnClickListener {showTaxBalanceDialog() }
        binding.btnProfit.setOnClickListener {showProfitBalanceDialog() }

        getUsers_Account_Nominee_FA()
    }

    fun showAddBalanceDialog() {




        var dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_balance)

        val etBalance = dialog.findViewById<EditText>(R.id.etBalance)
        val btnAddBalance = dialog.findViewById<Button>(R.id.btnAddBalance)


        btnAddBalance.setOnClickListener {
            dialog.dismiss()

            addInvestment(
                etBalance.text.toString(),
                "",
                investmentModel.investmentBalance,
                ""
            )

            binding.tvNewBalance.text=etBalance.text
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
            dialog.dismiss()

            withdrawInvestment(
                etBalance.text.toString(),
                "",
                investmentModel.investmentBalance,
                ""
            )

            binding.tvNewBalance.text=etBalance.text
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
            dialog.dismiss()

            addProfit(
                etBalance.text.toString(),
                "",
                investmentModel.investmentBalance,
                ""
            )

            binding.tvNewBalance.text=etBalance.text
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
            dialog.dismiss()

            addTax(
                etBalance.text.toString(),
                "",
                investmentModel.investmentBalance,
                ""
            )

            binding.tvNewBalance.text=etBalance.text
        }

        dialog.show()
    }
    private fun getInvestment() {

        db.collection(constants.INVESTMENT_COLLECTION).document(user.id).get()
            .addOnCompleteListener{task ->
                utils.endLoadingAnimation()
                if(task.result.exists()) {

                    investmentModel = task.result.toObject(InvestmentModel::class.java)!!
                    binding.tvBalance.text= investmentModel.investmentBalance.toString()
                    binding.tvBalance2.text= investmentModel.investmentBalance.toString()

                }
            }
    }

    private fun withdrawInvestment(amount:String ,receiverAccountID:String ,previousBalance:String ,senderAccountID:String ,) {




        val newBalance:Int

        newBalance= previousBalance.toInt()-amount.toInt()



        var transactionModel=TransactionModel(
            user.id,
            "Withdraw",
            "Approved",
            amount,
            receiverAccountID,
            previousBalance,
            senderAccountID,
            "",
            newBalance.toString(),
            Timestamp.now(),
            Timestamp.now()
        )



        //5263 + 105


        var transactionAmount = amount?.toInt() ?: 0

        investmentModel?.let {
            var investment = it.investmentBalance.toInt()
            var profit = it.lastProfit.toInt()

            var previousBalance= investment+profit


            if (transactionAmount <= profit) {
                profit -= transactionAmount
            } else {
                profit = 0
                transactionAmount=transactionAmount-profit // profit deduction from transaction amount to match profit= 0
                investment -= transactionAmount
            }

            //
            var newBalance= investment+profit


            transactionModel?.previousBalance = previousBalance.toString()
            transactionModel?.newBalance = newBalance.toString()

            it.investmentBalance = investment.toString()
            it.lastProfit = profit.toString()

        }














        utils.startLoadingAnimation()

        lifecycleScope.launch{
            investmentViewModel.setInvestment(investmentModel)
                .addOnCompleteListener{task->


                    db.collection(constants.TRANSACTION_REQ_COLLECTION).add(transactionModel)
                        .addOnCompleteListener {
                            utils.endLoadingAnimation()
                            Toast.makeText(mContext, "Withdraw Successfully", Toast.LENGTH_SHORT).show()
                            //startActivity(Intent(mContext,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                            //finish()
                        }
                }


        }



    }
    private fun addTax(amount:String ,receiverAccountID:String ,previousBalance:String ,senderAccountID:String ,) {




        val newBalance:Int

        newBalance= previousBalance.toInt()-amount.toInt()



        var transactionModel=TransactionModel(
            user.id,
            "Tax",
            "Approved",
            amount,
            receiverAccountID,
            previousBalance,
            senderAccountID,
            "",
            newBalance.toString(),
            Timestamp.now(),
            Timestamp.now()
        )
        investmentModel.investmentBalance = newBalance.toString()

        utils.startLoadingAnimation()

        lifecycleScope.launch{
            investmentViewModel.setInvestment(investmentModel)
                .addOnCompleteListener{task->


                    db.collection(constants.TRANSACTION_REQ_COLLECTION).add(transactionModel)
                        .addOnCompleteListener {
                            utils.endLoadingAnimation()
                            Toast.makeText(mContext, "Tax Deduction Successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(mContext,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                            finish()
                        }
                }


        }



    }

    private fun addProfit(amount:String ,receiverAccountID:String ,previousBalance:String ,senderAccountID:String ,) {




        val newBalance:Int

        newBalance= amount.toInt()+previousBalance.toInt()



        var transactionModel=TransactionModel(
            user.id,
            "Profit",
            "Approved",
            amount,
            receiverAccountID,
            previousBalance,
            senderAccountID,
            "",
            newBalance.toString(),
            Timestamp.now(),
            Timestamp.now()
        )
        investmentModel.investmentBalance = newBalance.toString()


        /*transactionModel.status=constants.TRANSACTION_STATUS_APPROVED
        transactionModel.transactionAt= Timestamp.now()
        val transactionAmount = transactionModel?.amount?.toInt() ?: 0
        if (investmentModel != null) {
            val currentBalance = investmentModel.investmentBalance.toInt()
            val newBalance = currentBalance + transactionAmount
            investmentModel.investmentBalance = newBalance.toString()
            transactionModel?.newBalance= newBalance.toString()
        }*/

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



    }

    private fun addInvestment(amount:String ,receiverAccountID:String ,previousBalance:String ,senderAccountID:String ,) {




        val newBalance:Int

        newBalance= previousBalance.toInt()
        //newBalance= amount.toInt()+previousBalance.toInt()



        var transactionModel=TransactionModel(
            user.id,
            "Investment",
            "Approved",
            amount,
            receiverAccountID,
            previousBalance,
            senderAccountID,
            "",
            newBalance.toString(),
            Timestamp.now(),
            Timestamp.now()
        )

        //investmentModel.lastInvestment = (investmentModel.lastInvestment?.toIntOrNull() ?: 0 + amount.toInt()?: 0).toString()

         var previousInActiveInvestment= 0
         if(!investmentModel.lastInvestment.isNullOrEmpty()) previousInActiveInvestment= investmentModel.lastInvestment.toInt()
         investmentModel.lastInvestment = (previousInActiveInvestment+amount.toInt()).toString()


        /*transactionModel.status=constants.TRANSACTION_STATUS_APPROVED
        transactionModel.transactionAt= Timestamp.now()
        val transactionAmount = transactionModel?.amount?.toInt() ?: 0
        if (investmentModel != null) {
            val currentBalance = investmentModel.investmentBalance.toInt()
            val newBalance = currentBalance + transactionAmount
            investmentModel.investmentBalance = newBalance.toString()
            transactionModel?.newBalance= newBalance.toString()
        }*/

        utils.startLoadingAnimation()

        lifecycleScope.launch{
            investmentViewModel.setInvestment(investmentModel)
                .addOnCompleteListener{task->


                    db.collection(constants.TRANSACTION_REQ_COLLECTION).add(transactionModel)
                        .addOnCompleteListener {
                            utils.endLoadingAnimation()
                            Toast.makeText(mContext, "Investment Added", Toast.LENGTH_SHORT).show()
                            //startActivity(Intent(mContext,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                            //finish()
                        }
                }


        }



    }

    fun getUsers_Account_Nominee_FA(){






        utils.startLoadingAnimation()
        db.collection(constants.ACCOUNTS_COLLECTION).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val listAccounts = ArrayList<ModelBankAccount>()
                    if(task.result.size()>0){
                        for (document in task.result)listAccounts.add( document.toObject(ModelBankAccount::class.java).apply { docID = document.id })
                        sharedPrefManager.putAccountList(listAccounts)


                        db.collection(constants.NOMINEE_COLLECTION).get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val listNominee = ArrayList<ModelNominee>()
                                    if(task.result.size()>0){
                                        for (document in task.result) listNominee.add( document.toObject(ModelNominee::class.java).apply { docID = document.id })
                                        sharedPrefManager.putNomineeList(listNominee)


                                        db.collection(constants.FA_COLLECTION).get()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {

                                                    val listFA = ArrayList<ModelFA>()
                                                    if(task.result.size()>0){
                                                        for (document in task.result)listFA.add( document.toObject(ModelFA::class.java).apply { id = document.id })

                                                        sharedPrefManager.putFAList(listFA)






                                                        db.collection(constants.INVESTOR_COLLECTION).get()
                                                            .addOnCompleteListener { task ->
                                                                if (task.isSuccessful) {
                                                                    utils.endLoadingAnimation()

                                                                    val listInvestors = ArrayList<User>()
                                                                    if(task.result.size()>0){
                                                                        for (document in task.result)listInvestors.add( document.toObject(User::class.java).apply { id = document.id })
                                                                        sharedPrefManager.putFAList(listFA)


                                                                        setData(user)


                                                                    }
                                                                }
                                                                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()




                                                            }
                                                            .addOnFailureListener{
                                                                Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

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
                                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                            }
                            .addOnFailureListener{
                                Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                            }



                    }
                }
                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

            }


    }
    fun setData(user: User){

        binding.tvInvestorName.text = user.firstName
        binding.tvInvestorFatherName.text = user.lastName
        binding.tvInvestorCnic.text = user.cnic
        binding.tvInvestorPhoneNumber.text = user.phone
        binding.tvInvestorAddress.text = user.address


        var bankAccount=sharedPrefManager.getAccountList().find { it.account_holder.equals(user.id)}


        binding.tvInvestorBankName.text = bankAccount?.bank_name
        binding.tvInvestorBankAccountNumber.text = bankAccount?.account_number
        binding.tvInvestorBankAccountTittle.text = bankAccount?.account_tittle


        var nominee=sharedPrefManager.getNomineeList().find { it.nominator.equals(user.id)}

        Toast.makeText(mContext, sharedPrefManager.getNomineeList().size.toString(), Toast.LENGTH_SHORT).show()
        if (nominee != null) {
            Log.d("d1",nominee.nominator)
        }
        Log.d("d2",user.id)
        //Toast.makeText(mContext, nominee!!.nominator, Toast.LENGTH_SHORT).show()

        binding.tvNomineeAddress.text=nominee?.address
        binding.tvNomineeCNIC.text=nominee?.cnic
        binding.tvNomineeBankName.text=nominee?.bank_name
        binding.tvNomineeName.text=nominee?.firstName
        binding.tvNomineePhone.text=nominee?.phone
        binding.tvNomineeBankAccountNumber.text=nominee?.acc_number
        binding.tvNomineeBankAccountTittle.text=nominee?.acc_tittle
        binding.tvNomineeFatherName.text=nominee?.lastName

        binding.tvHeader1.text= "Investor's Nominee (${nominee?.nominator_relation})"



    }


    fun approve(){


        user.status=constant.INVESTOR_STATUS_ACTIVE

        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.setUser(user)
                .addOnCompleteListener{task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {

                        Toast.makeText(mContext, "Investor Approved", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(mContext,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                        finish()


                    }
                    else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener{
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                }




        }

    }


    fun showFADialog(){

        var rvFA: RecyclerView

        dialog = BottomSheetDialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.bottom_sheet_investors)

        rvFA = dialog.findViewById<RecyclerView>(R.id.rvInvestors)as RecyclerView
        rvFA.layoutManager = LinearLayoutManager(mContext)
        rvFA.adapter= faViewModel.getFAAdapter(this@ActivityNewInvestorReqDetails)

        dialog.show()

    }


    override fun onItemClick(modelFA: ModelFA) {



        //binding.imgFAProfile
        binding.tvFAName.text=modelFA.firstName+" "+modelFA.lastName
        binding.tvDesignation.text=modelFA.designantion
        binding.layAssigned.setVisibility(View.VISIBLE)
        binding.layUnAssigned.setVisibility(View.GONE)
        user.fa_id=modelFA.id
        Toast.makeText(mContext, modelFA.id, Toast.LENGTH_SHORT).show()
        dialog.dismiss()


    }

    override fun onDeleteClick(modelFA: ModelFA) {

    }

}