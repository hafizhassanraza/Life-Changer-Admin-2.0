package com.enfotrix.adminlifechanger.ui

import User
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.databinding.ActivityUserStatementBinding
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils

import java.util.Locale

class ActivityUserStatement : AppCompatActivity() {

    private lateinit var user: User



    private lateinit var binding: ActivityUserStatementBinding
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager



    private lateinit var selectedCalendar: Calendar
    private var startFormattedDate = ""
    private var endFormattedDate= ""
    @RequiresApi(Build.VERSION_CODES.N)
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserStatementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext=this@ActivityUserStatement
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        user= User.fromString( intent.getStringExtra("user").toString())!!





        binding.imgBack.setOnClickListener{finish()}

        binding.rvInvestments.layoutManager = LinearLayoutManager(mContext)
        binding.rvInvestments.adapter= investmentViewModel.getStatmentAdapter(user!!.id)


    }
}