package com.enfotrix.adminlifechanger.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.enfotrix.adminlifechanger.Fragments.FragmenPendingFaWithdraw
import com.enfotrix.adminlifechanger.Fragments.FragmentApprovedFaWithdraw
import com.enfotrix.adminlifechanger.Fragments.FragmentApprovedWithdraw
import com.enfotrix.adminlifechanger.Fragments.FragmentPendingWithdraw

class FaWithdrawViewPagerAdapter (fragmentActivity: FragmentActivity, private var totalCount: Int) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return totalCount
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmenPendingFaWithdraw()
            1 -> FragmentApprovedFaWithdraw()
            else -> FragmenPendingFaWithdraw()
        }
    }
}