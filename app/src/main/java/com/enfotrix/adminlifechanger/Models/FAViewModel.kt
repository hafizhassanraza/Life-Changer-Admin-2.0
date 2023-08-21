package com.enfotrix.adminlifechanger.Models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enfotrix.adminlifechanger.Adapters.AdapterFA
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.lifechanger.Adapters.InvestorAccountsAdapter
import com.enfotrix.lifechanger.Data.Repo
import com.enfotrix.lifechanger.SharedPrefManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FAViewModel(context: Application) : AndroidViewModel(context) {

    private var constants= Constants()
    private val db = Firebase.firestore
    private var FACollection = db.collection(constants.FA_COLLECTION)


    private val userRepo = Repo(context)
    private val sharedPrefManager = SharedPrefManager(context)


    private var context = context


    suspend fun isFAExist(CNIC: String): Task<QuerySnapshot> {
        return userRepo.isFAExist(CNIC)
    }

    suspend fun addFA(modelFA: ModelFA): LiveData<Boolean> {
        return userRepo.registerFA(modelFA)
    }

    suspend fun getFA(): Task<QuerySnapshot> {
        return userRepo.getFA()
    }


    fun getOriginalFAList(): List<ModelFA> {
        return sharedPrefManager.getFAList() // Replace this with your actual data source
    }


    fun getFAAdapter(listener: AdapterFA.OnItemClickListener): AdapterFA {
        return AdapterFA(sharedPrefManager.getFAList(), listener)
    }
    fun updateFADetails(id: String, firstName: String, lastName: String, designation: String, cnic: String, password: String,addres:String): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        val faUpdate = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "designantion" to designation,
            "cnic" to cnic,
        "pin" to password,
           "address" to addres
        )
        FACollection.document(id).update(faUpdate)
            .addOnSuccessListener {
                result.value = true
            }.addOnFailureListener {
                result.value = false
            }

        return result
    }




}