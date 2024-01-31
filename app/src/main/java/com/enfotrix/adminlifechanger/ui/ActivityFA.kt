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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.AdapterActiveInvestors
import com.enfotrix.adminlifechanger.Adapters.AdapterFA
import com.enfotrix.adminlifechanger.Adapters.InvestorAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Pdf.pdfFA
import com.enfotrix.adminlifechanger.databinding.ActivityFaBinding
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.Objects

class ActivityFA : AppCompatActivity(), AdapterFA.OnItemClickListener {


    private val faViewModel: FAViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var mContext: Context
    private lateinit var binding: ActivityFaBinding

    private val userlist = ArrayList<User>()

    private var listFA = ArrayList<ModelFA>()

    private var listInvestment = ArrayList<InvestmentModel>()

    private val db = Firebase.firestore
    private val firebaseStorage = Firebase.storage
    private val storageRef = firebaseStorage.reference


    private val CREATE_PDF_REQUEST_CODE = 123


    var constant = Constants()
    private lateinit var utils: Utils
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this@ActivityFA
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        supportActionBar?.title = "Financial Advisor"

        binding.fbAddFA.setOnClickListener {
            startActivity(Intent(mContext, ActivityAddFA::class.java))
        }
        binding.rvFA.layoutManager = LinearLayoutManager(mContext)

        binding.pdfFA.setOnClickListener {
            generatePDF()
        }

