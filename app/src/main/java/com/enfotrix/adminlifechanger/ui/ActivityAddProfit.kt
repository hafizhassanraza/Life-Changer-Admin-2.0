package com.enfotrix.adminlifechanger.ui

import User
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.API.FCM
import com.enfotrix.adminlifechanger.Adapters.AdapterExcludeInvestors
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.NotificationModel
import com.enfotrix.adminlifechanger.Models.NotificationViewModel
import com.enfotrix.adminlifechanger.Models.ProfitHistory
import com.enfotrix.adminlifechanger.Models.ProfitModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityAddProfitBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import java.util.*
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date

class ActivityAddProfit : AppCompatActivity() , AdapterExcludeInvestors.OnItemClickListener{

    private val db = Firebase.firestore
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    var constant= Constants()
    var profitModel: ProfitModel? =null
    private val notificationViewModel: NotificationViewModel by viewModels()
    private var frombtn: String? =null
    private lateinit var binding: ActivityAddProfitBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog
    private lateinit var transactionModel: TransactionModel
    private lateinit var user: User
    private var profitCounter:Int?=null
    private var remarks:String?=null
    private  var listInvestmentModel= ArrayList<InvestmentModel>()
    private lateinit var rvInvestors: RecyclerView
    private var investorsList = ArrayList<User>()
    private var removedList = ArrayList<User>()
    private lateinit var selectedDay: Date
    private lateinit var dialogPinUpdate: Dialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProfitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Investment Details"

        mContext=this@ActivityAddProfit
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        investorsList = sharedPrefManager.getUsersList().filter { it.status == constants.INVESTOR_STATUS_ACTIVE } as ArrayList<User>
        listInvestmentModel= sharedPrefManager.getInvestmentList() as ArrayList<InvestmentModel>
        binding.included.text = investorsList.size.toString()
        binding.excluded.text = removedList?.size.toString()
        binding.availableProfit.text = sharedPrefManager.getInvestmentList()
            .sumOf { it.lastProfit.takeIf { profit -> profit.isNotBlank() }?.toInt() ?: 0 }
            .toString()


        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->

