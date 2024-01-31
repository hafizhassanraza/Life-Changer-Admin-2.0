package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.API.FCM
import com.enfotrix.adminlifechanger.Adapters.AdapterInActiveInvestment
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.NotificationModel
import com.enfotrix.adminlifechanger.Models.NotificationViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityInActiveInvestmentBinding
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityInActiveInvestment : AppCompatActivity(), AdapterInActiveInvestment.OnItemClickListener {

    private val db = Firebase.firestore

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding: ActivityInActiveInvestmentBinding
    private val notificationViewModel: NotificationViewModel by viewModels()

    private lateinit var userlist: List<User>
    private lateinit var investmentList: List<InvestmentModel>
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInActiveInvestmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this@ActivityInActiveInvestment
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvInvestors.layoutManager = LinearLayoutManager(mContext)

        setData()
    }

    private fun setData() {
        investmentList = sharedPrefManager.getInvestmentList()
            .filter { investment ->
                val inActiveInvestment = investment.lastInvestment.takeIf { !it.isNullOrEmpty() } ?: "0"
                val inActiveInvestment_ = inActiveInvestment.toIntOrNull() ?: 0
                inActiveInvestment_ > 0
            }

        userlist = sharedPrefManager.getUsersList().filter { user ->
            investmentList.any { it.investorID.equals(user.id) }
        }
        binding.rvInvestors.adapter =
            AdapterInActiveInvestment(
                userlist,
                investmentList,
                this@ActivityInActiveInvestment
            )

        binding.svUsers.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })
    }

    private fun filter(text: String) {
        val filteredList = if (text.isBlank()) {
            AdapterInActiveInvestment(
                userlist.filter { it.status.equals(constants.INVESTOR_STATUS_ACTIVE) }
                    .sortedByDescending { it.createdAt },
                investmentList,
                this@ActivityInActiveInvestment
            )
        } else {
            val filteredUsers = userlist.filter { user ->
                user.firstName.toLowerCase(Locale.getDefault())
                    .contains(text.toLowerCase(Locale.getDefault()))
            }
            AdapterInActiveInvestment(
                filteredUsers.filter { it.status.equals(constants.INVESTOR_STATUS_ACTIVE) }
                    .sortedByDescending { it.createdAt },
                investmentList,
                this@ActivityInActiveInvestment
            )
        }

        binding.rvInvestors.adapter = filteredList
    }


    private fun convertInvestment(investmentModel: InvestmentModel) {
        utils.startLoadingAnimation()
        var inActiveInvestment = "0"
        if (!investmentModel.lastInvestment.isNullOrEmpty()) inActiveInvestment =
            investmentModel.lastInvestment
        val activeInvestment = investmentModel.investmentBalance
        val inActiveInvestment_ = inActiveInvestment?.toInt() ?: 0
        val activeInvestment_ = activeInvestment?.toInt() ?: 0
        val newBalance = inActiveInvestment_ + activeInvestment_
        investmentModel.lastInvestment = "0"
        investmentModel.investmentBalance = newBalance.toString()

        db.collection(constants.INVESTMENT_COLLECTION).document(investmentModel.investorID)
            .set(investmentModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    db.collection(constants.INVESTMENT_COLLECTION).get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val User=sharedPrefManager.getUsersList().find { it.id.equals(investmentModel.investorID) }
                                val notificationData =
                                    "Dear ${User?.firstName},Your Inactive Investment of ${inActiveInvestment.toString()} PKR converted to Active Now your Current Active Balance is ${investmentModel.investmentBalance} PKR"
                                if (User != null) {
                                    addNotification(
                                        User,
                                        NotificationModel(
                                            "",
                                            User.id,
                                            getCurrentDateInFormat(),
                                            "Investment Activation",
                                            notificationData
                                        )
                                    )
                                }

                                utils.endLoadingAnimation()
                                sharedPrefManager.putAccountList(it.result.documents.mapNotNull { document ->
                                    document.toObject(ModelBankAccount::class.java)
                                        ?.apply { docID = document.id }
                                })

                                setData()
                            }
                        }
                }
            }
    }

    private fun addNotification(user: User , notificationModel: NotificationModel) {
        lifecycleScope.launch {
            try {
                FCM().sendFCMNotification(
                    user.userdevicetoken,
                    notificationModel.notiTitle,
                    notificationModel.notiData
                )
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

    override fun onItemClick(investment: InvestmentModel) {
        convertInvestment(investment)
    }

    override fun addInvestment(investment: InvestmentModel) {
        // Handle the addition of an investment
    }
}
