package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.AdapterActiveInvestors
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.databinding.ActivityInvestorManagerBinding
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class ActivityInvestorManager : AppCompatActivity() ,AdapterActiveInvestors.OnItemClickListener{

    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()

    var constant= Constants()



    private lateinit var binding: ActivityInvestorManagerBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityInvestorManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Investment Request"

        mContext=this@ActivityInvestorManager
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        binding.rvInvestors.layoutManager = LinearLayoutManager(mContext)

        runFirestoreRequests()

    }

    fun runFirestoreRequests() {
        // Start a coroutine
        utils.startLoadingAnimation()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                //awaitAll( async { getRequests() }, async { getAccount() }, async { getNominees() },async { getFA() })
                awaitAll( async { getRequests() })

                utils.endLoadingAnimation()
                // All requests have completed


            } catch (e: Exception) {
                // Handle any exceptions that occurred during the requests
                // ...
            }
        }

    }


    fun getRequests(){
        lifecycleScope.launch{
            userViewModel.getUsers()
                .addOnCompleteListener{task ->
                    // utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val list = ArrayList<User>()
                        if(task.result.size()>0){
                            for (document in task.result) {

                                val user =document.toObject(User::class.java)
                                user.id=document.id
                                list.add( user)

                            }

                            /*binding.rvInvestors.adapter= AdapterActiveInvestors(
                                list.filter {  it.status.equals(constant.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt },
                                this@ActivityInvestorManager)*/


                            //Toast.makeText(mContext, "d1 : "+ task.result.size(), Toast.LENGTH_SHORT).show()

                        }
                    }
                    else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener{
                    Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                }


        }
    }

    override fun onItemClick(user: User) {

    }


}