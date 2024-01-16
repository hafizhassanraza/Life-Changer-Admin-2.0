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
        setData()
        binding.fbAddEarning.setOnClickListener { addEarningDialog() }


    }

    private fun setData() {
        earningList = sharedPrefManager.getAgentEarningList().filter { it.agentID.equals(modelFA.id) }.sortedByDescending { it.createdAt }
        binding.rvStatment.adapter= AdapterEarning(earningList, this@ActivityEarning)
    }


    override fun onItemClick(modelEarning: ModelEarning) {
        dialogEarningDetails(modelEarning)
    }

    private fun dialogEarningDetails(modelEarning: ModelEarning) {


        //modelEarning.disc
        //// hussain -> create dialog  to  show {modelEarning.disc}

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