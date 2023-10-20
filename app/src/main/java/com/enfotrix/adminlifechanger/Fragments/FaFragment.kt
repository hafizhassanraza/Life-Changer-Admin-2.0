package com.enfotrix.adminlifechanger.Fragments

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.AgentTransactionviewModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.FragmentFaBinding
import com.enfotrix.adminlifechanger.databinding.FragmentPendingWithdrawBinding
import com.enfotrix.adminlifechanger.ui.ActivityFaIncome
import com.enfotrix.adminlifechanger.ui.ActivityFaWithdrawRequest
import com.enfotrix.adminlifechanger.ui.ActivityInvestmentRequest
import com.enfotrix.adminlifechanger.ui.ActivityWithdrawRequest
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils

class FaFragment : Fragment() {



    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val agentWithdrawReqDetails: AgentTransactionviewModel by viewModels()
    var constant= Constants()



    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog

    private var _binding: FragmentFaBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFaBinding.inflate(inflater, container, false)
        val root: View = binding.root


        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.layWithdraw.setOnClickListener { startActivity(
            Intent(requireContext(),
                ActivityFaWithdrawRequest::class.java)
        ) }
        binding.layIncome.setOnClickListener { startActivity(
            Intent(requireContext(),
                ActivityFaIncome::class.java)
        ) }

return root
    }

}