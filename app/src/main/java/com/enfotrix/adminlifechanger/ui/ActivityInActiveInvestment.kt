package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.AdapterInActiveInvestment
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityHomeBinding
import com.enfotrix.adminlifechanger.databinding.ActivityInActiveInvestmentBinding
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.util.Locale

class ActivityInActiveInvestment : AppCompatActivity() , AdapterInActiveInvestment.OnItemClickListener  {


    private val db = Firebase.firestore

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding : ActivityInActiveInvestmentBinding

    private lateinit var userlist : List<User>
    private lateinit var investmentList : List<InvestmentModel>
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInActiveInvestmentBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mContext=this@ActivityInActiveInvestment
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvInvestors.layoutManager = LinearLayoutManager(mContext)


        //binding.btnActiveAll.setOnClickListener { if(userlist.size>0) convertInvestment() }


        setData()


    }

    private fun setData() {


         investmentList= sharedPrefManager.getInvestmentList()
                .filter { investment ->
                    val inActiveInvestment = investment.lastInvestment.takeIf { !it.isNullOrEmpty() } ?: "0"
                    val inActiveInvestment_ = inActiveInvestment.toIntOrNull() ?: 0
                    inActiveInvestment_ > 0
                }

        userlist = sharedPrefManager.getUsersList().filter { user -> investmentList.any { it.investorID.equals(user.id) } }
        binding.rvInvestors.adapter= AdapterInActiveInvestment(userlist, investmentList, this@ActivityInActiveInvestment)


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
            binding.rvInvestors.adapter= AdapterInActiveInvestment(
                userlist.filter {  it.status.equals(constants.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt },
                investmentList ,
                this@ActivityInActiveInvestment)


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



                binding.rvInvestors.adapter= AdapterInActiveInvestment(
                    filteredlist.filter {  it.status.equals(constants.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt },
                    investmentList,
                    this@ActivityInActiveInvestment)


            }
        }
        // running a for loop to compare elements.

    }


    private fun convertInvestment(investmentModel: InvestmentModel) {
        utils.startLoadingAnimation()
        var inActiveInvestment = "0"
        if (!investmentModel.lastInvestment.isNullOrEmpty()) inActiveInvestment = investmentModel.lastInvestment
        val activeInvestment = investmentModel.investmentBalance
        val inActiveInvestment_ = inActiveInvestment?.toInt() ?: 0
        val activeInvestment_ = activeInvestment?.toInt() ?: 0
        val newBalance = inActiveInvestment_ + activeInvestment_
        investmentModel.lastInvestment="0"
        investmentModel.investmentBalance = newBalance.toString()


        db.collection(constants.INVESTMENT_COLLECTION).document(investmentModel.investorID).set(investmentModel)
            .addOnCompleteListener {task->
                if(task.isSuccessful){


                    db.collection(constants.INVESTMENT_COLLECTION).get()
                        .addOnCompleteListener {

                            if (it.isSuccessful){

                                utils.endLoadingAnimation()
                                sharedPrefManager.putAccountList(it.result.documents.mapNotNull { document ->
                                    document.toObject(ModelBankAccount::class.java)
                                        ?.apply { docID = document.id }
                                })

                                setData()
                            }
                        }

                }
            }


    }



    override fun onItemClick(investment: InvestmentModel) {
        convertInvestment(investment)
    }

    override fun addInvestment(investment: InvestmentModel) {
        TODO("Not yet implemented")
    }


}



