package com.enfotrix.adminlifechanger.Fragments

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.InvestorAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.databinding.FragmentDashboardBinding
import com.enfotrix.adminlifechanger.Models.DashboardViewModel
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.databinding.ActivityNewInvestorsReqBinding
import com.enfotrix.adminlifechanger.ui.ActivityFA
import com.enfotrix.adminlifechanger.ui.ActivityInvestors
import com.enfotrix.adminlifechanger.ui.ActivityNewInvestorReqDetails
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {






    private val db = Firebase.firestore
    private val firebaseStorage = Firebase.storage
    private val storageRef = firebaseStorage.reference





    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private val faViewModel: FAViewModel by viewModels()

    var constant= Constants()



    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog





    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/




        mContext=requireContext()
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)




        binding.layInvestors.setOnClickListener{startActivity(Intent(requireContext(), ActivityInvestors::class.java))}
        binding.layFA.setOnClickListener{startActivity(Intent(requireContext(), ActivityFA::class.java))}



        getUsers_Account_Nominee_FA()

        return root
    }



    fun runFirestoreRequests() {
        // Start a coroutine

        CoroutineScope(Dispatchers.Main).launch {
            try {

                val getAccountDeferred = async { getAccount() }
                val getNomineesDeferred = async { getNominees() }
                val getFADeferred = async { getFA() }

                // Wait for all deferred coroutines to complete
                joinAll( getAccountDeferred, getNomineesDeferred, getFADeferred)

                // All requests have completed
            } catch (e: Exception) {
                // Handle any exceptions that occurred during the requests
                // ...
            }
        }
    }



    fun getUsers_Account_Nominee_FA(){






        utils.startLoadingAnimation()
        db.collection(constants.ACCOUNTS_COLLECTION).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val listAccounts = ArrayList<ModelBankAccount>()
                    if(task.result.size()>0){
                        for (document in task.result)listAccounts.add( document.toObject(ModelBankAccount::class.java).apply { docID = document.id })
                        sharedPrefManager.putAccountList(listAccounts)


                        db.collection(constants.NOMINEE_COLLECTION).get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val listNominee = ArrayList<ModelNominee>()
                                    if(task.result.size()>0){
                                        for (document in task.result) listNominee.add( document.toObject(ModelNominee::class.java).apply { docID = document.id })
                                        sharedPrefManager.putNomineeList(listNominee)


                                        db.collection(constants.FA_COLLECTION).get()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {

                                                    val listFA = ArrayList<ModelFA>()
                                                    if(task.result.size()>0){
                                                        for (document in task.result)listFA.add( document.toObject(ModelFA::class.java).apply { id = document.id })

                                                        sharedPrefManager.putFAList(listFA)






                                                        db.collection(constants.INVESTOR_COLLECTION).get()
                                                            .addOnCompleteListener { task ->
                                                                if (task.isSuccessful) {
                                                                    utils.endLoadingAnimation()

                                                                    val listInvestors = ArrayList<User>()
                                                                    if(task.result.size()>0){
                                                                        for (document in task.result)listInvestors.add( document.toObject(User::class.java).apply { id = document.id })
                                                                        sharedPrefManager.putFAList(listFA)


                                                                    }
                                                                }
                                                                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()




                                                            }
                                                            .addOnFailureListener{
                                                                Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                                                            }



                                                    }
                                                }
                                                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()




                                            }
                                            .addOnFailureListener{
                                                Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                                            }





                                    }
                                }
                                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                            }
                            .addOnFailureListener{
                                Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                            }



                    }
                }
                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

            }


    }


    suspend fun getAccount(){
        //utils.startLoadingAnimation()
        userViewModel.getAccounts()
            .addOnCompleteListener{task ->
                // utils.endLoadingAnimation()

                if (task.isSuccessful) {
                    val list = ArrayList<ModelBankAccount>()
                    if(task.result.size()>0){
                        for (document in task.result)list.add( document.toObject(
                            ModelBankAccount::class.java).apply { docID = document.id })

                        sharedPrefManager.putAccountList(list)




                        //Toast.makeText(mContext, "d2 : "+ task.result.size(), Toast.LENGTH_SHORT).show()


                    }
                }
                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{
                Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

            }
    }

    suspend fun getNominees(){

        nomineeViewModel.getNominees()
            .addOnCompleteListener{task ->
                //utils.endLoadingAnimation()
                if (task.isSuccessful) {
                    val list = ArrayList<ModelNominee>()
                    if(task.result.size()>0){
                        for (document in task.result) list.add( document.toObject(ModelNominee::class.java).apply { docID = document.id })

                        sharedPrefManager.putNomineeList(list)

                        //Toast.makeText(mContext, "d3 : "+ task.result.size(), Toast.LENGTH_SHORT).show()

                    }
                }
                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{
                Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

            }
    }


    suspend fun getFA(){

        faViewModel.getFA()
            .addOnCompleteListener{task ->
                if (task.isSuccessful) {
                    val list = ArrayList<ModelFA>()
                    if(task.result.size()>0){
                        for (document in task.result)list.add( document.toObject(ModelFA::class.java).apply { id = document.id })

                        sharedPrefManager.putFAList(list)

                    }
                }
                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{
                utils.endLoadingAnimation()
                Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

            }
    }









    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}