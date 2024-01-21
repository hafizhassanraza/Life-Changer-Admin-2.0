package com.enfotrix.adminlifechanger.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.AdapterEarning
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.ModelEarning
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityAgentEarningBinding
import com.enfotrix.adminlifechanger.databinding.ActivityEarningBinding
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ActivityEarning : AppCompatActivity() , AdapterEarning.OnItemClickListener {


    private val db = Firebase.firestore

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding : ActivityEarningBinding

    private lateinit var earningList : List<ModelEarning>
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog
    private lateinit var modelFA: ModelFA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEarningBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mContext=this@ActivityEarning
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvStatment.layoutManager = LinearLayoutManager(mContext)




        modelFA = ModelFA.fromString(intent.getStringExtra("Fa").toString())!!
        binding.fbAddEarning.setOnClickListener { addEarningDialog() }

        setData()


    }

    private fun setData() {
        earningList = sharedPrefManager.getAgentEarningList().filter { it.agentID.equals(modelFA.id) }.sortedByDescending { it.createdAt }
        binding.rvStatment.adapter= AdapterEarning(earningList, this@ActivityEarning)
    }


    override fun onItemClick(modelEarning: ModelEarning) {
        dialogEarningDetails(modelEarning)
    }

    private fun dialogEarningDetails(modelEarning: ModelEarning) {


            val dialog = Dialog(mContext)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_earning_details)

            // Initialize the dialog views
            val tvHeaderBank = dialog.findViewById<TextView>(R.id.tvHeaderBank)
            val tvHeaderBankDisc = dialog.findViewById<TextView>(R.id.tvHeaderBankDisc)
            val oldBalance = dialog.findViewById<TextView>(R.id.oldBalance)
            val earningAmount = dialog.findViewById<TextView>(R.id.earningAmount)
            val clearance = dialog.findViewById<TextView>(R.id.clearance)
            val date = dialog.findViewById<TextView>(R.id.date)
            val remarks = dialog.findViewById<TextView>(R.id.remarks)

            // Set data to the dialog views
            tvHeaderBankDisc.text = ""
            oldBalance.text = "${modelEarning.balance}"
            earningAmount.text = " ${modelEarning.amount}"
            clearance.text = "${modelEarning.amount}"
            date.text = " ${modelEarning.createdAt.toDate()}" // Convert timestamp to date
            remarks.text = "${modelEarning.disc}"

            dialog.show()


    }



    fun getData() {


        db.collection(constants.FA_COLLECTION).document(modelFA.id)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                documentSnapshot?.let { snapshot ->
                    if (snapshot.exists()) {
                        modelFA= snapshot.toObject<ModelFA>()!!

                        // Update the shared preference with the modified modelFA
                        sharedPrefManager.putFAList(
                            sharedPrefManager.getFAList().map {
                                if (it.id == modelFA.id) modelFA else it
                            }
                        )


                    }


                }
            }


    }


    fun addEarningDialog(){

        dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_profit)

        val etBalance = dialog.findViewById<EditText>(R.id.etBalance)
        val etRemarks = dialog.findViewById<EditText>(R.id.etRemarks)
        val AddProfit = dialog.findViewById<Button>(R.id.AddProfit)
        AddProfit.setOnClickListener {


            if(etBalance.text.isNullOrEmpty()) Toast.makeText(mContext, "Please enter the amount", Toast.LENGTH_SHORT).show()
            else if(etRemarks.text.isNullOrEmpty()) Toast.makeText(mContext, "Please enter the remarks", Toast.LENGTH_SHORT).show()
            else {
                addEarning(
                    ModelEarning(
                        "",
                        etBalance.text.toString(),
                        modelFA.profit,
                        etRemarks.text.toString(),
                        modelFA.id,
                        constants.EARNING_STATUS_PENDING
                    )
                )
                dialog.dismiss()

            }
        }
        dialog.show()

    }
    fun addEarning(modelEarning: ModelEarning) {
        utils.startLoadingAnimation()

        var earning = modelEarning.amount.toString()?.toInt() ?: 0
        var previousBalance = 0
        if(modelFA.profit!="" || modelFA.profit!=null ) previousBalance=modelFA.profit.toInt()
        earning += previousBalance
        modelFA.profit=earning.toInt().toString()

        var documentRef=  db.collection(constants.AGENT_EARNING_COLLECTION).document()
        modelEarning.docID=documentRef.id
        documentRef.set(modelEarning).addOnCompleteListener{
            if(it.isSuccessful){
                db.collection(constants.FA_COLLECTION).document(modelFA.id).set(modelFA)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            db.collection(constants.AGENT_EARNING_COLLECTION).get()
                                .addOnCompleteListener {task->
                                    if(task.isSuccessful){
                                        Thread.sleep(200)
                                        utils.endLoadingAnimation()
                                        sharedPrefManager.putAgentEarningList(task.result!!.documents.mapNotNull { document -> document.toObject(
                                            ModelEarning::class.java)?.apply { docID = document.id } })


                                        sharedPrefManager.putFAList(
                                            sharedPrefManager.getFAList().map {
                                                if (it.id == modelFA.id) modelFA else it
                                            }
                                        )
                                        getData()
                                        setData()

                                    }
                                }
                        }
                    }

            }
            else Toast.makeText(mContext, ""+it.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}