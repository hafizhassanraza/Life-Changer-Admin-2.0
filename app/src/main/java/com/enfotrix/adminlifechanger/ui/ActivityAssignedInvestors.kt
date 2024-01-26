package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.Adapters.AdapterFA
import com.enfotrix.adminlifechanger.Adapters.InvestorAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.AgentTransactionModel
import com.enfotrix.adminlifechanger.Models.AgentTransactionviewModel
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Models.NotificationModel
import com.enfotrix.adminlifechanger.Models.NotificationViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityAssignedInvestorsBinding
import com.enfotrix.adminlifechanger.databinding.ActivityFadetailsBinding
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityAssignedInvestors : AppCompatActivity(), InvestorAdapter.OnItemClickListener,
    AdapterFA.OnItemClickListener {

    private lateinit var rvInvestors: RecyclerView
    private lateinit var dialog: BottomSheetDialog
    private var originalFAList: List<User> = emptyList()
    private var originallist: List<User> = emptyList()
    private lateinit var user: User
    private val notificationViewModel: NotificationViewModel by viewModels()


    private val userlist = ArrayList<User>()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val agentTransactionviewModel: AgentTransactionviewModel by viewModels()
    private lateinit var modelFA: ModelFA


    private lateinit var mContext: Context
    private lateinit var binding: ActivityAssignedInvestorsBinding


    var constant = Constants()
    private lateinit var utils: Utils
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var adapter: InvestorAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignedInvestorsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this@ActivityAssignedInvestors
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvClients.layoutManager = LinearLayoutManager(mContext)

        supportActionBar?.title = "Assigned Investors"
        modelFA = ModelFA.fromString(intent.getStringExtra("Fa").toString())!!
        binding.fbAddClient.setOnClickListener {
            showClientDialog()
        }


        getData()


        originalFAList = userViewModel.getusers(modelFA.id)
        originallist = userViewModel.getusers2(modelFA.id)















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

        binding.rvClients.adapter = userViewModel.getAssignedInvestorsAdapter(
            modelFA.id,
            constant.FROM_ASSIGNED_FA,
            this@ActivityAssignedInvestors
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
            userViewModel.getInvestorsAdapter(constant.FROM_UN_ASSIGNED_FA, this@ActivityAssignedInvestors)
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
        // creating a new array list to filter our data.
        val filteredlist = ArrayList<User>()
        if (text.isEmpty() || text.equals("")) {
            binding.rvClients.adapter =
                InvestorAdapter(constants.FROM_ASSIGNED_FA, originalFAList, this@ActivityAssignedInvestors)

        } else {
            for (user in originalFAList) {
                if (user.firstName.toLowerCase(Locale.getDefault())
                        .contains(text.toLowerCase(Locale.getDefault()))
                ) {
                    filteredlist.add(user)
                }
            }
            if (filteredlist.isEmpty()) {
                // if no item is added in filtered list we are
                // displaying a toast message as no data found.
                Toast.makeText(mContext, "No Data Found..", Toast.LENGTH_SHORT).show()
            } else {
                // at last we are passing that filtered
                // list to our adapter class.


                binding.rvClients.adapter = InvestorAdapter(
                    constants.FROM_ASSIGNED_FA,
                    filteredlist,
                    this@ActivityAssignedInvestors
                )

            }
        }
        // running a for loop to compare elements.

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


                                        val Name = SpannableString(user?.firstName)
                                        Name.setSpan(StyleSpan(Typeface.BOLD), 0, Name.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

                                        val notification_data = "Dear $Name, You have been assigned a new Financial Advisor ${modelFA.firstName}"
                                        addNotification(NotificationModel("",  user.id, getCurrentDateInFormat(), "Financial Advisor Assigned", notification_data))
                                        sharedPrefManager.putUserList(list)
                                        dialog.dismiss()
                                        val name = SpannableString(user?.firstName)
                                        name.setSpan(
                                            StyleSpan(Typeface.BOLD),
                                            0,
                                            name.length,
                                            Spannable.SPAN_INCLUSIVE_INCLUSIVE
                                        )


                                   //     val FA=sharedPrefManager.getFAList().find { it.id.equals(user.fa_id) }
                                        val notificationData =
                                            "Dear $name, Your financial advisor, ${modelFA.firstName}, has been removed. We will assign you a new financial advisor soon."
                                        addNotification(
                                            NotificationModel(
                                                "",
                                                user!!.id,
                                                getCurrentDateInFormat(),
                                                "Financial Advisor Unassigned",
                                                notificationData
                                            )
                                        )

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
    private fun addNotification(notificationModel: NotificationModel) {
        lifecycleScope.launch {
            try {
                notificationViewModel.setNotification(notificationModel).await()
                Toast.makeText(mContext, "Notification sent!!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(mContext, "Failed to send notification", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }


    fun getCurrentDateInFormat(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        return dateFormat.format(currentDate)
    }
    private fun filter(text: String) {
        val filteredList = ArrayList<User>()
        if (text.isEmpty() || text.isBlank()) {
            rvInvestors.adapter = InvestorAdapter(
                constants.FROM_UN_ASSIGNED_FA,
                originallist,
                this@ActivityAssignedInvestors
            )
        } else {
            for (user in originallist) {
                if (user.firstName.toLowerCase(Locale.getDefault())
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
                    this@ActivityAssignedInvestors
                )
            }
        }
    }


    override fun onRemoveClick(user: User) {
        user.fa_id = "" // Set the fa_id to empty to indicate unassigned status

        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.setUser(user).addOnCompleteListener { task ->
                lifecycleScope.launch {
                    userViewModel.getUsers().addOnCompleteListener { task ->
                        utils.endLoadingAnimation()
                        if (task.isSuccessful) {
                            val list = ArrayList<User>()
                            if (task.result.size() > 0) {
                                for (document in task.result) list.add(
                                    document.toObject(User::class.java).apply { id = document.id })
                                sharedPrefManager.putUserList(list)

                                val name = SpannableString(user?.firstName)
                                name.setSpan(
                                    StyleSpan(Typeface.BOLD),
                                    0,
                                    name.length,
                                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                                )
                              val FA=sharedPrefManager.getFAList().find { it.id.equals(user.fa_id) }
                                val notificationData =
                                    "Dear $name, Your financial advisor, ${FA?.firstName}, has been removed. We will assign you a new financial advisor soon."
                                addNotification(
                                    NotificationModel(
                                        "",
                                        user!!.id,
                                        getCurrentDateInFormat(),
                                        "Financial Advisor Unassigned",
                                        notificationData
                                    )
                                )

                                Toast.makeText(
                                    mContext,
                                    "Removed from assigned",
                                    Toast.LENGTH_SHORT
                                ).show()
                                getData()
                            }
                        } else {
                            Toast.makeText(
                                mContext,
                                constants.SOMETHING_WENT_WRONG_MESSAGE,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }.addOnFailureListener {
                        utils.endLoadingAnimation()
                        Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                utils.endLoadingAnimation()
                Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onItemClick(modelFA: ModelFA) {
    }


    override fun onDeleteClick(modelFA: ModelFA) {
        TODO("Not yet implemented")
    }



}