package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.enfotrix.adminlifechanger.Adapters.InvestorDetailViewPagerAdapter
import com.enfotrix.adminlifechanger.Adapters.InvestorViewPagerAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityInvestorDetailsProfileBinding
import com.enfotrix.adminlifechanger.databinding.ActivityInvestorDetailsTransactionsBinding
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ActivityInvestorDetailsTransactions : AppCompatActivity() {


    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()


    private lateinit var investmentModel: InvestmentModel


    private val db = Firebase.firestore
    private val firebaseStorage = Firebase.storage
    private val storageRef = firebaseStorage.reference

    var constant= Constants()

    private lateinit var binding: ActivityInvestorDetailsTransactionsBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityInvestorDetailsTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        user= User.fromString( intent.getStringExtra("user").toString())!!
        setupViewPager()
        setupTabLayout()


    }



    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if(position==0) tab.text ="Invest Record"
            else if(position==1) tab.text="Withdraw Record"
            else if(position==2) tab.text="Profit Record"
            else if(position==3) tab.text="Tax Record"
            else if(position==4) tab.text="All Records"
        }.attach()
    }

    private fun setupViewPager() {
        val adapter = InvestorDetailViewPagerAdapter(this, 5).apply {
            // Pass the User object to fragments
            setUser(user)

        }
        binding.viewPager.adapter = adapter
    }

        override fun onBackPressed() {
            val viewPager = binding.viewPager
            if (viewPager.currentItem == 0) {
                // If the user is currently looking at the first step, allow the system to handle the
                // Back button. This calls finish() on this activity and pops the back stack.
                super.onBackPressed()
            } else {
                // Otherwise, select the previous step.
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }
    }


