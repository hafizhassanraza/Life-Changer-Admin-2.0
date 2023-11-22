package com.enfotrix.adminlifechanger.Models

import User
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.lifechanger.Adapters.InvestorAccountsAdapter
import com.enfotrix.lifechanger.Adapters.ProfitTaxAdapter
import com.enfotrix.lifechanger.Adapters.TransactionsAdapter
import com.enfotrix.lifechanger.Data.Repo
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.SharedPrefManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class InvestmentViewModel(context: Application) : AndroidViewModel(context) {

    private val userRepo = Repo(context)
    private val sharedPrefManager = SharedPrefManager(context)

    private var constants= Constants()


    suspend fun addInvestment(investment: InvestmentModel): LiveData<Boolean> {
        return userRepo.addInvestment(investment)
    }
    suspend fun setInvestment(investment: InvestmentModel): Task<Void> {
        return userRepo.setInvestment(investment)
    }
    suspend fun getProfitTax(token: String): Task<QuerySnapshot> {
        return userRepo.getProfitTax(token)
    }

    suspend fun addTransactionReq(transactionModel: TransactionModel): LiveData<Boolean> {
        return userRepo.addTransactionReq(transactionModel)
    }
    suspend fun setTransactionReq(transactionModel: TransactionModel): Task<Void> {
        return userRepo.setTransactionReq(transactionModel)
    }
    fun getProfitAdapter( from:String): ProfitTaxAdapter {
        return ProfitTaxAdapter(from,sharedPrefManager.getProfitTaxList().filter{ it.type.equals(constants.PROFIT_TYPE) }.sortedByDescending { it.createdAt })
    }
    fun getTaxAdapter( from:String): ProfitTaxAdapter {
        return ProfitTaxAdapter(from,sharedPrefManager.getProfitTaxList().filter{ it.type.equals(constants.TAX_TYPE) }.sortedByDescending { it.createdAt })
    }
  /*  fun getPendingWithdrawReqAdapter( from:String): TransactionsAdapter {
        return TransactionsAdapter(from,sharedPrefManager.getWithdrawReqList().filter{ it.status.equals(constants.TRANSACTION_STATUS_PENDING) }.sortedByDescending { it.createdAt })
    }
    fun getPendingInvestmentReqAdapter( from:String): TransactionsAdapter {
        return TransactionsAdapter(from,sharedPrefManager.getInvestmentReqList().filter{ it.status.equals(constants.TRANSACTION_STATUS_PENDING) }.sortedByDescending { it.createdAt })
    }
    fun getApprovedWithdrawReqAdapter( from:String): TransactionsAdapter {
        return TransactionsAdapter(from,sharedPrefManager.getWithdrawReqList().filter{ it.status.equals(constants.TRANSACTION_STATUS_APPROVED) }.sortedByDescending { it.createdAt })
    }
    fun getApprovedInvestmentReqAdapter( from:String): TransactionsAdapter {
        return TransactionsAdapter(from,sharedPrefManager.getInvestmentReqList().filter{ it.status.equals(constants.TRANSACTION_STATUS_APPROVED) }.sortedByDescending { it.createdAt })
    }*/




    suspend fun getUserInvestment(ID:String): Task<DocumentSnapshot> {
        return userRepo.getUserInvestment(ID)
    }

    suspend fun getPendingWithdrawsReq(): Task<QuerySnapshot> {
        return userRepo.getTransactionReq(constants.TRANSACTION_STATUS_PENDING,constants.TRANSACTION_TYPE_WITHDRAW)
    }

    suspend fun getApprovedWithdrawsReq(): Task<QuerySnapshot> {
        return userRepo.getTransactionReq(constants.TRANSACTION_STATUS_APPROVED,constants.TRANSACTION_TYPE_WITHDRAW)
    }
    suspend fun getPendingInvestmentsReq(): Task<QuerySnapshot> {
        return userRepo.getTransactionReq(constants.TRANSACTION_STATUS_PENDING,constants.TRANSACTION_TYPE_INVESTMENT)
    }

    suspend fun getApprovedInvestmentsReq(): Task<QuerySnapshot> {
        return userRepo.getTransactionReq(constants.TRANSACTION_STATUS_APPROVED,constants.TRANSACTION_TYPE_INVESTMENT)
    }    suspend fun getApprovedProfitsReq(): Task<QuerySnapshot> {
        return userRepo.getTransactionReq(constants.TRANSACTION_STATUS_APPROVED,constants.TRANSACTION_TYPE_Profit)
    } suspend fun getApprovedTaxReq(): Task<QuerySnapshot> {
        return userRepo.getTransactionReq(constants.TRANSACTION_STATUS_APPROVED,constants.TRANSACTION_TYPE_Tax)
    }



}