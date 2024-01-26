package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.API.FCM
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

        setTitle("")
        user = User.fromString(intent.getStringExtra("user").toString())!!

        binding.addbtn.setOnClickListener {
            openAddNotificationDialog()
        }

        setData()
        //getNotificationsList() // Initialize the list when the activity is created
    }

    private fun setData() {

        val sortedNotifications = sharedPrefManager.getNotificationList()
            .filter { it.userID.equals(user.id) }
            .sortedByDescending { it.createdAt }

        binding.rvNoti.adapter = AdapterNotifications(sortedNotifications)

    }

    private fun openAddNotificationDialog() {
        val dialogBinding = DialogAddNotificationBinding.inflate(LayoutInflater.from(mContext))
        val addNotificationDialog = Dialog(mContext)
        addNotificationDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addNotificationDialog.setContentView(dialogBinding.root)


        val etTitle = dialogBinding.etBalance
        val etNotificationData = dialogBinding.notiData
        val btnSet = dialogBinding.btnnoti

        btnSet.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val notificationData = etNotificationData.text.toString().trim()

            if (title.isNotEmpty() && notificationData.isNotEmpty()) {

                AddNotification(NotificationModel("", user.id, getCurrentDateInFormat(), title, notificationData))

                // Dismiss the dialog
                addNotificationDialog.dismiss()
            } else {
                Toast.makeText(mContext, "Please enter valid data", Toast.LENGTH_SHORT).show()
            }
        }

        addNotificationDialog.show()
    }
    fun AddNotification(notificationModel: NotificationModel){
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            notificationViewModel.setNotification(notificationModel)
                .addOnSuccessListener { task ->

                    FCM().sendFCMNotification(
                        user.userdevicetoken,
                        notificationModel.notiTitle,
                        notificationModel.notiData
                    )

                    FirebaseFirestore.getInstance().collection(constants.NOTIFICATION_COLLECTION)
                        .addSnapshotListener { snapshot, firebaseFirestoreException ->
                            firebaseFirestoreException?.let {
                                Toast.makeText(mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                                return@addSnapshotListener
                            }
                            snapshot?.let { task ->
                                utils.endLoadingAnimation()

                                sharedPrefManager.putNotification(task.documents.mapNotNull { document -> document.toObject(NotificationModel::class.java)?.apply { id = document.id } })
                                setData()
                            }

                        }

                }


        }
    }

    fun getCurrentDateInFormat(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        return dateFormat.format(currentDate)
    }


}
