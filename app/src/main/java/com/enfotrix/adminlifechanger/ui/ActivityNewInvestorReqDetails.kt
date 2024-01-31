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
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.API.FCM
import com.enfotrix.adminlifechanger.Adapters.AdapterFA
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Models.NotificationModel
import com.enfotrix.adminlifechanger.Models.NotificationViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityNewInvestorReqDetailsBinding
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date

class ActivityNewInvestorReqDetails : AppCompatActivity(), AdapterFA.OnItemClickListener {

    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val notificationViewModel: NotificationViewModel by viewModels()


    private var faSelector: Boolean = false
    private var faID: String? = null
    private lateinit var investmentModel: InvestmentModel


    private val db = Firebase.firestore
    private val firebaseStorage = Firebase.storage
    private val storageRef = firebaseStorage.reference

    var constant = Constants()


    private lateinit var binding: ActivityNewInvestorReqDetailsBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewInvestorReqDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mContext = this@ActivityNewInvestorReqDetails
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        //Toast.makeText(mContext, sharedPrefManager.getNomineeList().size.toString(), Toast.LENGTH_SHORT).show()
        //Toast.makeText(mContext, sharedPrefManager.getAccountList().size.toString(), Toast.LENGTH_SHORT).show()
        //Toast.makeText(mContext, sharedPrefManager.getFAList().size.toString(), Toast.LENGTH_SHORT).show()

        user = User.fromString(intent.getStringExtra("user").toString())!!



        supportActionBar?.title = user.firstName


        binding.btnFAAssigned.setOnClickListener { showFADialog() }
        binding.btnApprove.setOnClickListener {

            if (faSelector) approve()
            else Toast.makeText(
                mContext,
                "Please Select the Financial Advisor!",
                Toast.LENGTH_SHORT
            ).show()
        }



        setData(user)

    }


    fun setData(user: User) {

        var nominee = sharedPrefManager.getNomineeList().find { it.nominator.equals(user.id) }
        binding.tvInvestorName.text = user.firstName
        binding.tvInvestorFatherName.text = user.lastName
        binding.tvInvestorCnic.text = user.cnic
        binding.tvInvestorPhoneNumber.text = user.phone
        binding.tvInvestorAddress.text = user.address
        binding.tvNomineeAddress.text = nominee?.address
        binding.tvNomineeCNIC.text = nominee?.cnic
        binding.tvNomineeBankName.text = nominee?.bank_name
        binding.tvNomineeName.text = nominee?.firstName
        binding.tvNomineePhone.text = nominee?.phone
        binding.tvNomineeBankAccountNumber.text = nominee?.acc_number
        binding.tvNomineeBankAccountTittle.text = nominee?.acc_tittle
        binding.tvNomineeFatherName.text = nominee?.lastName
        binding.tvHeader1.text = "Investor's Nominee (${nominee?.nominator_relation})"


    }


    fun approve() {


        user.status = constant.INVESTOR_STATUS_ACTIVE
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.setUser(user)
                .addOnCompleteListener { task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val faName =
                            sharedPrefManager.getFAList().find { it.id.equals(faID) }?.firstName
                        val fa = SpannableString(faName)
                        fa.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            fa.length,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE
                        )
                        val token=sharedPrefManager.getFAList().find { it.id.equals(faID) }
                        val notificationData =
                            "Dear $fa, You have been assigned as the financial advisor for a new investor Mr.${user.firstName}"
                        faID?.let {
                            NotificationModel(
                                "",
                                it,
                                getCurrentDateInFormat(),
                                "Client Assigned",
                                notificationData
                            )

                        }?.let { addNotification(it,token) }

//                        val notificationData_ =
//                            "Dear ${user.firstName}, your account request has been approved."
//                        addNotification(
//                            NotificationModel(
//                                "",
//                                user.id,
//                                getCurrentDateInFormat(),
//                                "Account Verification",
//                                notificationData_
//                            )
//                        )
//

                        Toast.makeText(mContext, "Investor Approved", Toast.LENGTH_SHORT).show()
                        Toast.makeText(mContext, "Notification Sent", Toast.LENGTH_SHORT).show()

                        startActivity(
                            Intent(
                                mContext,
                                ActivityHome::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        finish()


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

    private fun addNotification(notificationModel: NotificationModel, token: ModelFA?) {
        lifecycleScope.launch {
            try {
                notificationViewModel.setNotification(notificationModel).await()
                token?.devicetoken?.let {
                    FCM().sendFCMNotification(
                        it,
                        notificationModel.notiTitle,
                        notificationModel.notiData
                    )
                }


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


    fun showFADialog() {

        var rvFA: RecyclerView

        dialog = BottomSheetDialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.bottom_sheet_investors)

        rvFA = dialog.findViewById<RecyclerView>(R.id.rvInvestors) as RecyclerView
        rvFA.layoutManager = LinearLayoutManager(mContext)
        rvFA.adapter = faViewModel.getFAAdapter(this@ActivityNewInvestorReqDetails)

        dialog.show()

    }


    override fun onItemClick(modelFA: ModelFA) {


        //binding.imgFAProfile
        binding.tvFAName.text = modelFA.firstName + " " + modelFA.lastName
        binding.tvDesignation.text = modelFA.designantion
        binding.layAssigned.setVisibility(View.VISIBLE)
        binding.layUnAssigned.setVisibility(View.GONE)
        user.fa_id = modelFA.id
        faID = modelFA.id
        faSelector = true
        dialog.dismiss()


    }

    override fun onDeleteClick(modelFA: ModelFA) {

    }

}