package com.enfotrix.adminlifechanger.Fragments

import User
import android.app.Activity
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
import com.enfotrix.adminlifechanger.Adapters.AdapterActiveInvestment
import com.enfotrix.adminlifechanger.Adapters.AdapterActiveInvestors
import com.enfotrix.adminlifechanger.Adapters.InvestorAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.Pdf.PdfTransaction
import com.enfotrix.adminlifechanger.Pdf.PdfUsers
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.FragmentActiveInvestorsBinding
import com.enfotrix.adminlifechanger.databinding.FragmentNewInvestersBinding
import com.enfotrix.adminlifechanger.ui.ActivityInvestorDetails
import com.enfotrix.adminlifechanger.ui.ActivityNewInvestorReqDetails
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


class FragmentActiveInvestors : Fragment() ,  AdapterActiveInvestors.OnItemClickListener{


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
    private val CREATE_PDF_REQUEST_CODE = 123
    val filteredlist = ArrayList<User>()




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

        binding.pdfInvesters.setOnClickListener { generatePDF() }

        runFirestoreRequests()







        return root
    }

    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Investors List.pdf")
        }
        startActivityForResult(intent, CREATE_PDF_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val outputStream = requireContext().contentResolver.openOutputStream(uri)
                if (outputStream != null) {

                    val success =
                        PdfUsers(userlist.filter {  it.status.equals(constant.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt }).generatePdf(
                            outputStream
                        )
                    outputStream.close()
                    if (success) {
                        Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to save", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }



    private fun filter(text: String) {
        // creating a new array list to filter our data.

        if(text.isEmpty()||text.equals("")||text==null){
            binding.rvInvestors.adapter= AdapterActiveInvestors(userlist.filter {  it.status.equals(constant.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt }, listInvestment,this@FragmentActiveInvestors)

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

                binding.rvInvestors.adapter= AdapterActiveInvestors(filteredlist.filter {  it.status.equals(constant.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt }, listInvestment,this@FragmentActiveInvestors)

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
                if (task.isSuccessful) {
                    if(task.result.size()>0){
                        for (document in task.result) {

                            val user =document.toObject(User::class.java)
                            user.id=document.id
                            userlist.add( user)

                        }

                        db.collection(constants.INVESTMENT_COLLECTION)
                            .addSnapshotListener { snapshot, firebaseFirestoreException ->
                                firebaseFirestoreException?.let {
                                    Toast.makeText(mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                                    return@addSnapshotListener
                                }
                                snapshot?.let { documents ->

                                    utils.endLoadingAnimation()

                                    listInvestment = documents.map { it.toObject(InvestmentModel::class.java) } as ArrayList<InvestmentModel>
                                    sharedPrefManager.putActiveInvestment(listInvestment)
                                    binding.rvInvestors.adapter= AdapterActiveInvestors(userlist.filter {  it.status.equals(constant.INVESTOR_STATUS_ACTIVE) }.sortedByDescending { it.createdAt }, listInvestment,this@FragmentActiveInvestors)
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

    override fun onItemClick(user: User) {

        startActivity(Intent(mContext, ActivityInvestorDetails::class.java).putExtra("user",user.toString()))


    }



}