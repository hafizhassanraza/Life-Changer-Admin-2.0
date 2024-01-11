package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.AdapterAccounts
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityAccountsBinding
import com.enfotrix.lifechanger.Adapters.InvestorAccountsAdapter
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class ActivityAccounts : AppCompatActivity()  , AdapterAccounts.OnItemClickListener  {


    private val db = Firebase.firestore

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding : ActivityAccountsBinding

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog


    private lateinit var adapter: InvestorAccountsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mContext=this@ActivityAccounts
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvInvestorAccounts.layoutManager = LinearLayoutManager(mContext)
        setTitle("Admin Bank Accounts")


        getAccounts()

        binding.fbAddInvestorAccount.setOnClickListener {

            addAccountDialog()


        }

    }

    fun getAccounts(){
        binding.rvInvestorAccounts.adapter=
            AdapterAccounts(sharedPrefManager.getAccountList().filter { it.account_holder.equals(constants.ADMIN) }, this@ActivityAccounts)
    }

    fun addAccountDialog(){

        dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_account)
        val tvHeaderBank = dialog.findViewById<TextView>(R.id.tvHeaderBank)
        val tvHeaderBankDisc = dialog.findViewById<TextView>(R.id.tvHeaderBankDisc)
        val spBank = dialog.findViewById<Spinner>(R.id.spBank)
        val etAccountTittle = dialog.findViewById<EditText>(R.id.etAccountTittle)
        val etAccountNumber = dialog.findViewById<EditText>(R.id.etAccountNumber)
        val btnAddAccount = dialog.findViewById<Button>(R.id.btnAddAccount)
        btnAddAccount.setOnClickListener {
            updateAdminBankList(
                ModelBankAccount(
                    "",
                    spBank.selectedItem.toString(),
                    etAccountTittle.text.toString(),
                    etAccountNumber.text.toString(),
                    constants.ADMIN
                )
            )
            dialog.dismiss()
        }
        dialog.show()


    }
    fun updateAdminBankList(modelBankAccount: ModelBankAccount) {

        utils.startLoadingAnimation()


        var documentRef=  db.collection(constants.ACCOUNTS_COLLECTION).document()
        modelBankAccount.docID=documentRef.id
        documentRef.set(modelBankAccount).addOnCompleteListener{

            if(it.isSuccessful){
                db.collection(constants.ACCOUNTS_COLLECTION).get()
                    .addOnCompleteListener {task->
                        if(task.isSuccessful){
                            Thread.sleep(200)
                            utils.endLoadingAnimation()
                            sharedPrefManager.putAccountList(task.result!!.documents.mapNotNull { document -> document.toObject(ModelBankAccount::class.java)?.apply { docID = document.id } })
                            getAccounts()
                        }
                    }
            }
        }
    }
    fun deleteAdminBankAccount(modelBankAccount: ModelBankAccount) {

        utils.startLoadingAnimation()


        db.collection(constants.ACCOUNTS_COLLECTION).document(modelBankAccount.docID).delete()
            .addOnCompleteListener {task->
                if(task.isSuccessful){
                    db.collection(constants.ACCOUNTS_COLLECTION).get()
                        .addOnCompleteListener {task->
                            if(task.isSuccessful){
                                Thread.sleep(200)
                                utils.endLoadingAnimation()
                                sharedPrefManager.putAccountList(task.result!!.documents.mapNotNull { document -> document.toObject(ModelBankAccount::class.java)?.apply { docID = document.id } })
                                getAccounts()
                            }
                        }
                }
            }
    }



    override fun onItemClick(modelBankAccount: ModelBankAccount) {

    }

    override fun onDeleteClick(modelBankAccount: ModelBankAccount) {

        if(sharedPrefManager.getAccountList().filter { it.account_holder.equals(constants.ADMIN) }.count()>1) deleteAdminBankAccount(modelBankAccount)
        else Toast.makeText(mContext, "you should contain at least one bank account", Toast.LENGTH_SHORT).show()

    }
}