package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.enfotrix.adminlifechanger.API.FCM
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Models.NotificationModel
import com.enfotrix.adminlifechanger.Models.NotificationViewModel
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
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityInvestmentReqDetails : AppCompatActivity() {


    private val db = Firebase.firestore
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    var constant= Constants()
    private val notificationViewModel: NotificationViewModel by viewModels()


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


        //41//1111


        setData()

        binding.receiptimage.setOnClickListener {
        downloadImageUsingDownloadManager("receipturl")
        }


        binding.btnAccept.setOnClickListener{

            if(intent.getStringExtra("from").toString().equals(constant.FROM_PENDING_WITHDRAW_REQ)) approvedWithdraw()
            else if(intent.getStringExtra("from").toString().equals(constant.FROM_PENDING_INVESTMENT_REQ)) approvedInvestment()
        }


    }


    private fun downloadImageUsingDownloadManager(imageUrl: String) {
        utils.startLoadingAnimation()
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(imageUrl))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setTitle("Image Download") // Set your desired title here
            .setDescription("Downloading") // Set your desired description here
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Cnic.jpg")

        downloadManager.enqueue(request)
        utils.endLoadingAnimation()
    }



    private fun approvedWithdraw() {



        transactionModel.status=constants.TRANSACTION_STATUS_APPROVED
        transactionModel.transactionAt= Timestamp.now()
        var transactionAmount = transactionModel?.amount?.toInt() ?: 0

        investmentModel?.let {
            var investment = it.investmentBalance.toInt()

            var profit = 0
            var inActiveInv = 0
            if(it.lastProfit!="") profit=it.lastProfit.toInt()
            if(it.lastInvestment!="") inActiveInv=it.lastInvestment.toInt()
            //var previousBalance= investment+profit
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

                transactionAmount=transactionAmount-profit
                profit = 0
                investment -= transactionAmount
            }*/

            it.investmentBalance = investment.toString()
            it.lastProfit = profit.toString()
            it.lastInvestment= inActiveInv.toString()

        }


        val newTotal = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()
        transactionModel.newBalance= newTotal.toInt().toString()


        utils.startLoadingAnimation()

        db.collection(constants.INVESTMENT_COLLECTION).document(investmentModel.investorID).set(investmentModel)
            .addOnSuccessListener {

                db.collection(constants.TRANSACTION_REQ_COLLECTION).document(transactionModel.id).set(transactionModel)
                    .addOnSuccessListener {

                        //for notifications
                        val amount = SpannableString(transactionAmount.toString())
                        amount.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            amount.length,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE
                        )
                        //for notifications
                        val name = SpannableString(user?.firstName)
                        name.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            name.length,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE
                        )

                        val notificationData =
                            "Dear $name, Your withdraw request of $amount has been approved."
                        addNotification(
                             user,
                            NotificationModel(
                                "",
                                user!!.id,
                                getCurrentDateInFormat(),
                                "Withdrawal Approved",
                                notificationData
                            )
                        )


                    }

            }



    }

    fun getCurrentDateInFormat(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        return dateFormat.format(currentDate)
    }

    private fun addNotification(user: User, notificationModel: NotificationModel) {
        lifecycleScope.launch {
            try {

                FCM().sendFCMNotification(
                    user.userdevicetoken,
                    notificationModel.notiTitle,
                    notificationModel.notiData
                )
                notificationViewModel.setNotification(notificationModel).await()

                utils.endLoadingAnimation()


                Toast.makeText(mContext, "Request Approved", Toast.LENGTH_SHORT).show()

                startActivity(Intent(mContext,ActivityHome::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                finish()
            } catch (e: Exception) {
                Toast.makeText(mContext, "Failed to send notification", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    fun getTextFromInvestment(value: String?): String {
        return if (value.isNullOrEmpty()) "0" else value
    }

    private fun approvedInvestment() {



        transactionModel.status=constants.TRANSACTION_STATUS_APPROVED
        transactionModel.transactionAt= Timestamp.now()
        val transactionAmount = transactionModel?.amount?.toInt() ?: 0
        if (investmentModel != null) {

            var inActiveInvestment=0
            if(!investmentModel.lastInvestment.isNullOrEmpty())
                inActiveInvestment = investmentModel.lastInvestment.toInt()


            val newInActiveInvestment = inActiveInvestment + transactionAmount
            investmentModel.lastInvestment = newInActiveInvestment.toString()
            //transactionModel?.newBalance= newInActiveInvestment.toString()
        }


        val newTotal = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()
        transactionModel.newBalance= newTotal.toInt().toString()



        utils.startLoadingAnimation()


        db.collection(constants.INVESTMENT_COLLECTION).document(investmentModel.investorID).set(investmentModel)
            .addOnCompleteListener {
                if(it.isSuccessful){

                    db.collection(constants.TRANSACTION_REQ_COLLECTION).document(transactionModel.id).set(transactionModel).addOnCompleteListener {
                            if(it.isSuccessful){

                                //for notifications
                                val amount = SpannableString(transactionAmount.toString())
                                amount.setSpan(
                                    StyleSpan(Typeface.BOLD),
                                    0,
                                    amount.length,
                                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                                )
                                //for notifications
                                val name = SpannableString(user?.firstName)
                                name.setSpan(
                                    StyleSpan(Typeface.BOLD),
                                    0,
                                    name.length,
                                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                                )

                                val notificationData =
                                    "Dear $name, Your Investment request of  $amount PKR has been approved."
                                addNotification(
                                    user,
                                    NotificationModel(
                                        "",
                                        user!!.id,
                                        getCurrentDateInFormat(),
                                        "Investment Approved",
                                        notificationData
                                    )
                                )





                            }

                    }
                }

            }

    }

    fun setData(){


        utils.startLoadingAnimation()
        transactionModel= TransactionModel.fromString( intent.getStringExtra("transactionModel").toString())!!
        user=User.fromString( intent.getStringExtra("User").toString())!!
        investmentModel= sharedPrefManager.getInvestmentList().find { it.investorID == user.id }!!
        val fa: ModelFA? = if (!user.fa_id.isNullOrEmpty()) { sharedPrefManager.getFAList().find { it.id == user.fa_id } } else { null }

        
        if(intent.getStringExtra("from").toString().equals(constant.FROM_PENDING_WITHDRAW_REQ)){
            binding.tvHeader22.text="Withdraw"
            supportActionBar?.title = "Withdraw Details"
        }

        var transactionModel=TransactionModel.fromString( intent.getStringExtra("transactionModel").toString())
        var user=User.fromString( intent.getStringExtra("User").toString())

        var modelFA=sharedPrefManager.getFAList().find { it.id.equals(user?.fa_id) }

        var senderBankAccount = sharedPrefManager.getAccountList().find { it.docID.equals(transactionModel?.senderAccountID) }
        var receiverBankAccount = sharedPrefManager.getAccountList().find { it.docID.equals(transactionModel?.receiverAccountID) }


        binding.tvInvestorName.text="${user?.firstName} "
        binding.tvInvestorCnic.text="${user?.cnic}"
        binding.investmentDate.text="${SimpleDateFormat( "hh:mm a dd/MM/yy", Locale.getDefault()).format(transactionModel?.createdAt!!.toDate()).toString()}"
        binding.tvInvestment.text="${transactionModel?.amount}"

        Toast.makeText(mContext, ""+senderBankAccount?.bank_name, Toast.LENGTH_SHORT).show()
        binding.tvInvestorBankName.text="${senderBankAccount?.bank_name}"
        binding.tvInvestorAccountNumber.text="${senderBankAccount?.account_number}"
        binding.tvInvestorAccountTittle.text="${senderBankAccount?.account_tittle}"

        binding.tvBankName.text="${receiverBankAccount?.bank_name}"
        binding.tvAccountNumber.text="${receiverBankAccount?.account_number}"
        binding.tvAccountTittle.text="${receiverBankAccount?.account_tittle}"


        if (user != null) {
            Glide.with(mContext)
                .load(user.photo)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background) // Placeholder image while loading
                .into(binding.imgUserProfile)
        }


        if(modelFA!=null){
            binding.tvFAName.text="${modelFA?.firstName} ${modelFA?.lastName}"
            binding.tvDesignation.text="${modelFA?.designantion}"
            Glide.with(mContext)
                .load(modelFA.photo)
                .into(binding.imgFA)
        }
        Thread.sleep(400)
        utils.endLoadingAnimation()

    }


}