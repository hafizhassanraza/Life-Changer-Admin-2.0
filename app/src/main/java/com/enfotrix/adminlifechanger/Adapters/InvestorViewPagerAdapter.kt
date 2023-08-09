package com.enfotrix.adminlifechanger.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.enfotrix.adminlifechanger.Fragments.FragmentActiveInvestors
import com.enfotrix.adminlifechanger.Fragments.FragmentBlockedInvesters
import com.enfotrix.adminlifechanger.Fragments.FragmentNewInvesters

class InvestorViewPagerAdapter (fragmentActivity: FragmentActivity, private var totalCount: Int) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return totalCount
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentActiveInvestors()
            1 -> FragmentNewInvesters()
            else -> FragmentBlockedInvesters()
        }
    }
}
