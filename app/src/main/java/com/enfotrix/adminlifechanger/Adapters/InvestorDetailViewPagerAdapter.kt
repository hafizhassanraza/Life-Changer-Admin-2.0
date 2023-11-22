package com.enfotrix.adminlifechanger.Adapters

import User
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.enfotrix.adminlifechanger.Fragments.FragmentActiveInvestors
import com.enfotrix.adminlifechanger.Fragments.FragmentAllRecord
import com.enfotrix.adminlifechanger.Fragments.FragmentBlockedInvesters
import com.enfotrix.adminlifechanger.Fragments.FragmentInvestRecord
import com.enfotrix.adminlifechanger.Fragments.FragmentNewInvesters
import com.enfotrix.adminlifechanger.Fragments.FragmentProfitRecord
import com.enfotrix.adminlifechanger.Fragments.FragmentTaxRecord
import com.enfotrix.adminlifechanger.Fragments.FragmentWithdrawRecord

class InvestorDetailViewPagerAdapter (fragmentActivity: FragmentActivity, private var totalCount: Int) : FragmentStateAdapter(fragmentActivity) {
    private var user: User? = null

    // Method to set User object
    fun setUser(user: User) {
        this.user = user
    }
    override fun getItemCount(): Int {
        return totalCount
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val fragment = FragmentInvestRecord()
                fragment.arguments = Bundle().apply {
                    putParcelable("user", user)
                }
                fragment
            }
            1 -> {
                val fragment = FragmentWithdrawRecord()
                fragment.arguments = Bundle().apply {
                    putParcelable("user", user)
                }
                fragment
            }
            2 -> {
                val fragment = FragmentProfitRecord()
                fragment.arguments = Bundle().apply {
                    putParcelable("user", user)
                }
                fragment
            }
            3-> {
                val fragment = FragmentTaxRecord()
                fragment.arguments = Bundle().apply {
                    putParcelable("user", user)
                }
                fragment
            }
            else -> {
                val fragment = FragmentAllRecord()
                fragment.arguments = Bundle().apply {
                    putParcelable("user", user)
                }
                fragment
            }
        }
    }


}
