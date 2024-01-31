package com.enfotrix.adminlifechanger.ui

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.API.FCM
import com.enfotrix.adminlifechanger.Adapters.AdapterNotifications
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Models.NotificationModel
import com.enfotrix.adminlifechanger.Models.NotificationViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityNotificationAgentBinding
import com.enfotrix.adminlifechanger.databinding.ActivityNotificationBinding
import com.enfotrix.adminlifechanger.databinding.DialogAddNotificationBinding
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class ActivityNotificationAgent : AppCompatActivity() {

    private val notificationViewModel: NotificationViewModel by viewModels()
    private lateinit var binding: ActivityNotificationAgentBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog
    private var notificationsList: MutableList<NotificationModel> = mutableListOf()
    private lateinit var adapter: AdapterNotifications

    private lateinit var modelFA: ModelFA

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityNotificationAgentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this@ActivityNotificationAgent
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvNoti.layoutManager = LinearLayoutManager(mContext)
        notificationsList.sortBy { it.createdAt}
        adapter = AdapterNotifications(notificationsList)
        binding.rvNoti.adapter = adapter
        setTitle("Notifications")


        modelFA = ModelFA.fromString(intent.getStringExtra("Fa").toString())!!

        binding.addbtn.setOnClickListener {
            openAddNotificationDialog()
        }

        setData()

    }

    private fun setData() {

        binding.rvNoti.adapter = AdapterNotifications(sharedPrefManager.getNotificationList().sortedBy { it.createdAt }.filter { it.userID.equals(modelFA.id) })

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

                AddNotification(NotificationModel("", modelFA.id, getCurrentDateInFormat(), title, notificationData))


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

                    modelFA?.devicetoken?.let {
                        FCM().sendFCMNotification(
                            it,
                            notificationModel.notiTitle,
                            notificationModel.notiData
                        )
                    }
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