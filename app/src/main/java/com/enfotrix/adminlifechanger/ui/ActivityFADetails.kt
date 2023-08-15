package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.Adapters.AdapterFA
import com.enfotrix.adminlifechanger.Adapters.InvestorAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityFadetailsBinding
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.util.Locale

class ActivityFADetails : AppCompatActivity(), InvestorAdapter.OnItemClickListener {

    private lateinit var rvInvestors: RecyclerView
    private lateinit var dialog: BottomSheetDialog
    private var originalFAList: List<User> = emptyList()
    private lateinit var user: User


    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()


    private lateinit var modelFA: ModelFA


    private lateinit var mContext: Context
    private lateinit var binding: ActivityFadetailsBinding


    var constant = Constants()
    private lateinit var utils: Utils
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var adapter: InvestorAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFadetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this@ActivityFADetails
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        supportActionBar?.title = "Financial Advisor Details"

        modelFA = ModelFA.fromString(intent.getStringExtra("FA").toString())!!

        binding.fbAddClient.setOnClickListener {
            showClientDialog()
        }



        getData()



        binding.svClients.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterclients(newText)
                return false
            }
        })




    }

    fun getData() {

        binding.rvClients.layoutManager = LinearLayoutManager(mContext)
        binding.rvClients.adapter = userViewModel.getAssignedInvestorsAdapter(
            modelFA.id,
            constant.FROM_ASSIGNED_FA,
            this@ActivityFADetails
        )
    }



















    fun showClientDialog() {
        dialog = BottomSheetDialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.bottom_sheet_investors)
        rvInvestors = dialog.findViewById<RecyclerView>(R.id.rvInvestors) as RecyclerView
        rvInvestors.layoutManager = LinearLayoutManager(mContext)
        rvInvestors.adapter =
            userViewModel.getInvestorsAdapter(constant.FROM_UN_ASSIGNED_FA, this@ActivityFADetails)
        dialog.show()
        val svFadetail = dialog.findViewById<androidx.appcompat.widget.SearchView>(R.id.svFadetail)
        svFadetail?.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })


    }











    private fun filterclients(text: String) {
        val filteredList = ArrayList<User>()
        if (text.isEmpty() || text.isBlank()) {
            rvInvestors.adapter =
                InvestorAdapter(constants.FROM_ASSIGNED_FA, filteredList, this@ActivityFADetails)
        } else {
            for (User in originalFAList) {
                if (User.cnic.toLowerCase(Locale.getDefault())
                        .contains(text.toLowerCase(Locale.getDefault()))
                ) {
                    filteredList.add(user)
                }
            }

            if (filteredList.isEmpty()) {
                Toast.makeText(mContext, "No Data Found..", Toast.LENGTH_SHORT).show()
            } else {
                rvInvestors.adapter = InvestorAdapter(
                    constants.FROM_ASSIGNED_FA,
                    filteredList,
                    this@ActivityFADetails
                )
            }
        }
    }











    override fun onItemClick(user: User) {
    }

    override fun onAssignClick(user: User) {


        user.fa_id = modelFA.id

        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.setUser(user)
                .addOnCompleteListener { task ->


                    lifecycleScope.launch {
                        userViewModel.getUsers()
                            .addOnCompleteListener { task ->
                                utils.endLoadingAnimation()
                                if (task.isSuccessful) {
                                    val list = ArrayList<User>()
                                    if (task.result.size() > 0) {
                                        for (document in task.result) list.add(
                                            document.toObject(
                                                User::class.java
                                            ).apply { id = document.id })
                                        sharedPrefManager.putUserList(list)
                                        dialog.dismiss()

                                        Toast.makeText(mContext, "Assigned", Toast.LENGTH_SHORT)
                                            .show()
                                        getData()
                                    }
                                } else Toast.makeText(
                                    mContext,
                                    constants.SOMETHING_WENT_WRONG_MESSAGE,
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                            .addOnFailureListener {
                                utils.endLoadingAnimation()
                                dialog.dismiss()

                                Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()

                            }
                    }


                }
                .addOnFailureListener {
                    utils.endLoadingAnimation()
                    dialog.dismiss()
                    Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()

                }


        }


    }


    private fun filter(text: String) {
        val filteredList = ArrayList<User>()
        if (text.isEmpty() || text.isBlank()) {
            rvInvestors.adapter =
                InvestorAdapter(constants.FROM_UN_ASSIGNED_FA, filteredList, this@ActivityFADetails)
        } else {
            for (User in originalFAList) {
                if (User.cnic.toLowerCase(Locale.getDefault())
                        .contains(text.toLowerCase(Locale.getDefault()))
                ) {
                    filteredList.add(user)
                }
            }

            if (filteredList.isEmpty()) {
                Toast.makeText(mContext, "No Data Found..", Toast.LENGTH_SHORT).show()
            } else {
                rvInvestors.adapter = InvestorAdapter(
                    constants.FROM_UN_ASSIGNED_FA,
                    filteredList,
                    this@ActivityFADetails
                )
            }
        }
    }







    override fun onRemoveClick(user: User) {
        Toast.makeText(mContext, "Un Assigned", Toast.LENGTH_SHORT).show()

    }

}