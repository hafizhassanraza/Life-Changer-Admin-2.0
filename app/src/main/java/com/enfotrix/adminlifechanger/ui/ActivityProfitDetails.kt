package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.Adapters.AdapterCreditedInvestors
import com.enfotrix.adminlifechanger.Adapters.AdapterExcludeInvestors
import com.enfotrix.adminlifechanger.Adapters.AdapterProfitHistory
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.ProfitHistory
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityProfitDetailsBinding
import com.enfotrix.adminlifechanger.databinding.ActivityProfitHistoryBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ActivityProfitDetails : AppCompatActivity() {
    private lateinit var binding: ActivityProfitDetailsBinding

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var listInvestors: List<User>
    private lateinit var transactionList: List<TransactionModel>
    private lateinit var dialog : Dialog
    private lateinit var rvInvestors: RecyclerView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfitDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this@ActivityProfitDetails
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        listInvestors = sharedPrefManager.getUsersList()
        val profitModel = Gson().fromJson(intent.getStringExtra("profitModel"), ProfitHistory::class.java)
        binding.rvProfit.layoutManager = LinearLayoutManager(mContext)
         transactionList=filterTransactionList(profitModel)


        setData()






    }


    private fun setData() {

        binding.rvProfit.adapter = AdapterCreditedInvestors(

            sharedPrefManager.getUsersList(),
            transactionList,
            mContext

        )

    }

    fun filterTransactionList(profitModel: ProfitHistory): List<TransactionModel> {
        return sharedPrefManager.getTransactionList()
            .filter { it.type == constants.PROFIT_TYPE }
            .filter { transaction ->
                val transactionDate = transaction.createdAt.toDate()
                val profitModelDate = profitModel.createdAt.toDate()

                val transactionCalendar = Calendar.getInstance().apply {
                    time = transactionDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val profitModelCalendar = Calendar.getInstance().apply {
                    time = profitModelDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                return@filter transactionCalendar.timeInMillis == profitModelCalendar.timeInMillis
            }
    }


}