package com.enfotrix.lifechanger.Models

import User
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.enfotrix.adminlifechanger.Adapters.AdapterFA
import com.enfotrix.adminlifechanger.Adapters.InvestorAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.lifechanger.Adapters.InvestorAccountsAdapter
import com.enfotrix.lifechanger.Data.Repo
import com.enfotrix.lifechanger.SharedPrefManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask


class UserViewModel(context: Application) : AndroidViewModel(context) {

    private val userRepo = Repo(context)
    private val sharedPrefManager = SharedPrefManager(context)
    private var constants = Constants()

    private var context = context

    /*suspend fun registerUser(user: User): LiveData<Pair<Boolean, String>> {
        return userRepo.registerUser(user)
    }*/

    //suspend fun uploadProfilePic(uri: Uri): LiveData<Uri> = userRepo.uploadProfilePic(uri)
    suspend fun loginUser(CNIC: String, PIN: String): Task<QuerySnapshot> {
        return userRepo.loginUser(CNIC, PIN)
    }

    fun saveLoginAuth(user: User, token: String, loggedIn: Boolean) {
        sharedPrefManager.saveUser(user)
        sharedPrefManager.putToken(token)
        sharedPrefManager.setLogin(loggedIn)
    }


    /*suspend fun isInvestorExist(CNIC: String): MutableLiveData<Pair<Boolean, String>> {

            val result = MutableLiveData<Pair<Boolean, String>>()

            userRepo.isInvestorExist(CNIC)
                 .addOnSuccessListener {
                     var user:User?=null
                     for (document in it) user = document.toObject<User>()
                     result.postValue(Pair( true,"gdfgfsdgdfg"))
                     if(user?.status.equals(INVESTOR_STATUS_ACTIVE)) result.postValue(Pair(true, INVESTOR_CNIC_EXIST))
                     else if(user?.status.equals(INVESTOR_STATUS_BLOCKED))result.postValue(Pair(false, INVESTOR_CNIC_BLOCKED))
                     else if(it==null)result.postValue(Pair(false, INVESTOR_CNIC_NOT_EXIST))
                 }
                .addOnFailureListener{
                    result.postValue(Pair( true,"aaaaaaaaa"))

                }
            return result
        }*/
    suspend fun isInvestorExist(CNIC: String): Task<QuerySnapshot> {
        return userRepo.isInvestorExist(CNIC)
    }

    suspend fun addUser(user: User): LiveData<Boolean> {
        return userRepo.registerUser(user)
    }

    suspend fun updateUser(user: User): LiveData<Boolean> {
        return userRepo.updateUser(user)
    }

    suspend fun setUser(user: User): Task<Void> {
        return userRepo.setUser(user)
    }

    suspend fun addUserAccount(bankAccount: ModelBankAccount): LiveData<Boolean> {
        return userRepo.registerBankAccount(bankAccount)
    }

    suspend fun getUserAccounts(token: String): Task<QuerySnapshot> {
        return userRepo.userAccounts(token)
    }

    suspend fun getAccounts(): Task<QuerySnapshot> {
        return userRepo.accounts()
    }

    suspend fun getUsers(): Task<QuerySnapshot> {
        return userRepo.users()
    }

    suspend fun uploadPhoto(imageUri: Uri, type: String): UploadTask {
        return userRepo.uploadPhoto(imageUri, type)
    }

    suspend fun uploadPhotoRefrence(imageUri: Uri, type: String): StorageReference {
        return userRepo.uploadPhotoRefrence(imageUri, type)
    }

    fun getInvestorsAdapter(
        fromActivity: String,
        listener: InvestorAdapter.OnItemClickListener
    ): InvestorAdapter {
        return InvestorAdapter(
            fromActivity,
            sharedPrefManager.getUsersList().filter { it.fa_id.isNullOrEmpty() },
            listener
        )
    }

    fun getAssignedInvestorsAdapter(
        fa_ID: String,
        fromActivity: String,
        listener: InvestorAdapter.OnItemClickListener
    ): InvestorAdapter {
        return InvestorAdapter(
            fromActivity,
            sharedPrefManager.getUsersList().filter { it.fa_id.equals(fa_ID) },
            listener
        )
    }


    /*
        fun getInvestorAccountsAdapter( fromActivity:String,listener: InvestorAccountsAdapter.OnItemClickListener): InvestorAccountsAdapter {
            return InvestorAccountsAdapter(fromActivity,sharedPrefManager.getInvestorBankList(), listener)
        }
        fun getAdminAccountsAdapter( fromActivity:String,listener: InvestorAccountsAdapter.OnItemClickListener): InvestorAccountsAdapter {
            return InvestorAccountsAdapter(fromActivity,sharedPrefManager.getAdminBankList(), listener)
        }*/


    //suspend fun getBalance(): String = userRepo.getBalance()

    /*suspend fun updateUser(
        firstName: String,
        lastName: String,
        address: String,
        mobileNumber: String
    ): LiveData<Boolean> {
        return userRepo.updateUser(firstName, lastName, address, mobileNumber)
    }*/


    fun getusers(fa_ID: String): List<User> {
        return sharedPrefManager.getUsersList().filter {
            it.fa_id.equals(fa_ID)
        }
    }
        fun getusers2(fa_ID: String): List<User> {
        return sharedPrefManager.getUsersList().filter {
            it.fa_id.isEmpty()
        }


    }
}