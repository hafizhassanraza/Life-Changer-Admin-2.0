package com.enfotrix.adminlifechanger.Fragments

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.InvestorAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.FragmentActiveInvestorsBinding
import com.enfotrix.adminlifechanger.databinding.FragmentNewInvestersBinding
import com.enfotrix.adminlifechanger.ui.ActivityNewInvestorReqDetails
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


class FragmentActiveInvestors : Fragment() ,  InvestorAdapter.OnItemClickListener{

    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()

    var constant= Constants()

    private val userlist = ArrayList<User>()


    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog



    private var _binding: FragmentActiveInvestorsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentActiveInvestorsBinding.inflate(inflater, container, false)
        val root: View = binding.root


        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        binding.rvInvestors.layoutManager = LinearLayoutManager(mContext)

        runFirestoreRequests()







        return root
    }


    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist = ArrayList<User>()

        if(text.isEmpty()||text.equals("")||text==null){
            binding.rvInvestors.adapter= InvestorAdapter(
                constant.FROM_PENDING_INVESTOR_REQ,
                userlist.filter {  it.status.equals(constant.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt }, this@FragmentActiveInvestors)

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

                binding.rvInvestors.adapter= InvestorAdapter(
                    constant.FROM_PENDING_INVESTOR_REQ,
                    filteredlist.filter {  it.status.equals(constant.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt },
                    this@FragmentActiveInvestors)

            }
        }
        // running a for loop to compare elements.

    }


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



    suspend  fun getRequests(){
        userViewModel.getUsers()
            .addOnCompleteListener{task ->
                utils.endLoadingAnimation()
                if (task.isSuccessful) {
                    if(task.result.size()>0){
                        for (document in task.result) {

                            val user =document.toObject(User::class.java)
                            user.id=document.id
                            userlist.add( user)

                        }

                        binding.rvInvestors.adapter= InvestorAdapter(
                            constant.FROM_PENDING_INVESTOR_REQ,
                            userlist.filter {  it.status.equals(constant.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt }, this@FragmentActiveInvestors)



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

    override fun onItemClick(user: User) {
        startActivity(Intent(mContext, ActivityNewInvestorReqDetails::class.java).putExtra("user",user.toString()).putExtra("from","active"))
    }

    override fun onAssignClick(user: User) {
    }

    override fun onRemoveClick(user: User) {


    }

}