             val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+5"))
             calendar.set(year, month, dayOfMonth, 0, 0, 0)
                selectedDay = calendar.time

        }
        binding.btnProfitHistory.setOnClickListener {
            val intent = Intent(mContext, ActivityProfitHistory::class.java)
            startActivity(intent)
        }



        binding.btnRemoveProfit.setOnClickListener {
            checkPin(object : PinCheckCallback {
                override fun onPinChecked(isValid: Boolean) {
                    if (isValid) {
                        Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show()
                         deductProfit(selectedDay)
                    } else {
                        Toast.makeText(mContext, "Enter valid pin please", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        binding.btnRemoveProfitTransactions.setOnClickListener {
            checkPin(object : PinCheckCallback {
                override fun onPinChecked(isValid: Boolean) {
                    if (isValid) {
                        Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show()
                         deleteProfitTransactions(selectedDay)
                    } else {
                        Toast.makeText(mContext, "Enter valid pin please", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        binding.btnRemoveProfitNotifications.setOnClickListener {
            checkPin(object : PinCheckCallback {
                override fun onPinChecked(isValid: Boolean) {
                    if (isValid) {
                        Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show()
                        deleteProfitNotifications(selectedDay)
                    } else {
                        Toast.makeText(mContext, "Enter valid pin please", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }





//        binding.btnTemp.setOnClickListener {
//            //tempCode()
//            //tempGetTodayActiveInvestment()
//
//            //deductProfit()
//            //deleteProfitTransactions()
//
//            //deleteProfitNotifications()
//
//        }


        //41//1111



        getData()
        binding.btnExclude.setOnClickListener {
            frombtn="remove"
            showClientDialog()
        }
        binding.btnInclude.setOnClickListener {
            frombtn="select"
          showClientDialog()
        }




        binding.btnAddProfit.setOnClickListener {
            val percentage = binding.etProfit.text.toString()
            val check: Double? = percentage.toDoubleOrNull()
            if(check==null){
                Toast.makeText(mContext, "Please Enter Valid Amount", Toast.LENGTH_SHORT).show()
            }
            else {
                val dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_varify_profit_pin, null)
                val builder = AlertDialog.Builder(mContext)
                builder.setView(dialogView)
                val alertDialog = builder.create()
                alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val passwordEditText = dialogView.findViewById<EditText>(R.id.password)
                val remarksEditText = dialogView.findViewById<EditText>(R.id.etRemarks)
                val enterButton = dialogView.findViewById<Button>(R.id.btnEnter)
                enterButton.setOnClickListener {
                    remarks=remarksEditText.text.toString()
                    val enteredPassword = passwordEditText.text.toString()
                    Toast.makeText(mContext, "percentage $percentage", Toast.LENGTH_SHORT).show()
                    if (enteredPassword != "123789"&& remarks!!.isEmpty()) {
                        Toast.makeText(mContext, "Please Enter Remarks", Toast.LENGTH_SHORT).show()
                    }

                    else {
                        Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show()
                        addProfit(percentage.toDouble() / 100,percentage)
                        alertDialog.dismiss()
                    }
                }

                alertDialog.show()
            }

        }





        binding.btnConvertProfit.setOnClickListener {
            checkPin(object : PinCheckCallback {
                override fun onPinChecked(isValid: Boolean) {
                    if (isValid) {
                        Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show()
                          convertProfit()
                    } else {
                        Toast.makeText(mContext, "Enter valid pin please", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }





        /*binding.btnAllInvestorsAccept.setOnClickListener {
            startActivity(Intent(mContext, ActivityExcludeInvestors::class.java))
        }*/


        /*binding.btnConvertInvestment.setOnClickListener{


            convertInvestment()
        }*/

    }





    private fun tempCode (){



        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+5"))
        calendar.set(2024, Calendar.FEBRUARY, 23, 0, 0, 0)
        val startOfDay = calendar.time
        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val filtered_list_February_16_2024_Profit = sharedPrefManager.getTransactionList().filter {
            it.type == constants.TRANSACTION_TYPE_PROFIT &&
                    dateFormatter.format(it.createdAt.toDate()) == dateFormatter.format(startOfDay)
        }




        //val sum = filtered_list_February_16_2024_Profit.sumOf { it.amount.toIntOrNull() ?: 0 }

        // Deduct Profit

        //filtered_list_February_16_2024_Profit

        //Toast.makeText(mContext, sum.toString(), Toast.LENGTH_SHORT).show()



    }



    private fun tempGetTodayActiveInvestment (){
        //var list_February_16_2024_Profit = sharedPrefManager.getTransactionList().filter { it.type.equals(constants.TRANSACTION_TYPE_PROFIT) && it.createdAt.equals() }.count()

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+5"))
        calendar.set(2024, Calendar.FEBRUARY, 23, 0, 0, 0)
        val startOfDay = calendar.time

        calendar.set(2024, Calendar.FEBRUARY, 17, 0, 0, 0)
        val startOfNextDay = calendar.time

        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

        val usersIDs=""
        val filtered_list_February_23_2024_ActiveInvestment = sharedPrefManager.getNotificationList().filter {
            it.notiTitle.equals("Investment Activation") &&
                    dateFormatter.format(it.createdAt.toDate()) == dateFormatter.format(startOfDay)
        }





            //val sum = filtered_list_February_16_2024_Profit.sumOf { it.amount.toIntOrNull() ?: 0 }


        Toast.makeText(mContext, filtered_list_February_23_2024_ActiveInvestment.count().toString(), Toast.LENGTH_SHORT).show()



    }


    fun showClientDialog() {
        dialog = BottomSheetDialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.bottom_sheet_investors)
        rvInvestors = dialog.findViewById<RecyclerView>(R.id.rvInvestors) as RecyclerView
        rvInvestors.layoutManager = LinearLayoutManager(mContext)
        if(frombtn=="remove"){
            val adapter = frombtn?.let {
                AdapterExcludeInvestors(constant.FROM_ACTIVITYEXCLUDEINVESTORS, investorsList, this,
                    it
                )

            }
            rvInvestors.adapter = adapter

        }
        else {

            val adapter = frombtn?.let {
                AdapterExcludeInvestors(constant.FROM_ACTIVITYEXCLUDEINVESTORS, removedList, this,
                    it
                )

            }
            rvInvestors.adapter = adapter
        }


        dialog.show()

        if(frombtn=="remove"){
            val svFadetail = dialog.findViewById<androidx.appcompat.widget.SearchView>(R.id.svFadetail)
            svFadetail?.setOnQueryTextListener(object :
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }
                override fun onQueryTextChange(newText: String): Boolean {
                    filter(newText,investorsList,"remove")
                    return false
                }
            })
        }
        else {

            val svFadetail = dialog.findViewById<androidx.appcompat.widget.SearchView>(R.id.svFadetail)
            svFadetail?.setOnQueryTextListener(object :
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }
                override fun onQueryTextChange(newText: String): Boolean {
                    filter(newText,removedList,"select")
                    return false
                }
            })

        }

    }


    fun filter(query: String, dataList: List<User>, action: String) {
        val filteredList = dataList.filter { investor ->
            val fullName = "${investor.firstName} ${investor.lastName}".replace("\\s".toRegex(), "")

            if (action == "remove") {
                fullName.contains(query.replace("\\s".toRegex(), ""), ignoreCase = true)
            } else {
                fullName.contains(query.replace("\\s".toRegex(), ""), ignoreCase = true)
            }
        }

        when (action) {
            "remove" -> {
                val adapter = AdapterExcludeInvestors(
                    constant.FROM_ACTIVITYEXCLUDEINVESTORS,
                    filteredList as ArrayList<User>,
                    this,
                    "remove"
                )
                rvInvestors.adapter = adapter
//                investorsList.clear()
//                investorsList.addAll(filteredList)
            }
            "select" -> {
                val adapter = AdapterExcludeInvestors(
                    constant.FROM_ACTIVITYEXCLUDEINVESTORS,
                    filteredList as ArrayList<User>,
                    this,
                    "select"
                )
                rvInvestors.adapter = adapter
//                investorsList.clear()
//                investorsList.addAll(filteredList)
            }
        }
    }


    fun yourFunctionName(newText: String): Boolean {
        filter(newText.orEmpty(), investorsList, frombtn!!)
        return false
    }

    


    private fun deductProfit(selectedDay: Date) {

        utils.startLoadingAnimation()

        val totalInvestments = listInvestmentModel.size


        var EffectedSum= 0


        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val listTransactionsOfTodayEffectedProfit = sharedPrefManager.getTransactionList().filter {
            it.type == constants.TRANSACTION_TYPE_PROFIT &&
                    dateFormatter.format(it.createdAt.toDate()) == dateFormatter.format(selectedDay)
        }


        // loop for  all  investments
        for ((index, investmentModel) in listInvestmentModel.withIndex()) {



            // 1 by 1 total Profit
            val totalProfit = investmentModel.lastProfit

            if (!totalProfit.isNullOrEmpty()) {


               val effectedProfitSumOfUser= listTransactionsOfTodayEffectedProfit.filter { it.investorID.equals(investmentModel.investorID) }.sumOf { it.amount.toInt() }
                var totalProfit_ = totalProfit.toInt()


                var  newTotalProfit_ = totalProfit_ - effectedProfitSumOfUser

                EffectedSum = EffectedSum+effectedProfitSumOfUser

                investmentModel.lastProfit = newTotalProfit_.toString()


                lifecycleScope.launch {
                    val setInvestmentTask = investmentViewModel.setInvestment(investmentModel)

                    Tasks.whenAllComplete(setInvestmentTask)
                        .addOnCompleteListener {

                            if (index == totalInvestments - 1) {
                                utils.endLoadingAnimation()
                                Toast.makeText(mContext, "Profit Deduct Successfully!", Toast.LENGTH_SHORT).show()
                            }

                        }
                }


            }
        }


    }



    private fun deleteProfitTransactions(selectedDay: Date) {

        utils.startLoadingAnimation()





        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val listTransactionsOfTodayEffectedProfit = sharedPrefManager.getTransactionList().filter {
            it.type == constants.TRANSACTION_TYPE_PROFIT &&
                    dateFormatter.format(it.createdAt.toDate()) == dateFormatter.format(selectedDay)
        }




        var totalTransactions= listTransactionsOfTodayEffectedProfit.count()


        // loop for  all  investments
        for ((index, transactionModel)   in listTransactionsOfTodayEffectedProfit.withIndex()) {

            lifecycleScope.launch {
                val setInvestmentTask =  investmentViewModel.deleteTransactionReq(transactionModel)

                Tasks.whenAllComplete(setInvestmentTask)
                    .addOnCompleteListener {

                        if (index == totalTransactions - 1) {

                            utils.endLoadingAnimation()

                            Toast.makeText(mContext, "Transaction Deleted Successfully!", Toast.LENGTH_SHORT).show()

                        }

                    }
            }
        }

    }


    private fun deleteProfitNotifications(selectedDay: Date) {

        utils.startLoadingAnimation()









        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val filtered_list_February_23_2024_ProfitNotifications = sharedPrefManager.getNotificationList().filter {
            it.notiTitle.equals("Profit Credited") &&
                    dateFormatter.format(it.createdAt.toDate()) == dateFormatter.format(selectedDay)
        }




        var totalTransactions= filtered_list_February_23_2024_ProfitNotifications.count()




        // loop for  all  investments
        for ((index, notificationModel)   in filtered_list_February_23_2024_ProfitNotifications.withIndex()) {

            lifecycleScope.launch {
                val setInvestmentTask =  investmentViewModel.deleteNotifications(notificationModel)

                Tasks.whenAllComplete(setInvestmentTask)
                    .addOnCompleteListener {

                        if (index == totalTransactions - 1) {

                            utils.endLoadingAnimation()

                            Toast.makeText(mContext, "Notification Deleted Successfully!", Toast.LENGTH_SHORT).show()

                        }

                    }
            }
        }

    }


    private fun addProfit(percentage: Double, percentage_: String) {

      utils.startLoadingAnimation()

        val filteredInvestmentList = listInvestmentModel.filter { investment ->
            investorsList.any { investor -> investor.id == investment.investorID }
        }
        val totalInvestments = filteredInvestmentList.size

        Toast.makeText(mContext, "invest$totalInvestments", Toast.LENGTH_SHORT).show()
        for ((index, investmentModel) in filteredInvestmentList.withIndex()) {




            val previousBalance = investmentModel.investmentBalance
            val previousTotalBalance = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()

            var previousProfit = investmentModel.lastProfit

            if (!previousBalance.isNullOrEmpty()) {



                if(previousProfit.isNullOrEmpty()) previousProfit="0"

                val previousBalance_ = previousBalance.toInt()
                val previousProfit_ = previousProfit.toInt()
                val profit = (previousBalance_ * percentage).toInt()
                val newProfit = previousProfit_ + profit

                investmentModel.lastProfit = newProfit.toString()
                val newTotalBalance = getTextFromInvestment(investmentModel.investmentBalance).toDouble()+ getTextFromInvestment(investmentModel.lastProfit).toDouble() + getTextFromInvestment(investmentModel.lastInvestment).toDouble()
//                   profitCounter = profitCounter?.plus(profit.toInt()) ?: profit.toInt()
                profitCounter = (profitCounter ?: 0) + profit.toInt()
                //for notification
//                val User=investorsList.find { it.id.equals(investmentModel.investorID) }
//                val notificationData = "Dear ${User?.firstName}, The profit of ${profit} has been credited to your account"
//                if (User != null) {
//                    addNotification(
//                        NotificationModel(
//                            "",
//                            User.id,
//                            getCurrentDateInFormat(),
//                            "Profit Credited",
//                            notificationData
//                        ),
//                        User
//                    )
//                }

                val profitModel = TransactionModel(
                    investmentModel.investorID,
                    "Profit",
                    "Approved",
                    profit.toString(),  // Current (weekly) Profit
                    "",
                    previousTotalBalance.toString(), // Previous Total (Investment + profit + inactiveInvestment)
                    "",
                    "",
                    newTotalBalance.toString(),  //  New Total (Investment + profit + inactiveInvestment)
                    Timestamp.now(),
                    Timestamp.now()
                )


//                lifecycleScope.launch {
//                    val setInvestmentTask = investmentViewModel.setInvestment(investmentModel)
//                    val addTransactionTask = db.collection(constants.TRANSACTION_REQ_COLLECTION).add(profitModel)
//
//                    Tasks.whenAllComplete(setInvestmentTask, addTransactionTask)
//                        .addOnCompleteListener {
//                            if (index == totalInvestments - 1) {
//                                utils.endLoadingAnimation()
//                                saveProfitHistory(profitCounter,percentage_)
//                            }
//
//
//
//                        }
//                }

                lifecycleScope.launch {
                    val setInvestmentTask = investmentViewModel.setTestInvestment(investmentModel)
                    val addTransactionTask = db.collection(constants.TEST_TRANSACTION).add(profitModel)

                    Tasks.whenAllComplete(setInvestmentTask, addTransactionTask)
                        .addOnCompleteListener {
                            if (index == totalInvestments - 1) {
                                utils.endLoadingAnimation()
                                saveProfitHistory(profitCounter,percentage_)
                            }



                        }
                }





            }
        }

    }

    private fun saveProfitHistory(newProfitAmount: Int?, newProfitPercentage: String) {
        var ActiveInvestment =sharedPrefManager. getInvestmentList().sumOf {
            it.investmentBalance.takeIf { it.isNotBlank() }?.toInt() ?: 0
        }.toInt()
        var totalProfit =
            sharedPrefManager. getInvestmentList().sumOf { it.lastProfit.takeIf { it.isNotBlank() }?.toInt() ?: 0 }
                .toInt()
        var InActiveInvestment =
            sharedPrefManager. getInvestmentList().sumOf { it.lastInvestment.takeIf { it.isNotBlank() }?.toInt() ?: 0 }
                .toInt()
        var totalSum = ActiveInvestment + InActiveInvestment + totalProfit



        lifecycleScope.launch {
            investmentViewModel.setProfitHistory(ProfitHistory("",
                totalProfit.toString(),ActiveInvestment.toString(),totalSum.toString(), InActiveInvestment.toString(),investorsList?.size.toString()
                ,newProfitAmount.toString(),newProfitPercentage.toString(),remarks!!,
                Timestamp.now())).addOnCompleteListener { task->
                if(task.isSuccessful){
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, "Profit Added Successfully!", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
        }






    }


    fun getTextFromInvestment(value: String?): String {
        return if (value.isNullOrEmpty()) "0" else value
    }
    private fun addNotification(notificationModel: NotificationModel, user_: User) {
        lifecycleScope.launch {
            try {
                notificationViewModel.setNotification(notificationModel).await()
                FCM().sendFCMNotification(
                    user_.userdevicetoken,
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


    private fun convertProfit() {
        utils.startLoadingAnimation()

        val totalInvestments = listInvestmentModel.size
        for ((index, investmentModel) in listInvestmentModel.withIndex()) {
            var previous_profit = investmentModel.lastProfit
            val User=sharedPrefManager.getUsersList().find { it.id.equals(investmentModel.investorID) }
            val notificationData = "Dear ${User?.firstName}, Your previous profit of $previous_profit PKR has been converted to your Investment "
            if (User != null) {
                addNotification(
                    NotificationModel(
                        "",
                        User.id,
                        getCurrentDateInFormat(),
                        "Profit Converted to Investment",
                        notificationData,

                    ), user
                )
            }





            val previousBalance = investmentModel.investmentBalance
            val previousProfit = investmentModel.lastProfit

            if (!previousProfit.isNullOrEmpty()) {


                var previousBalance_ = previousBalance.toInt()
                val previousProfit_ = previousProfit.toInt()

                var newBalance = previousBalance_+ previousProfit_

                investmentModel.lastProfit = "0"
                investmentModel.investmentBalance= (newBalance.toInt()).toString()



                lifecycleScope.launch {
                    val setInvestmentTask = investmentViewModel.setInvestment(investmentModel)

                    Tasks.whenAllComplete(setInvestmentTask)
                        .addOnCompleteListener {

                            if (index == totalInvestments - 1) {
                                utils.endLoadingAnimation()
                                Toast.makeText(mContext, "Profit Converted Successfully!", Toast.LENGTH_SHORT).show()
                            }

                        }
                }

            }
        }
    }
    private fun convertInvestment() {
        utils.startLoadingAnimation()

        val totalInvestments = listInvestmentModel.size

        for ((index, investmentModel) in listInvestmentModel.withIndex()) {

            var inActiveInvestment = "0"
            if (!investmentModel.lastInvestment.isNullOrEmpty()) inActiveInvestment = investmentModel.lastInvestment
            val activeInvestment = investmentModel.investmentBalance


            val inActiveInvestment_ = inActiveInvestment?.toInt() ?: 0
            val activeInvestment_ = activeInvestment?.toInt() ?: 0
            val newBalance = inActiveInvestment_ + activeInvestment_

            investmentModel.lastInvestment="0"
            investmentModel.investmentBalance = newBalance.toString()

            lifecycleScope.launch {
                val setInvestmentTask = investmentViewModel.setInvestment(investmentModel)

                setInvestmentTask.addOnCompleteListener {
                    if (index == totalInvestments - 1) {
                        utils.endLoadingAnimation()
                        Toast.makeText(mContext, "Investment Converted Successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /*    private fun addProfit(percentage: Double) {


            utils.startLoadingAnimation()

            val totalInvestments = listInvestmentModel.size


            for ((index, investmentModel) in listInvestmentModel.withIndex()){


                var previousBalance = investmentModel.investmentBalance
                var previousProfit = investmentModel.lastProfit

                if (previousBalance != null && previousBalance != "") {
                    var profit: Double=0.0

                    var previousBalance_ = previousBalance.toInt()
                    var previousProfit_ = previousProfit.toInt()
                    profit = previousBalance_ * percentage

                    //var newBalance = previousBalance_ + profit.toInt()
                    var newProfit = previousProfit_ + profit.toInt()



                    investmentModel.lastProfit = (newProfit.toInt()).toString()


                    var profit_=profit.toInt()

                    var transactionModel=TransactionModel(
                        investmentModel.investorID,
                        "Profit",
                        "Approved",
                        profit_.toString(), // current (weekly) Profit
                        "",
                        previousProfit_.toString(), //  previous profit
                        "",
                        "",
                        (newProfit.toInt()).toString(),// new balance -> new profit
                        Timestamp.now(),
                        Timestamp.now()
                    )

                    lifecycleScope.launch{
                        investmentViewModel.setInvestment(investmentModel)
                            .addOnCompleteListener{task->


                                db.collection(constants.TRANSACTION_REQ_COLLECTION).add(transactionModel)
                                    .addOnCompleteListener {
                                        if (index == totalInvestments - 1) {
                                            utils.endLoadingAnimation()
                                            Toast.makeText(mContext, "Profit Added Successfully!", Toast.LENGTH_SHORT).show()

                                        }
                                    }
                            }


                    }


                }
            }


        }*/

    private fun getData() {
        db.collection(constants.INVESTMENT_COLLECTION).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    utils.endLoadingAnimation()
                    if(task.result.size()>0){

                        var balance:Int=0
                        for (document in task.result){
                            var investmentModel=document.toObject(InvestmentModel::class.java)
                            listInvestmentModel.add( document.toObject(InvestmentModel::class.java))
                            if (investmentModel!=null) balance=balance+ investmentModel.investmentBalance.toInt()
                        }



                    }
                }
                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{
                Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

            }
    }



    override fun onItemClick(user: User) {
       binding.excluded.text = removedList.size.toString()
        investorsList.add(user)
        removedList = ArrayList(removedList.filter { it.id != user.id })
        update()
    }

    override fun onAssignClick(user: User) {

    }

    override fun onRemoveClick(user: User) {
        binding.included.text = investorsList.size.toString()
        investorsList = ArrayList(investorsList.filter { it.id != user.id })
        removedList.add(user)
        updateAdapter()
    }
       private fun updateAdapter() {
        binding.excluded.text = removedList.size.toString()
        binding.included.text = investorsList.size.toString()
        val adapter = frombtn?.let {
            AdapterExcludeInvestors(constant.FROM_UN_ASSIGNED_FA, investorsList, this,
                it
            )
        }
        rvInvestors.adapter = adapter
    }

    private fun update() {
        binding.excluded.text = removedList.size.toString()
       binding.included.text = investorsList.size.toString()

        val adapter = frombtn?.let {
            AdapterExcludeInvestors(constant.FROM_UN_ASSIGNED_FA, removedList, this,
                it
            )
        }
        rvInvestors.adapter = adapter
    }


    private fun checkPin(callback: PinCheckCallback) {
        dialogPinUpdate = Dialog(mContext)
        dialogPinUpdate.setContentView(R.layout.dialog_varify_pin)
        dialogPinUpdate.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogPinUpdate.setCancelable(true)

        val pin1 = dialogPinUpdate.findViewById<EditText>(R.id.etPin1)
        val pin2 = dialogPinUpdate.findViewById<EditText>(R.id.etPin2)
        val pin3 = dialogPinUpdate.findViewById<EditText>(R.id.etPin3)
        val pin4 = dialogPinUpdate.findViewById<EditText>(R.id.etPin4)
        val pin5 = dialogPinUpdate.findViewById<EditText>(R.id.etPin5)
        val pin6 = dialogPinUpdate.findViewById<EditText>(R.id.etPin6)
        val btnSetPin = dialogPinUpdate.findViewById<Button>(R.id.btnSetpin)

        pin1.requestFocus()
        utils.moveFocus(listOf(pin1, pin2, pin3, pin4, pin5, pin6))

        val tvClearAll = dialogPinUpdate.findViewById<TextView>(R.id.tvClearAll)
        tvClearAll.setOnClickListener {
            utils.clearAll(listOf(pin1, pin2, pin3, pin4, pin5, pin6))
            pin1.requestFocus()
        }

        btnSetPin.setOnClickListener {
            val completePin = "${pin1.text}${pin2.text}${pin3.text}${pin4.text}${pin5.text}${pin6.text}"
       //     Toast.makeText(mContext, "Entered PIN: $completePin", Toast.LENGTH_SHORT).show()
            callback.onPinChecked(completePin == "123789")
            dialogPinUpdate.dismiss()
        }

        dialogPinUpdate.show()
    }
    interface PinCheckCallback {
        fun onPinChecked(isValid: Boolean)
    }








}