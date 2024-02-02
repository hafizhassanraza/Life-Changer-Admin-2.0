package com.enfotrix.adminlifechanger.ui

import User
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.AdapterActiveInvestors
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.InvestmentViewModel
import com.enfotrix.adminlifechanger.Models.NomineeViewModel
import com.enfotrix.adminlifechanger.Pdf.PdfUsers
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
        binding.pdfAllInvestors.setOnClickListener {
             generatePDF()
        }


        setData()

    }




    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "All Investors.pdf")
        }
        startActivityForResult(intent, 123)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val outputStream = mContext.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val success =
                        PdfUsers(
                            sharedPrefManager.getUsersList().filter { it.status==constant.INVESTOR_STATUS_ACTIVE}
                        ).generatePdf(
                            outputStream
                        )
                    outputStream.close()
                    if (success) {
                        Toast.makeText(mContext, "Saved successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(mContext, "Failed to save", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }



    private fun setData() {


        investmentList = sharedPrefManager.getInvestmentList()
        userlist = sharedPrefManager.getUsersList().filter { it.status.equals(constant.INVESTOR_STATUS_ACTIVE) }
        userlist.sortedByDescending { it.firstName }

        binding.rvInvestors.adapter= AdapterActiveInvestors(userlist, investmentList,this@ActivityInvestors)

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
        val filteredList = userlist.filter { user ->
            user.firstName.toLowerCase(Locale.getDefault()).startsWith(text.toLowerCase(Locale.getDefault()))
        }.filter { it.status.equals(constants.INVESTOR_STATUS_ACTIVE) }
            .sortedByDescending { it.firstName }

        binding.rvInvestors.adapter = AdapterActiveInvestors(filteredList, investmentList, this@ActivityInvestors)
    }




    override fun onItemClick(user: User) {
        startActivity(Intent(mContext, ActivityInvestorDetails::class.java).putExtra("user",user.toString()))
    }
}