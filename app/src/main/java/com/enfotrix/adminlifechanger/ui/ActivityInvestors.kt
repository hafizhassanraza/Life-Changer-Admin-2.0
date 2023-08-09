package com.enfotrix.adminlifechanger.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.enfotrix.adminlifechanger.Adapters.InvestorViewPagerAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityInvestorsBinding
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.material.tabs.TabLayoutMediator

class ActivityInvestors : AppCompatActivity() {

    private lateinit var binding: ActivityInvestorsBinding

    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext=this@ActivityInvestors
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        setTitle("Investors")
        setupViewPager()
        setupTabLayout()    }

    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if(position==0) tab.text ="Active"
            else if(position==1) tab.text="New"
            else if(position==2) tab.text="Blocked"
        }.attach()
    }

    private fun setupViewPager() {
        val adapter = InvestorViewPagerAdapter(this, 3)
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