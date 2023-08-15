package com.enfotrix.adminlifechanger.Models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.enfotrix.adminlifechanger.Adapters.AdapterFA
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.lifechanger.Adapters.InvestorAccountsAdapter
import com.enfotrix.lifechanger.Data.Repo
import com.enfotrix.lifechanger.SharedPrefManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot

class FAViewModel(context: Application) : AndroidViewModel(context) {


    private val userRepo = Repo(context)
    private val sharedPrefManager = SharedPrefManager(context)
    private var constants = Constants()

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


    ///return list
    fun getOriginalFAList(): List<ModelFA> {
        return sharedPrefManager.getFAList() // Replace this with your actual data source
    }


    fun getFAAdapter(listener: AdapterFA.OnItemClickListener): AdapterFA {
        return AdapterFA(sharedPrefManager.getFAList(), listener)
    }


}