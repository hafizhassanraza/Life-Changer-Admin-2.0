package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.enfotrix.adminlifechanger.Adapters.AdapterFA
import com.enfotrix.adminlifechanger.Adapters.InvestorAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.AgentTransactionModel
import com.enfotrix.adminlifechanger.Models.AgentTransactionviewModel
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.ModelEarning
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityFadetailsBinding
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class ActivityFADetails : AppCompatActivity(), InvestorAdapter.OnItemClickListener, AdapterFA.OnItemClickListener {


    private val db = Firebase.firestore

    private lateinit var rvInvestors: RecyclerView
    private lateinit var dialog: BottomSheetDialog
    private var originalFAList: List<User> = emptyList()
    private var originallist: List<User> = emptyList()
    private lateinit var user: User

    private val userlist = ArrayList<User>()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val agentTransactionviewModel: AgentTransactionviewModel by viewModels()
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

        binding.editfa.setOnClickListener {
            startActivity(Intent(mContext,ActivityEditFA::class.java).putExtra("FA",modelFA.toString()))
        }

        Glide.with(mContext).load(modelFA.photo).centerCrop().placeholder(R.drawable.ic_launcher_background).into(binding.userPhoto)

        binding.tvName.text= modelFA.firstName
        binding.tvDesignantion.text= modelFA.designantion
        binding.tvEarning.text=modelFA.profit



        /*binding.fbAddClient.setOnClickListener {
            showClientDialog()
        }*/
        binding.layWithdraw.setOnClickListener() {
            //WithdrawEarnings()
            startActivity(Intent(mContext,ActivityAgentWithdraw::class.java).putExtra("Fa",modelFA.toString()))

        }
        binding.layInvest.setOnClickListener() {
            startActivity(Intent(mContext,ActivityAssignedInvestors::class.java).putExtra("Fa",modelFA.toString()))
        }
        binding.layEarning.setOnClickListener() {

            startActivity(Intent(mContext,ActivityEarning::class.java).putExtra("Fa",modelFA.toString()))

        }
        binding.layNotification.setOnClickListener() {

            startActivity(Intent(mContext,ActivityNotificationAgent::class.java).putExtra("Fa",modelFA.toString()))




        }



        getData()
        setdata()

        originalFAList = userViewModel.getusers(modelFA.id)
        originallist = userViewModel.getusers2(modelFA.id)


    }

    fun WithdrawEarnings(){


        // with draw balance from user profile
        //  change the the status of pending transactions
        // getData

        utils.startLoadingAnimation()


        CoroutineScope(Dispatchers.IO).launch {
            var earningList = sharedPrefManager.getAgentEarningList().filter { it.agentID == modelFA.id }

            if (earningList.isNotEmpty()) {
                var pendingEarningsList = earningList.filter { it.status == constants.EARNING_STATUS_PENDING }

                if (pendingEarningsList.isNotEmpty()) {
                    for (earning in pendingEarningsList) {
                        earning.status = constants.EARNING_STATUS_WITHDRAW
                        earning.withdrawAt=Timestamp.now()
                        db.collection(constants.AGENT_EARNING_COLLECTION).document(earning.docID).set(earning).await()
                    }

                    modelFA.profit="0"
                    db.collection(constants.FA_COLLECTION).document(modelFA.id).set(modelFA)



                    withContext(Dispatchers.Main) {
                        getData()
                        utils.endLoadingAnimation()

                        Toast.makeText(mContext, "Withdraw successfully", Toast.LENGTH_SHORT).show()
                    }
                }
                else utils.endLoadingAnimation()
            }
            else utils.endLoadingAnimation()

        }





    }


    fun showProfitDialog() {
        var agentTransactionModel=AgentTransactionModel()

        var list=ArrayList<AgentTransactionModel>()

        lifecycleScope.launch {
            agentTransactionviewModel.getAgentTransaction()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<AgentTransactionModel>()

                        for (document in task.result) {
                            val transactionModel = document.toObject(AgentTransactionModel::class.java)
                            if (transactionModel.fa_id == modelFA.id && transactionModel.type == constants.PROFIT_TYPE) {
                                list.add(transactionModel)
                            }
                        }

                        // Sort the list by transactionAt in descending order
                        val sortedList = list.sortedByDescending { it.transactionAt }

                        // Get the first element, which is the latest transaction
                        if (sortedList.isNotEmpty()) {
                            agentTransactionModel = sortedList[0]
                            Toast.makeText(mContext, "${agentTransactionModel?.previousBalance} ${agentTransactionModel?.newBalance}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(mContext, constant.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                }
        }


        var dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_profit)

        val profit = dialog.findViewById<EditText>(R.id.etBalance)
        val remarks = dialog.findViewById<EditText>(R.id.etRemarks)
        val addProfit = dialog.findViewById<Button>(R.id.AddProfit)
        addProfit.setOnClickListener {
            var amount=profit.text.toString()


            /*    for(listagent in listagentTransactionModel)
                {
                    Toast.makeText(mContext, ""+listagent.newBalance, Toast.LENGTH_SHORT).show()
                }*/
            var oldbalance=agentTransactionModel.newBalance
            agentTransactionModel.salary=profit.text.toString()
            agentTransactionModel.newBalance=(amount.toInt()+agentTransactionModel.newBalance.toInt()).toString()
            agentTransactionModel.previousBalance=oldbalance.toString()
            agentTransactionModel.remarks=remarks.text.toString()
            agentTransactionModel.fa_id=modelFA.id
            agentTransactionModel.receiverAccountID=modelFA.id
            agentTransactionModel.amount=profit.text.toString()
            agentTransactionModel.type=constant.PROFIT_TYPE
            agentTransactionModel.status=constant.TRANSACTION_STATUS_APPROVED
            agentTransactionModel.senderAccountID=sharedPrefManager.getToken()
            agentTransactionModel.transactionAt=Timestamp.now()
            lifecycleScope.launch {
                faViewModel.addFaProfit(
                    agentTransactionModel
                )
                    .observe(this@ActivityFADetails)
                    { isSuccess ->
                        if (isSuccess) {
                            Toast.makeText(
                                mContext,
                                "Profit Added Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                            startActivity(Intent(mContext,ActivityHome::class.java))
                        } else Toast.makeText(mContext, "Failed to Add profit", Toast.LENGTH_SHORT)
                            .show()
                    }
            }


        }
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
                        modelFA= snapshot.toObject<ModelFA>()!!

                        // Update the shared preference with the modified modelFA
                        sharedPrefManager.putFAList(
                            sharedPrefManager.getFAList().map {
                                if (it.id == modelFA.id) modelFA else it
                            }
                        )
                        setdata()
                    }


                }
            }

        db.collection(constants.AGENT_EARNING_COLLECTION)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                documentSnapshot?.let { snapshot ->

                    sharedPrefManager.putAgentEarningList(snapshot.documents.mapNotNull { document ->
                        document.toObject(ModelEarning::class.java)?.apply { docID = document.id }
                    })
                }
            }



    }

    fun withdrawEarning(){



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


    /*private fun filterclients(text: String) {
        // creating a new array list to filter our data.
        val filteredlist = ArrayList<User>()
        if (text.isEmpty() || text.equals("")) {
            binding.rvClients.adapter =
                InvestorAdapter(constants.FROM_ASSIGNED_FA, originalFAList, this@ActivityFADetails)

        } else {
            for (user in originalFAList) {

                // Toast.makeText(this@ActivityFADetails, user.cnic +"", Toast.LENGTH_SHORT).show()
                // checking if the entered string matched with any item of our recycler view.
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
                    this@ActivityFADetails
                )

            }
        }
        // running a for loop to compare elements.

    }*/


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
            rvInvestors.adapter = InvestorAdapter(
                constants.FROM_UN_ASSIGNED_FA,
                originallist,
                this@ActivityFADetails
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
                    this@ActivityFADetails
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

    fun setdata() {
    /*    val modelFAStr = intent.getStringExtra("FA")
        val model: ModelFA? = modelFAStr?.let { ModelFA.fromString(it) }*/

         if (modelFA != null) {


             val InActiveInvestCounter = sharedPrefManager.getInvestmentList().filter { investment ->
                 val inActiveInvestment = investment.lastInvestment.takeIf { !it.isNullOrEmpty() } ?: "0"
                 val inActiveInvestment_ = inActiveInvestment.toIntOrNull() ?: 0
                 inActiveInvestment_ > 0 }.count()

             var investorsOfFA= sharedPrefManager.getUsersList().filter { it.fa_id.equals(modelFA.id) }


             var listInvestmentOfFaInvestors= sharedPrefManager.getInvestmentList().filter { investment -> investorsOfFA.any { it.id == investment.investorID } }




             var ActiveInvestment= listInvestmentOfFaInvestors.sumOf { it.investmentBalance.takeIf { it.isNotBlank() }?.toInt() ?: 0 }.toInt()
             var Profit=listInvestmentOfFaInvestors.sumOf { it.lastProfit.takeIf { it.isNotBlank() }?.toInt() ?: 0 }.toInt()
             var InActiveInvestment=listInvestmentOfFaInvestors.sumOf { it.lastInvestment.takeIf { it.isNotBlank() }?.toInt() ?: 0 }.toInt()
             var totalSum= ActiveInvestment+InActiveInvestment+Profit





             binding.tvActiveInvestment.text= ActiveInvestment.toString()
             binding.tvProfit.text= Profit.toString()
             binding.tvInActiveInvestment.text= InActiveInvestment.toString()
             binding.tvExpectedSum.text= totalSum.toString()

             binding.tvName.text = modelFA.firstName
             binding.tvDesignantion.text = modelFA.designantion
             binding.tvEarning.text = modelFA.profit
             binding.tvTotalInvestors.text = investorsOfFA.count().toString()


         }
    }

}