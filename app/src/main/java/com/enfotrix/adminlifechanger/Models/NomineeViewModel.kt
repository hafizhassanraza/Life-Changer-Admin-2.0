package com.enfotrix.adminlifechanger.Models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.enfotrix.lifechanger.Data.Repo
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.SharedPrefManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class NomineeViewModel (context: Application) : AndroidViewModel(context)  {


    private val userRepo = Repo(context)
    private val sharedPrefManager = SharedPrefManager(context)


    suspend fun addNominee(nominee: ModelNominee): LiveData<Boolean> {
        return userRepo.registerNominee(nominee)
    }
    suspend fun updateNominee(nominee: ModelNominee): LiveData<Boolean> {
        return userRepo.updateNominee(nominee) // set(update) model
    }
    suspend fun getNominee(nominator: String): Task<DocumentSnapshot> {
        return userRepo.getNominee(nominator)
    }

    /*suspend fun getNominees(): Task<QuerySnapshot> {
        return userRepo.nominees()
    }*/
    suspend fun getNominees(): Task<QuerySnapshot> {
        return userRepo.nominees()
    }




}