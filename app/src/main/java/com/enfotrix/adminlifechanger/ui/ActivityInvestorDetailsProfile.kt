package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityInvestorDetailsBinding
import com.enfotrix.adminlifechanger.databinding.ActivityInvestorDetailsProfileBinding
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ActivityInvestorDetailsProfile : AppCompatActivity() {


    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()


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
/*

        user= User.fromString( intent.getStringExtra("user").toString())!!

        supportActionBar?.title = user.firstName

        Glide.with(mContext).load(user.photo).centerCrop().placeholder(R.drawable.ic_launcher_background).into(binding.userPhoto);
        binding.tvInvestorName.text = user.firstName
        binding.tvInvestorFatherName.text = user.lastName
        binding.tvInvestorCnic.text = user.cnic
        binding.tvInvestorPhoneNumber.text = user.phone
*/



        /*binding.tvViewDetailsInvestment.setOnClickListener { startActivity(Intent(mContext, ActivityInvestorDetailsTransactions::class.java).putExtra("user",user.toString())) }
        binding.tvViewDetailsUser.setOnClickListener { startActivity(Intent(mContext, ActivityInvestorDetailsProfile::class.java).putExtra("user",user.toString())) }
        binding.layInvest.setOnClickListener {showAddBalanceDialog() }
        binding.layWithdraw.setOnClickListener {showWithdrawBalanceDialog() }
        binding.layTax.setOnClickListener {showTaxBalanceDialog() }
        binding.layProfit.setOnClickListener {showProfitBalanceDialog() }*/



    }
}