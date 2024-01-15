package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.AdapterNotifications
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.NotificationModel
import com.enfotrix.adminlifechanger.Models.NotificationViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityFaWithdrawRequestBinding
import com.enfotrix.adminlifechanger.databinding.ActivityNotificationBinding
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.adminlifechanger.databinding.DialogAddNotificationBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class ActivityNotification : AppCompatActivity() {
    private val notificationViewModel: NotificationViewModel by viewModels()
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog
    private var notificationsList: MutableList<NotificationModel> = mutableListOf()
    private lateinit var adapter: AdapterNotifications

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Investment Request"

        mContext = this@ActivityNotification
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvNoti.layoutManager = LinearLayoutManager(mContext)
        notificationsList.sortBy { it.createdAt}
        adapter = AdapterNotifications(notificationsList)
        binding.rvNoti.adapter = adapter

        setTitle("")
        user = User.fromString(intent.getStringExtra("user").toString())!!
        Toast.makeText(mContext, "" + user.id, Toast.LENGTH_SHORT).show()

        binding.addbtn.setOnClickListener {
            openAddNotificationDialog()
        }

        getNotificationsList() // Initialize the list when the activity is created
    }

    private fun openAddNotificationDialog() {
        val dialogBinding = DialogAddNotificationBinding.inflate(LayoutInflater.from(mContext))
        val addNotificationDialog = Dialog(mContext)
        addNotificationDialog.setContentView(dialogBinding.root)

        val etTitle = dialogBinding.etBalance
        val etNotificationData = dialogBinding.notiData
        val btnSet = dialogBinding.btnnoti

        btnSet.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val notificationData = etNotificationData.text.toString().trim()

            if (title.isNotEmpty() && notificationData.isNotEmpty()) {
                utils.startLoadingAnimation()
                lifecycleScope.launch {
                    var notiModel = NotificationModel("", user.id, getCurrentDateInFormat(), title, notificationData)
                    notificationViewModel.setNotification(notiModel)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val docId = task.result?.id
                                if (docId != null) {
                                    notiModel.id = docId
                                    lifecycleScope.launch {
                                        FirebaseFirestore.getInstance().collection(constants.NOTIFICATION_COLLECTION).document(docId).set(notiModel)
                                            .addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    utils.endLoadingAnimation()
                                                    Toast.makeText(mContext, "Added", Toast.LENGTH_SHORT).show()
                                                    // Update the RecyclerView when a new notification is added
                                                    getNotificationsList()
                                                } else {
                                                    utils.endLoadingAnimation()
                                                }
                                            }
                                    }

                                }

                            }
                        }


                }


                // Dismiss the dialog
                addNotificationDialog.dismiss()
            } else {
                Toast.makeText(mContext, "Please enter valid data", Toast.LENGTH_SHORT).show()
            }
        }

        addNotificationDialog.show()
    }

    fun getCurrentDateInFormat(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        return dateFormat.format(currentDate)
    }

    private fun getNotificationsList() {
        FirebaseFirestore.getInstance().collection(constants.NOTIFICATION_COLLECTION)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Handle the error
                    Toast.makeText(mContext, "Error fetching notifications", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Clear the previous list to avoid duplicates
                notificationsList.clear()

                // Iterate through the documents and add them to the list
                for (document in snapshot!!) {
                    val notification = document.toObject(NotificationModel::class.java)
                    notification.id = document.id
                    notificationsList.add(notification)
                }

                // Notify the adapter that the dataset has changed
                adapter.notifyDataSetChanged()
            }
    }
}
