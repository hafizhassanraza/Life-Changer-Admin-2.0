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
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.AgentTransactionviewModel
import com.enfotrix.adminlifechanger.Models.AgentWithdrawModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.FragmentFragmenPendingFaWithdrawBinding
import com.enfotrix.adminlifechanger.ui.ActivityAgentWithdrawReqDetails
import com.enfotrix.lifechanger.Adapters.AgentTransactionsAdapter
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import kotlinx.coroutines.launch

class FragmentApprovedFaWithdraw : Fragment() ,AgentTransactionsAdapter.OnItemClickListener{

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


    private var _binding: FragmentFragmenPendingFaWithdrawBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFragmenPendingFaWithdrawBinding.inflate(inflater, container, false)
        val root: View = binding.root


        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)






        binding.rvWithdrawRequests.layoutManager = LinearLayoutManager(mContext)

       // getFaWithdraw()

        return root
    }
    /*fun getFaWithdraw()
    {
        lifecycleScope.launch{
            agentWithdrawReqDetails.getPendingWithdrawsAgentReq()
                .addOnCompleteListener{task ->
                    if (task.isSuccessful) {
                        utils.endLoadingAnimation()
                        val list = ArrayList<AgentWithdrawModel>()
                        if(task.result.size()>0){
                            for (document in task.result) {
                                if(document.toObject(AgentWithdrawModel::class.java).status==constant.TRANSACTION_STATUS_APPROVED)
                                {
                                    var agentWithdrawModel= document.toObject(AgentWithdrawModel::class.java)
                                    list.add(agentWithdrawModel)

                                }
                            }
                            binding.rvWithdrawRequests.adapter= AgentTransactionsAdapter(
                                constant.FROM_APPROVED_WITHDRAW_REQ,
                                list.sortedByDescending { it.lastWithdrawReqDate },
                                sharedPrefManager.getFAList(),
                                this@FragmentApprovedFaWithdraw)
                            getAccount()
                        }
                    }
                    else {
                        utils.endLoadingAnimation()
                        Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                    }

                }
                .addOnFailureListener{
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()
                }

        }
    }*/
    fun getAccount(){
        lifecycleScope.launch{
            userViewModel.getAccounts()
                .addOnCompleteListener{task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val list = ArrayList<ModelBankAccount>()
                        if(task.result.size()>0){
                            for (document in task.result)list.add(document.toObject(ModelBankAccount::class.java).apply { docID = document.id })
                            sharedPrefManager.putAccountList(list)
                            //getNominees()
                        }
                    }
                    else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener{
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                }
        }
    }

    override fun onAgentItemClick(agentWithdrawModel: AgentWithdrawModel, modelFA: ModelFA) {

    }


}