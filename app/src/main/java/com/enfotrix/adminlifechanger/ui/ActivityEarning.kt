package com.enfotrix.adminlifechanger.ui

import android.app.Dialog
import android.content.Context
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.API.FCM
import com.enfotrix.adminlifechanger.Adapters.AdapterEarning
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.ModelEarning
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Models.NotificationModel
import com.enfotrix.adminlifechanger.Models.NotificationViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityEarningBinding

import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityEarning : AppCompatActivity(), AdapterEarning.OnItemClickListener {


    private val db = Firebase.firestore

    private val notificationViewModel: NotificationViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding: ActivityEarningBinding

    private lateinit var earningList: List<ModelEarning>
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog
    private lateinit var modelFA: ModelFA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEarningBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mContext = this@ActivityEarning
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvStatment.layoutManager = LinearLayoutManager(mContext)




        modelFA = ModelFA.fromString(intent.getStringExtra("Fa").toString())!!
        binding.fbAddEarning.setOnClickListener { addEarningDialog() }

        setData()


    }

    private fun setData() {
        earningList =
            sharedPrefManager.getAgentEarningList().filter { it.agentID.equals(modelFA.id) }
                .sortedByDescending { it.createdAt }
        binding.rvStatment.adapter = AdapterEarning(earningList, this@ActivityEarning)
    }


    override fun onItemClick(modelEarning: ModelEarning) {
        dialogEarningDetails(modelEarning)
    }

    private fun dialogEarningDetails(modelEarning: ModelEarning) {

//
//            val dialog = Dialog(mContext)
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            dialog.setContentView(R.layout.dialog_earning_details)

        var dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_earning_details)

        // Initialize the dialog views
        val tvHeaderBank = dialog.findViewById<TextView>(R.id.tvHeaderBank)
        val tvHeaderBankDisc = dialog.findViewById<TextView>(R.id.tvHeaderBankDisc)
        val oldBalance = dialog.findViewById<TextView>(R.id.oldBalance)
        val earningAmount = dialog.findViewById<TextView>(R.id.earningAmount)
//        val clearance = dialog.findViewById<TextView>(R.id.clearance)
        val date = dialog.findViewById<TextView>(R.id.date)
        val remarks = dialog.findViewById<TextView>(R.id.remarks)

        // Set data to the dialog views
        oldBalance.text = "${modelEarning.balance} PKR"
        earningAmount.text = " ${modelEarning.amount} PKR"
        date.text= SimpleDateFormat("dd-MM-yy", Locale.getDefault()).format(modelEarning.createdAt.toDate())
        remarks.text = "${modelEarning.disc}"

        dialog.show()


    }


    fun getData() {


        db.collection(constants.FA_COLLECTION).document(modelFA.id)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                documentSnapshot?.let { snapshot ->
                    if (snapshot.exists()) {
                        modelFA = snapshot.toObject<ModelFA>()!!

                        // Update the shared preference with the modified modelFA
                        sharedPrefManager.putFAList(
                            sharedPrefManager.getFAList().map {
                                if (it.id == modelFA.id) modelFA else it
                            }
                        )


                    }


                }
            }


    }


    fun addEarningDialog() {

        dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_profit)

        val etBalance = dialog.findViewById<EditText>(R.id.etBalance)
        val etRemarks = dialog.findViewById<EditText>(R.id.etRemarks)
        val AddProfit = dialog.findViewById<Button>(R.id.AddProfit)
        AddProfit.setOnClickListener {


            if (etBalance.text.isNullOrEmpty()) Toast.makeText(
                mContext,
                "Please enter the amount",
                Toast.LENGTH_SHORT
            ).show()
            else if (etRemarks.text.isNullOrEmpty()) Toast.makeText(
                mContext,
                "Please enter the remarks",
                Toast.LENGTH_SHORT
            ).show()
            else {
                val modelEarning = ModelEarning(
                    "",
                    etBalance.text.toString(),
                    modelFA.profit,
                    etRemarks.text.toString(),
                    modelFA.id,
                    constants.EARNING_STATUS_PENDING
                )
                addEarning(modelEarning)
                val name = SpannableString(modelFA.firstName)
                name.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    name.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )

                val earning = SpannableString(modelEarning.amount)
                earning.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    earning.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )

                val profit = SpannableString(modelFA.profit)
                profit.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    profit.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )

                val notificationData =
                    "Dear $name, You have been credited with an amount of $earning PKR, resulting in a new balance of $profit PKR"
                addNotification(
                    NotificationModel(
                        "",
                        modelFA.id,
                        getCurrentDateInFormat(),
                        "Earning credited",
                        notificationData
                    )
                )

                dialog.dismiss()

            }
        }
        dialog.show()

    }

    private fun addNotification(notificationModel: NotificationModel) {
        lifecycleScope.launch {
            try {
                notificationViewModel.setNotification(notificationModel).await()
                modelFA?.devicetoken?.let {
                    FCM().sendFCMNotification(
                        it,
                        notificationModel.notiTitle,
                        notificationModel.notiData
                    )
                }
                Toast.makeText(mContext, "Notification sent!!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(mContext, "Failed to send notification", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    fun addEarning(modelEarning: ModelEarning) {
        utils.startLoadingAnimation()

        var profit = modelEarning.amount.toString()?.toInt() ?: 0
        var previousBalance = 0
        if (modelFA.profit != "" || modelFA.profit != null) previousBalance = modelFA.profit.toInt()
        profit += previousBalance
        modelFA.profit = profit.toInt().toString()

        var documentRef = db.collection(constants.AGENT_EARNING_COLLECTION).document()
        modelEarning.docID = documentRef.id
        documentRef.set(modelEarning).addOnCompleteListener {
            if (it.isSuccessful) {
                db.collection(constants.FA_COLLECTION).document(modelFA.id).set(modelFA)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            db.collection(constants.AGENT_EARNING_COLLECTION).get()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Thread.sleep(200)
                                        utils.endLoadingAnimation()
                                        sharedPrefManager.putAgentEarningList(task.result!!.documents.mapNotNull { document ->
                                            document.toObject(
                                                ModelEarning::class.java
                                            )?.apply { docID = document.id }
                                        })


                                        sharedPrefManager.putFAList(
                                            sharedPrefManager.getFAList().map {
                                                if (it.id == modelFA.id) modelFA else it
                                            }
                                        )
                                        getData()
                                        setData()

                                    }
                                }
                        }
                    }

            } else Toast.makeText(mContext, "" + it.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    fun getCurrentDateInFormat(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        return dateFormat.format(currentDate)
    }

}