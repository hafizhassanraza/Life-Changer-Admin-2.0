package com.enfotrix.adminlifechanger.ui

import User
import android.app.Activity
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
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.API.FCM
import com.enfotrix.adminlifechanger.Adapters.AdapterFA
import com.enfotrix.adminlifechanger.Adapters.InvestorAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.AgentTransactionviewModel
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Models.NotificationModel
import com.enfotrix.adminlifechanger.Models.NotificationViewModel
import com.enfotrix.adminlifechanger.Pdf.PdfUsers
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityAssignedInvestorsBinding

import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
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
    private var InvestorsList: List<User>? = null


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
        getData()
        originalFAList = userViewModel.getusers(modelFA.id)
        originallist = userViewModel.getusers2(modelFA.id)

        binding.fbAddClient.setOnClickListener {
            showClientDialog()
        }
        binding.pdfAssignedInvestors.setOnClickListener {
            InvestorsList = sharedPrefManager.getUsersList()
                .filter { it.status == constants.INVESTOR_STATUS_ACTIVE && it.fa_id == modelFA.id }
            Toast.makeText(mContext, "size "+InvestorsList!!.size, Toast.LENGTH_SHORT).show()
            generatePDF()
        }






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







    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "${modelFA.firstName} Assigned Clients.pdf")
        }
        startActivityForResult(intent, 123)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val outputStream = mContext.contentResolver.openOutputStream(uri)
                if (outputStream != null) {

                    val success =
                        InvestorsList?.let {
                            PdfUsers(it).generatePdf(
                                outputStream
                            )
                        }
                    outputStream.close()
                    if (success == true) {
                        Toast.makeText(mContext, "Saved successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(mContext, "Failed to save", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
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
            userViewModel.getInvestorsAdapter(
                constant.FROM_UN_ASSIGNED_FA,
                this@ActivityAssignedInvestors
            )
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
        val filteredList = if (text.isEmpty() || text.isBlank()) {
            originalFAList
        } else {
            originalFAList.filter { user ->
                user.firstName.toLowerCase(Locale.getDefault())
                    .contains(text.toLowerCase(Locale.getDefault()))
            }
        }
        binding.rvClients.adapter = InvestorAdapter(
            constants.FROM_ASSIGNED_FA,
            filteredList,
            this@ActivityAssignedInvestors
        )
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
                                        Name.setSpan(
                                            StyleSpan(Typeface.BOLD),
                                            0,
                                            Name.length,
                                            Spannable.SPAN_INCLUSIVE_INCLUSIVE
                                        )

                                        val notification_data =
                                            "Dear ${modelFA.firstName}, You have been assigned a new investor  ${modelFA.firstName}"
                                        addNotification(
                                            NotificationModel(
                                                "",
                                                modelFA.id,
                                                getCurrentDateInFormat(),
                                                "Investor Assigned",
                                                notification_data
                                            )
                                        )
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
//                                        val notificationData =
//                                            "Dear $name, Your financial advisor, ${modelFA.firstName}, has been removed. We will assign you a new financial advisor soon."
//                                        addNotification(
//                                            NotificationModel(
//                                                "",
//                                                user!!.id,
//                                                getCurrentDateInFormat(),
//                                                "Financial Advisor Unassigned",
//                                                notificationData
//                                            ),2
//                                        )

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

                FCM().sendFCMNotification(
                    modelFA.devicetoken,
                    notificationModel.notiTitle,
                    notificationModel.notiData
                )

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

        val filteredList = if (text.isBlank()) {
            InvestorAdapter(
                constants.FROM_UN_ASSIGNED_FA,
                originallist,
                this@ActivityAssignedInvestors
            )
        } else {
            val filteredUsers = originallist.filter { user ->
                user.firstName.toLowerCase(Locale.getDefault())
                    .contains(text.toLowerCase(Locale.getDefault()))
            }
            InvestorAdapter(
                constants.FROM_UN_ASSIGNED_FA,
                filteredUsers,
                this@ActivityAssignedInvestors
            )
        }


        rvInvestors.adapter = filteredList
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