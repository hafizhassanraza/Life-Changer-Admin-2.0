package com.enfotrix.adminlifechanger.ui

import User
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityExcludeInvestorsBinding
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.adminlifechanger.Adapters.AdapterExcludeInvestors
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

class ActivityExcludeInvestors : AppCompatActivity() {

    private lateinit var mContext: Context
    private lateinit var binding: ActivityExcludeInvestorsBinding
    private lateinit var dialog: BottomSheetDialog
    private var investorsList = ArrayList<User>()
    private var removedList = ArrayList<User>()
    private lateinit var rvInvestors: RecyclerView
    private val userViewModel: UserViewModel by viewModels()
    private val constant = Constants()
    private var frombtn: String? =null
    private lateinit var utils: Utils
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var originalInvestorsList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcludeInvestorsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this@ActivityExcludeInvestors
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        investorsList = sharedPrefManager.getUsersList().filter { it.status == constants.INVESTOR_STATUS_ACTIVE } as ArrayList<User>
        originalInvestorsList = ArrayList(investorsList) // Keep a copy for filtering
        binding.selectedClients.text = investorsList.size.toString()
        binding.removedClients.text = removedList?.size.toString()

        binding.removeClients.setOnClickListener {
            frombtn="remove"
//            showClientDialog()
        }
        binding.selectClients.setOnClickListener {
            frombtn="select"
//            showClientDialog()
        }


    }

//    fun showClientDialog() {
//        dialog = BottomSheetDialog(mContext)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.setContentView(R.layout.bottom_sheet_investors)
//        rvInvestors = dialog.findViewById<RecyclerView>(R.id.rvInvestors) as RecyclerView
//        rvInvestors.layoutManager = LinearLayoutManager(mContext)
//        if(frombtn=="remove"){
//            val adapter = frombtn?.let {
//                AdapterExcludeInvestors(constant.FROM_ACTIVITYEXCLUDEINVESTORS, investorsList, this,
//                    it
//                )
//
//            }
//            rvInvestors.adapter = adapter
//
//        }
//        else {
//
//            val adapter = frombtn?.let {
//                AdapterExcludeInvestors(constant.FROM_ACTIVITYEXCLUDEINVESTORS, removedList, this,
//                    it
//                )
//
//            }
//            rvInvestors.adapter = adapter
//        }
//
//
//        dialog.show()
//
//        if(frombtn=="remove"){
//            val svFadetail = dialog.findViewById<androidx.appcompat.widget.SearchView>(R.id.svFadetail)
//            svFadetail?.setOnQueryTextListener(object :
//                androidx.appcompat.widget.SearchView.OnQueryTextListener {
//                override fun onQueryTextSubmit(query: String): Boolean {
//                    return false
//                }
//                override fun onQueryTextChange(newText: String): Boolean {
//                    filter(newText,investorsList,"remove")
//                    return false
//                }
//            })
//        }
//        else {
//
//                val svFadetail = dialog.findViewById<androidx.appcompat.widget.SearchView>(R.id.svFadetail)
//                svFadetail?.setOnQueryTextListener(object :
//                    androidx.appcompat.widget.SearchView.OnQueryTextListener {
//                    override fun onQueryTextSubmit(query: String): Boolean {
//                        return false
//                    }
//                    override fun onQueryTextChange(newText: String): Boolean {
//                        filter(newText,removedList,"select")
//                        return false
//                    }
//                })
//
//        }
//
//    }


    private fun filter(text: String, list: ArrayList<User>, s: String) {
        list.filter { user ->
            user.firstName.toLowerCase(Locale.getDefault())
                .contains(text.toLowerCase(Locale.getDefault()))
        }
//
//            val adapter = frombtn?.let {
//                AdapterExcludeInvestors(constant.FROM_UN_ASSIGNED_FA, list, this,
//                    it
//                )
//            }
//            rvInvestors.adapter = adapter


    }

//    private fun updateAdapter() {
//        binding.removedClients.text = removedList.size.toString()
//        binding.selectedClients.text = investorsList.size.toString()
//        val adapter = frombtn?.let {
//            AdapterExcludeInvestors(constant.FROM_UN_ASSIGNED_FA, investorsList, this,
//                it
//            )
//        }
//        rvInvestors.adapter = adapter
//    }
//    private fun update() {
//        binding.removedClients.text = removedList.size.toString()
//        binding.selectedClients.text = investorsList.size.toString()
//
//        val adapter = frombtn?.let {
//            AdapterExcludeInvestors(constant.FROM_UN_ASSIGNED_FA, removedList, this,
//                it
//            )
//        }
//        rvInvestors.adapter = adapter
//    }
}
