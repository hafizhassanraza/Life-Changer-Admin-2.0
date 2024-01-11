package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.AdapterActiveInvestors
import com.enfotrix.adminlifechanger.Adapters.AdapterInActiveInvestment
import com.enfotrix.adminlifechanger.Adapters.InvestorViewPagerAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityInvestorsBinding
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.Locale

class ActivityInvestors : AppCompatActivity() ,  AdapterActiveInvestors.OnItemClickListener{



    private lateinit var userlist : List<User>
    private lateinit var investmentList : List<InvestmentModel>

    private val db = Firebase.firestore
    private val firebaseStorage = Firebase.storage
    private val storageRef = firebaseStorage.reference

    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()

    var constant= Constants()




    private lateinit var user: User
    private lateinit var dialog : Dialog
    private val CREATE_PDF_REQUEST_CODE = 123
    val filteredlist = ArrayList<User>()



    private lateinit var binding: ActivityInvestorsBinding

    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestorsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityInvestors
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        setTitle("Investors")

        binding.rvInvestors.layoutManager = LinearLayoutManager(mContext)



        setData()

    }

    private fun setData() {


        investmentList = sharedPrefManager.getInvestmentList()
        userlist = sharedPrefManager.getUsersList().filter { it.status.equals(constant.INVESTOR_STATUS_ACTIVE) }

        binding.rvInvestors.adapter= AdapterActiveInvestors(userlist.sortedByDescending { it.createdAt }, investmentList,this@ActivityInvestors)

        binding.svUsers.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // inside on query text change method we are
                // calling a method to filter our recycler view.
                filter(newText)
                return false
            }
        })



    }



    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist = ArrayList<User>()
        if(text.isEmpty()||text.equals("")||text==null){

            binding.rvInvestors.adapter= AdapterActiveInvestors(userlist.sortedByDescending { it.createdAt }, investmentList,this@ActivityInvestors)
        }
        else {
            for (user in userlist) {
                // checking if the entered string matched with any item of our recycler view.
                if (user.firstName.toLowerCase().contains(text.lowercase(Locale.getDefault()))) {
                    // if the item is matched we are
                    // adding it to our filtered list.
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

                binding.rvInvestors.adapter= AdapterActiveInvestors(
                    filteredlist.filter {  it.status.equals(constants.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt },
                    investmentList,this@ActivityInvestors)


            }
        }
        // running a for loop to compare elements.

    }
    override fun onItemClick(user: User) {
        startActivity(Intent(mContext, ActivityInvestorDetails::class.java).putExtra("user",user.toString()))
    }
}