        binding.svAgents.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })
        getFa()

    }

    private fun generatePDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Financial Advisors.pdf")
        }
        startActivityForResult(intent, CREATE_PDF_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val outputStream = mContext.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val success =
                        pdfFA(listFA).generatePdf(
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


    fun getFa() {


        utils.startLoadingAnimation()


        lifecycleScope.launch {


            userViewModel.getUsers()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result.size() > 0) {
                            for (document in task.result) {

                                val user = document.toObject(User::class.java)
                                user.id = document.id
                                userlist.add(user)

                            }

                            db.collection(constants.INVESTMENT_COLLECTION)
                                .addSnapshotListener { snapshot, firebaseFirestoreException ->
                                    firebaseFirestoreException?.let {
                                        Toast.makeText(
                                            mContext,
                                            it.message.toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@addSnapshotListener
                                    }
                                    snapshot?.let { documents ->


                                        listInvestment =
                                            documents.map { it.toObject(InvestmentModel::class.java) } as ArrayList<InvestmentModel>
                                        sharedPrefManager.putActiveInvestment(listInvestment)




                                        lifecycleScope.launch {
                                            faViewModel.getFA()
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        utils.endLoadingAnimation()
                                                        if (task.result.size() > 0) {


                                                            for (document in task.result) {

                                                                //listFA.add(document.toObject(ModelFA::class.java).apply { id = document.id })

                                                                var modelFA =
                                                                    document.toObject(ModelFA::class.java)
                                                                        .apply { id = document.id }


                                                                var InActiveInvestment_Counter = 0
                                                                var ActiveInvestment_Counter = 0
                                                                var Profit_Counter = 0
                                                                var totalinvestors = 0
                                                                for (investor in userlist) {
                                                                    if (investor.fa_id.equals(
                                                                            modelFA.id
                                                                        )
                                                                    ) {
                                                                        totalinvestors++


                                                                        var investment =
                                                                            listInvestment.find {
                                                                                it.investorID.equals(
                                                                                    investor.id
                                                                                )
                                                                            }
                                                                        var InActiveInvestment = 0
                                                                        var ActiveInvestment = 0
                                                                        var Profit = 0
                                                                        if (investment != null) {
                                                                            if (!investment.investmentBalance.isNullOrEmpty()) ActiveInvestment =
                                                                                investment.investmentBalance.toInt()
                                                                            if (!investment.lastInvestment.isNullOrEmpty()) InActiveInvestment =
                                                                                investment.lastInvestment.toInt()
                                                                            if (!investment.lastProfit.isNullOrEmpty()) Profit =
                                                                                investment.lastProfit.toInt()
                                                                        }

                                                                        ActiveInvestment_Counter =
                                                                            ActiveInvestment_Counter + ActiveInvestment
                                                                        InActiveInvestment_Counter =
                                                                            InActiveInvestment_Counter + InActiveInvestment
                                                                        Profit_Counter =
                                                                            Profit_Counter + Profit
                                                                    }

                                                                }

                                                                modelFA.phone =
                                                                    InActiveInvestment_Counter.toString()
                                                                modelFA.cnic =
                                                                    ActiveInvestment_Counter.toString()
                                                                modelFA.address =
                                                                    Profit_Counter.toString()
                                                                modelFA.cnic_back =
                                                                    totalinvestors.toString()

                                                                listFA.add(modelFA)


                                                            }


                                                            binding.rvFA.adapter =
                                                                AdapterFA(listFA, this@ActivityFA)


                                                        }
                                                    } else Toast.makeText(
                                                        mContext,
                                                        constants.SOMETHING_WENT_WRONG_MESSAGE,
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                }
                                                .addOnFailureListener {
                                                    utils.endLoadingAnimation()
                                                    Toast.makeText(
                                                        mContext,
                                                        it.message + "",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }


                                        /* binding.svAgents.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                                             override fun onQueryTextSubmit(query: String): Boolean {
                                                 return false
                                             }
                                             override fun onQueryTextChange(newText: String): Boolean {
                                                 filter(newText)
                                                 return false
                                             }
                                         })*/


                                    }
                                }


                            //Toast.makeText(mContext, "d1 : "+ task.result.size(), Toast.LENGTH_SHORT).show()

                        }
                    } else Toast.makeText(
                        mContext,
                        constants.SOMETHING_WENT_WRONG_MESSAGE,
                        Toast.LENGTH_SHORT
                    ).show()

                }
                .addOnFailureListener {
                    Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()

                }


        }


    }


    fun getUser() {
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.getUsers()
                .addOnCompleteListener { task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val list = ArrayList<User>()
                        if (task.result.size() > 0) {
                            for (document in task.result) list.add(
                                document.toObject(User::class.java).apply { id = document.id })
                            sharedPrefManager.putUserList(list)

                        }
                    } else Toast.makeText(
                        mContext,
                        constants.SOMETHING_WENT_WRONG_MESSAGE,
                        Toast.LENGTH_SHORT
                    ).show()

                }
                .addOnFailureListener {
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()

                }
        }


    }


    private fun filter(text: String) {
        val filteredList = if (text.isBlank()) {
            AdapterFA(listFA, this@ActivityFA)
        } else {
            val filteredModels = listFA.filter { modelFA ->
                modelFA.firstName.toLowerCase(Locale.getDefault())
                    .contains(text.toLowerCase(Locale.getDefault()))
            }
            AdapterFA(filteredModels, this@ActivityFA)
        }

        binding.rvFA.adapter = filteredList
    }


    fun getAccount() {
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.getAccounts()
                .addOnCompleteListener { task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        val list = ArrayList<ModelBankAccount>()
                        if (task.result.size() > 0) {
                            for (document in task.result) list.add(
                                document.toObject(
                                    ModelBankAccount::class.java
                                ).apply { docID = document.id })
                            sharedPrefManager.putAccountList(list)

                        }
                    } else Toast.makeText(
                        mContext,
                        constants.SOMETHING_WENT_WRONG_MESSAGE,
                        Toast.LENGTH_SHORT
                    ).show()

                }
                .addOnFailureListener {
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()

                }
        }
    }


    override fun onItemClick(modelFA: ModelFA) {


        startActivity(
            Intent(mContext, ActivityFADetails::class.java).putExtra(
                "FA",
                modelFA.toString()
            )
        )

    }

    override fun onDeleteClick(modelFA: ModelFA) {

    }


}

