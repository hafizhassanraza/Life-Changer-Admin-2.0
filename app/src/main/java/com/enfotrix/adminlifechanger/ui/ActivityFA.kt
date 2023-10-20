package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.AdapterFA
import com.enfotrix.adminlifechanger.Adapters.InvestorAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.databinding.ActivityFaBinding
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import kotlinx.coroutines.launch
import java.util.Locale

class ActivityFA : AppCompatActivity(), AdapterFA.OnItemClickListener {


    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var mContext: Context
    private lateinit var binding: ActivityFaBinding


    private var originalFAList: List<ModelFA> = emptyList()




    var constant = Constants()
    private lateinit var utils: Utils
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this@ActivityFA
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        supportActionBar?.title = "Financial Advisor"

        binding.fbAddFA.setOnClickListener {
            startActivity(Intent(mContext, ActivityAddFA::class.java))
        }
        binding.rvFA.layoutManager = LinearLayoutManager(mContext)
        binding.rvFA.adapter = faViewModel.getFAAdapter(this@ActivityFA)


        originalFAList = faViewModel.getOriginalFAList()
        binding.rvFA.adapter = AdapterFA(originalFAList, this@ActivityFA)

        binding.svAgents.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })

    }


    fun getUser() {
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.getUsers()
                .addOnCompleteListener { task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val list = ArrayList<User>()
                        if (task.result.size() > 0) {
                            for (document in task.result) list.add(
                                document.toObject(User::class.java).apply { id = document.id })
                            sharedPrefManager.putUserList(list)

                        }
                    } else Toast.makeText(
                        mContext,
                        constants.SOMETHING_WENT_WRONG_MESSAGE,
                        Toast.LENGTH_SHORT
                    ).show()

                }
                .addOnFailureListener {
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()

                }
        }




    }


    private fun filter(text: String) {
        val filteredList = ArrayList<ModelFA>()

        if (text.isEmpty() || text.isBlank()) {
            // If the search query is empty or blank, show the entire original data
            binding.rvFA.adapter = AdapterFA(originalFAList, this@ActivityFA)
        } else {
            // Filter the original data based on the search query
            for (modelFA in originalFAList) {
                if (modelFA.firstName.toLowerCase(Locale.getDefault())
                        .contains(text.toLowerCase(Locale.getDefault()))
                ) {
                    filteredList.add(modelFA)
                }
            }

            if (filteredList.isEmpty()) {
                // If no matching data found, show a message
                Toast.makeText(mContext, "No Data Found..", Toast.LENGTH_SHORT).show()
            } else {
                // Update the RecyclerView with the filtered list
                binding.rvFA.adapter =AdapterFA(filteredList, this@ActivityFA)
            }
        }
    }


    fun getAccount() {
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.getAccounts()
                .addOnCompleteListener { task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val list = ArrayList<ModelBankAccount>()
                        if (task.result.size() > 0) {
                            for (document in task.result) list.add(
                                document.toObject(
                                    ModelBankAccount::class.java
                                ).apply { docID = document.id })
                            sharedPrefManager.putAccountList(list)

                        }
                    } else Toast.makeText(
                        mContext,
                        constants.SOMETHING_WENT_WRONG_MESSAGE,
                        Toast.LENGTH_SHORT
                    ).show()

                }
                .addOnFailureListener {
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()

                }
        }
    }


    override fun onItemClick(modelFA: ModelFA) {


        startActivity(
            Intent(mContext, ActivityFADetails::class.java).putExtra(
                "FA",
                modelFA.toString()
            )
        )

    }

    override fun onDeleteClick(modelFA: ModelFA) {
    }


}

