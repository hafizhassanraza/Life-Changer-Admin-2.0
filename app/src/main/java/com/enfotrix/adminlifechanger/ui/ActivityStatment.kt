package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.AdapterInActiveInvestment
import com.enfotrix.adminlifechanger.Adapters.AdapterInvStatment
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityInActiveInvestmentBinding
import com.enfotrix.adminlifechanger.databinding.ActivityStatmentBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class ActivityStatment : AppCompatActivity() {

    private val db = Firebase.firestore

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding : ActivityStatmentBinding

    private lateinit var userlist : List<User>
    private lateinit var transactionList : List<TransactionModel>
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatmentBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mContext=this@ActivityStatment
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvStatment.layoutManager = LinearLayoutManager(mContext)






        setData()




    }

    private fun setData() {

        transactionList = sharedPrefManager.getTransactionList()
            .filter {
                it.type.equals(constants.TRANSACTION_TYPE_INVESTMENT) || it.type.equals(constants.TRANSACTION_TYPE_WITHDRAW)
                it.status.equals(constants.TRANSACTION_STATUS_APPROVED)
            }
            .sortedByDescending { it.createdAt }



        userlist = sharedPrefManager.getUsersList().filter { user -> transactionList.any { it.investorID.equals(user.id) } }
        binding.rvStatment.adapter= AdapterInvStatment(transactionList, userlist)



    }



}