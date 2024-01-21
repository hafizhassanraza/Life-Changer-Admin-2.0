package com.enfotrix.adminlifechanger.Fragments

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.databinding.FragmentHomeBinding
import com.enfotrix.adminlifechanger.ui.ActivityAddProfit
import com.enfotrix.adminlifechanger.ui.ActivityFA
import com.enfotrix.adminlifechanger.ui.ActivityInvestmentManager
import com.enfotrix.adminlifechanger.ui.ActivityInvestmentRequest
import com.enfotrix.adminlifechanger.ui.ActivityInvestors
import com.enfotrix.adminlifechanger.ui.ActivityWithdrawRequest
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {


    private val db = Firebase.firestore


    var constant= Constants()




    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root



        binding.btnInvestmentReq.setOnClickListener { startActivity(Intent(requireContext(),ActivityInvestmentRequest::class.java)) }
        binding.btnWithdrawReq.setOnClickListener { startActivity(Intent(requireContext(),ActivityWithdrawRequest::class.java)) }

        binding.layInvestment.setOnClickListener { startActivity(Intent(requireContext(),ActivityInvestmentManager::class.java)) }
        binding.layInvestors.setOnClickListener { startActivity(Intent(requireContext(), ActivityInvestors::class.java)) }
        binding.layProfit.setOnClickListener { startActivity(Intent(requireContext(),ActivityAddProfit::class.java)) }
        binding.layAgent.setOnClickListener { startActivity(Intent(requireContext(),ActivityFA::class.java)) }

        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        getData()

        return root
    }



    private fun getData() {

        val collections = listOf(
            constants.ACCOUNTS_COLLECTION,
            constants.INVESTOR_COLLECTION,
            constants.INVESTMENT_COLLECTION,
            constants.TRANSACTION_REQ_COLLECTION,
            constants.FA_COLLECTION,
            constants.NOMINEE_COLLECTION
        )
        utils.startLoadingAnimation()
        collections.forEach { collection ->
            db.collection(collection)
                .addSnapshotListener { snapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    snapshot?.let { task ->
                        when (collection) {
                            constants.ACCOUNTS_COLLECTION -> sharedPrefManager.putAccountList(task.documents.mapNotNull { document ->
                                document.toObject(ModelBankAccount::class.java)?.apply { docID = document.id }
                            })
                            constants.INVESTOR_COLLECTION -> sharedPrefManager.putUserList(task.documents.mapNotNull { document ->
                                document.toObject(User::class.java)?.apply { id = document.id }
                            })
                            constants.INVESTMENT_COLLECTION -> sharedPrefManager.putInvestmentList(
                                task.documents.mapNotNull { document ->
                                    document.toObject(InvestmentModel::class.java)
                                }
                            )
                            constants.TRANSACTION_REQ_COLLECTION -> sharedPrefManager.putTransactionList(
                                task.documents.mapNotNull { document ->
                                    document.toObject(TransactionModel::class.java)?.apply { id = document.id }
                                }
                            )
                            constants.FA_COLLECTION -> sharedPrefManager.putFAList(task.documents.mapNotNull { document ->
                                document.toObject(ModelFA::class.java)?.apply { id = document.id }
                            })
                            constants.NOMINEE_COLLECTION -> sharedPrefManager.putNomineeList(task.documents.mapNotNull { document ->
                                document.toObject(ModelNominee::class.java)?.apply { docID = document.id }
                            })

                        }
                        // Call endLoading after each snapshot listener completes
                        utils.endLoadingAnimation()
                        setData()

                    }
                }
        }








    }
    private fun setData() {


        utils.startLoadingAnimation()
        Thread.sleep(50)
        utils.endLoadingAnimation()
        var listInvestmentModel= sharedPrefManager.getInvestmentList()
        var listTransaction= sharedPrefManager.getTransactionList()
        val pendingInvestmentCounter = listTransaction.count { it.type == constant.TRANSACTION_TYPE_INVESTMENT && it.status == constant.TRANSACTION_STATUS_PENDING }?.toInt() ?: 0
        val pendingWithdrawCounter = listTransaction.count { it.type == constant.TRANSACTION_TYPE_WITHDRAW && it.status == constant.TRANSACTION_STATUS_PENDING }?.toInt() ?: 0

        binding.tvBalance.text= listInvestmentModel.sumOf { it.investmentBalance.takeIf { it.isNotBlank() }?.toInt() ?: 0 }.toInt().toString()
        binding.tvProfit.text= listInvestmentModel.sumOf { it.lastProfit.takeIf { it.isNotBlank() }?.toInt() ?: 0 }.toInt().toString()
        binding.tvInActiveInvestment.text= listInvestmentModel.sumOf { it.lastInvestment.takeIf { it.isNotBlank() }?.toInt() ?: 0 }.toInt().toString()
        binding.btnInvestmentReq.text= "Investment(${pendingInvestmentCounter})"
        binding.btnWithdrawReq.text= "Withdraw(${pendingWithdrawCounter})"

    }

    //new investor -> nominee get on run time
    //set check for primary account not delete



}