package com.enfotrix.adminlifechanger.Models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.lifechanger.Data.Repo
import com.enfotrix.lifechanger.SharedPrefManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot

class AgentTransactionviewModel(context: Application) : AndroidViewModel(context) {

    private val userRepo = Repo(context)
    private val sharedPrefManager = SharedPrefManager(context)

    private var constants = Constants()

    suspend fun setAgentWithdraw(agentWithdrawModel: AgentWithdrawModel): Task<Void> {
        return userRepo.setAgentWithdraw(agentWithdrawModel)
    }   suspend fun setAgentTransaction(agentTransactionModel: AgentTransactionModel): Task<DocumentReference> {
        return userRepo.setAgentTransaction(agentTransactionModel)
    }
    suspend fun getPendingWithdrawsAgentReq(): Task<QuerySnapshot> {
        return userRepo.getTransactionAgentReq()
    }

    suspend fun getAgentTransaction(): Task<QuerySnapshot> {
        return userRepo.getAgentTransaction()
    }
    suspend fun getAgentWithdraw(): Task<QuerySnapshot> {
        return userRepo.getAgentWithdraw()
    }

}
