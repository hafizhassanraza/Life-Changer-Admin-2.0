package com.enfotrix.adminlifechanger.Fragments

import User
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.AdapterActiveInvestment
import com.enfotrix.adminlifechanger.Adapters.AdapterInActiveInvestment
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityAddFaBinding
import com.enfotrix.adminlifechanger.databinding.FragmentActiveInvestmentBinding
import com.enfotrix.adminlifechanger.databinding.FragmentInActiveInvestmentBinding
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class FragmentInActiveInvestment : Fragment() , AdapterInActiveInvestment.OnItemClickListener {


    private val db = Firebase.firestore
    private val firebaseStorage = Firebase.storage
    private val storageRef = firebaseStorage.reference



    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()

    var constant= Constants()

    private val userlist = ArrayList<User>()
    private var listInvestment = ArrayList<InvestmentModel>()


    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog



    private var _binding: FragmentInActiveInvestmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentInActiveInvestmentBinding.inflate(inflater, container, false)
        val root: View = binding.root


        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        binding.rvInvestors.layoutManager = LinearLayoutManager(mContext)

        runFirestoreRequests()


        binding.btnActiveAll.setOnClickListener { if(userlist.size>0) convertInvestment() }





        return root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun convertInvestment() {
        utils.startLoadingAnimation()

        val totalInvestments = listInvestment.size

        for ((index, investmentModel) in listInvestment.withIndex()) {

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
                        runFirestoreRequests()
                    }
                }
            }
        }
    }



    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist = ArrayList<User>()
        if(text.isEmpty()||text.equals("")||text==null){
            binding.rvInvestors.adapter= AdapterInActiveInvestment(userlist.filter {  it.status.equals(constant.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt }, listInvestment, this@FragmentInActiveInvestment)


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



                binding.rvInvestors.adapter= AdapterInActiveInvestment(filteredlist.filter {  it.status.equals(constant.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt }, listInvestment, this@FragmentInActiveInvestment)


            }
        }
        // running a for loop to compare elements.

    }



    @RequiresApi(Build.VERSION_CODES.N)
    fun runFirestoreRequests() {
        // Start a coroutine
        utils.startLoadingAnimation()

        CoroutineScope(Dispatchers.Main).launch {
            try {

                getRequests()
                /*val getRequestsDeferred = async { getRequests() }
                val getAccountDeferred = async { getAccount() }
                val getNomineesDeferred = async { getNominees() }
                val getFADeferred = async { getFA() }

                // Wait for all deferred coroutines to complete
                joinAll(getRequestsDeferred, getAccountDeferred, getNomineesDeferred, getFADeferred)*/

                // All requests have completed
            } catch (e: Exception) {
                // Handle any exceptions that occurred during the requests
                // ...
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.N)
    suspend  fun getRequests(){
        userViewModel.getUsers()
            .addOnCompleteListener{task ->
                if (task.isSuccessful) {
                    if(task.result.size()>0) {

                        for (document in task.result) {

                            val user = document.toObject(User::class.java)
                            user.id = document.id
                            userlist.add(user)

                        }




                        db.collection(constants.INVESTMENT_COLLECTION)
                            .addSnapshotListener { snapshot, firebaseFirestoreException ->
                                firebaseFirestoreException?.let {
                                    Toast.makeText(mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                                    return@addSnapshotListener
                                }
                                snapshot?.let { documents ->

                                    listInvestment = documents.map { it.toObject(InvestmentModel::class.java) } as ArrayList<InvestmentModel>
                                    sharedPrefManager.putActiveInvestment(listInvestment)



                                    listInvestment
                                        .filter { investment ->
                                            val inActiveInvestment = investment.lastInvestment.takeIf { !it.isNullOrEmpty() } ?: "0"
                                            val inActiveInvestment_ = inActiveInvestment.toIntOrNull() ?: 0
                                            inActiveInvestment_ <= 0
                                        }
                                        .sortedBy { investment ->
                                            val inActiveInvestment = investment.lastInvestment.takeIf { !it.isNullOrEmpty() } ?: "0"
                                            inActiveInvestment.toIntOrNull() ?: 0
                                        }
                                        .forEach { investment ->
                                            userlist.removeIf { it.id == investment.investorID }
                                        }





                                    binding.rvInvestors.adapter= AdapterInActiveInvestment(userlist.filter {  it.status.equals(constant.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt }, listInvestment, this@FragmentInActiveInvestment)

                                    utils.endLoadingAnimation()


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
                            }







                        //Toast.makeText(mContext, "d1 : "+ task.result.size(), Toast.LENGTH_SHORT).show()

                    }
                }
                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{
                Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

            }

        /*lifecycleScope.launch{}*/
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onItemClick(investment: InvestmentModel) {

        convertInvestment(investment)

    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun addInvestment(investment: InvestmentModel) {



        var dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_balance)

        val etBalance = dialog.findViewById<EditText>(R.id.etBalance)
        val btnAddBalance = dialog.findViewById<Button>(R.id.btnAddBalance)


        btnAddBalance.setOnClickListener {

            var amount =etBalance.text.toString()
            if (!amount.isNullOrEmpty()) {
                dialog.dismiss()
                val amount_ = amount?.toInt() ?: 0
                if(amount_>0){
                    dialog.dismiss()
                    AddInvestment(investment,amount_)
                }
                else Toast.makeText(mContext, "Incorrect Amount!", Toast.LENGTH_SHORT).show()

            }
            else Toast.makeText(mContext, "Incorrect Amount!", Toast.LENGTH_SHORT).show()



        }
        dialog.show()








    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun AddInvestment(investmentModel: InvestmentModel, amount:Int) {
        utils.startLoadingAnimation()

        var inActiveInvestment = "0"
        if (!investmentModel.lastInvestment.isNullOrEmpty()) inActiveInvestment = investmentModel.lastInvestment


        val inActiveInvestment_ = inActiveInvestment?.toInt() ?: 0
        val newBalance = inActiveInvestment_ + amount

        investmentModel.lastInvestment=newBalance.toString()

        lifecycleScope.launch {
            val setInvestmentTask = investmentViewModel.setInvestment(investmentModel)

            setInvestmentTask.addOnCompleteListener {
                utils.endLoadingAnimation()
                Toast.makeText(mContext, "Investment Added Successfully!", Toast.LENGTH_SHORT).show()

                runFirestoreRequests()

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
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

        lifecycleScope.launch {
            val setInvestmentTask = investmentViewModel.setInvestment(investmentModel)

            setInvestmentTask.addOnCompleteListener {
                utils.endLoadingAnimation()
                Toast.makeText(mContext, "Investment Converted Successfully!", Toast.LENGTH_SHORT).show()

                runFirestoreRequests()

            }
        }
    }




}