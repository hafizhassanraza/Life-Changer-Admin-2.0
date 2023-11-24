package com.enfotrix.adminlifechanger.ui

import User
import android.Manifest
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityInvestorDetailsBinding
import com.enfotrix.adminlifechanger.databinding.ActivityInvestorDetailsProfileBinding
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ActivityInvestorDetailsProfile : AppCompatActivity() {


    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()
    private val PERMISSION_REQUEST_CODE = 1001

    private lateinit var investmentModel: InvestmentModel


    private val db = Firebase.firestore
    private val firebaseStorage = Firebase.storage
    private val storageRef = firebaseStorage.reference

    lateinit var constant: Constants

    private lateinit var binding: ActivityInvestorDetailsProfileBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    var modelNominee=ModelNominee()
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestorDetailsProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityInvestorDetailsProfile
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
        binding.postalAddress.text = user.address


        binding.investorcnicfront.setOnClickListener()
        {

            downloadImageUsingDownloadManager(user.cnic_front)
        }
        binding.investorcnicback.setOnClickListener()
        {

            downloadImageUsingDownloadManager(user.cnic_back)
        }



        setData()


binding.nomineecnicfront.setOnClickListener()
{
    downloadImageUsingDownloadManager(modelNominee.cnic_front)
}
        binding.nomineecnicback.setOnClickListener()
{

    downloadImageUsingDownloadManager(modelNominee.cnic_back)
}

        /*binding.tvViewDetailsInvestment.setOnClickListener { startActivity(Intent(mContext, ActivityInvestorDetailsTransactions::class.java).putExtra("user",user.toString())) }
        binding.tvViewDetailsUser.setOnClickListener { startActivity(Intent(mContext, ActivityInvestorDetailsProfile::class.java).putExtra("user",user.toString())) }
        binding.layInvest.setOnClickListener {showAddBalanceDialog() }
        binding.layWithdraw.setOnClickListener {showWithdrawBalanceDialog() }
        binding.layTax.setOnClickListener {showTaxBalanceDialog() }
        binding.layProfit.setOnClickListener {showProfitBalanceDialog() }*/



    }




    private fun downloadImageUsingDownloadManager(imageUrl: String) {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(imageUrl))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setTitle("Image Download") // Set your desired title here
            .setDescription("Downloading") // Set your desired description here
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Cnic.jpg")

        downloadManager.enqueue(request)
    }
 private fun setData()
    {

        var modelBankAccount=ModelBankAccount()
        lifecycleScope.launch {

            userViewModel.getUserAccounts(user.id)
                .addOnCompleteListener()
                { task->

                    if(task.isSuccessful)
                    {

                        var documents=task.result


                        for (document in documents)
                        {
                            modelBankAccount= document.toObject(ModelBankAccount::class.java)!!
                            binding.account.text=modelBankAccount.account_number
                            binding.accountTitle.text=modelBankAccount.account_tittle
                            binding.bankName.text=modelBankAccount.bank_name
                            break


                        }

                    }
                }



            nomineeViewModel.getNominee(user.id)
                .addOnCompleteListener {task->

                if(task.isSuccessful)
                {

                        modelNominee= task.result.toObject(ModelNominee::class.java)!!
                    binding.nomineename.text=modelNominee.firstName
                    binding.nomineAddress.text=modelNominee.address

                }

                    else
                    Toast.makeText(mContext, constant.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                }

        }




    }





}