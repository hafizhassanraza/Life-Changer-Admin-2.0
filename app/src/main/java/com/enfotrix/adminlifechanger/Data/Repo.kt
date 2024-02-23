package com.enfotrix.lifechanger.Data

import com.enfotrix.lifechanger.SharedPrefManager
import User
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.AgentTransactionModel
import com.enfotrix.adminlifechanger.Models.AgentWithdrawModel
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.Models.NotificationModel
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.Models.TransactionModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage


class Repo(val context: Context) {






    private var constants= Constants()
    private val sharedPrefManager = SharedPrefManager(context)
    private val token = sharedPrefManager.getToken()



    ///////////////////////////   FIREBASE    //////////////////////////////////

    private val db = Firebase.firestore
    private val firebaseStorage = Firebase.storage
    private val storageRef = firebaseStorage.reference

    private var InvestorsCollection = db.collection(constants.INVESTOR_COLLECTION)
    private var FACollection = db.collection(constants.FA_COLLECTION)
    private var AgentTransactionCollection = db.collection(constants.AGENT_TRANSACTION)
    private val NomineesCollection = db.collection(constants.NOMINEE_COLLECTION)
    private val AccountsCollection = db.collection(constants.ACCOUNTS_COLLECTION)
    private val InvestmentCollection = db.collection(constants.INVESTMENT_COLLECTION)
    private val TransactionsReqCollection = db.collection(constants.TRANSACTION_REQ_COLLECTION)
    private val ProfitTaxCollection = db.collection(constants.PROFIT_TAX_COLLECTION)
    private val WithdrawCollection = db.collection(constants.WITHDRAW_COLLECTION)
    private val NotificationCollection = db.collection(constants.NOTIFICATION_COLLECTION)


    ///////////////////////////////////////////////////////////////////////////////



    /*class MyApplication : Application() {
        override fun onCreate() {
            super.onCreate()
            FirebaseApp.initializeApp(this)
        }
    }*/

    /*suspend fun isInvestorExist(CNIC: String): LiveData<Pair<Boolean, String>> {
        val result = MutableLiveData<Pair<Boolean, String>>()

        InvestorsCollection.whereEqualTo(INVESTOR_CNIC, CNIC).get()
            .addOnSuccessListener {documents ->
            *//*    val user: User
                 user = it.documents.firstOrNull()?.toObject(User::class.java)!!*//*


                //for (document in documents) user = document.toObject<User>()

                var user:User?=null
                for (document in documents) user= document.toObject(User::class.java)

                if(user?.status.equals(INVESTOR_STATUS_ACTIVE)){
                    result.value?.first ?: true
                    result.value?.second ?: INVESTOR_CNIC_EXIST
                }
                else if(user?.status.equals(INVESTOR_STATUS_BLOCKED)){
                    result.value?.first ?: false
                    result.value?.second ?: INVESTOR_CNIC_BLOCKED
                }
                else if(documents.size()==0){
                    result.value?.first ?: false
                    result.value?.second ?: INVESTOR_CNIC_NOT_EXIST
                }
            }
            .addOnFailureListener{


                result.value?.first ?: false
                result.value?.second ?: INVESTOR_CNIC_NOT_EXIST
            }

        return result
    }*/


    suspend fun isInvestorExist(CNIC: String): Task<QuerySnapshot> {
        return InvestorsCollection.whereEqualTo(constants.INVESTOR_CNIC, CNIC).get()
    }

    suspend fun isFAExist(CNIC: String): Task<QuerySnapshot> {
        return FACollection.whereEqualTo(constants.INVESTOR_CNIC, CNIC).get()
    }
    suspend fun loginUser(CNIC: String, PIN: String): Task<QuerySnapshot> {
        return InvestorsCollection.whereEqualTo(constants.INVESTOR_CNIC, CNIC).whereEqualTo(constants.INVESTOR_PIN,PIN).get()
    }

    suspend fun getNominee( nominator: String): Task<DocumentSnapshot> {
        return NomineesCollection.document(nominator).get()
       // return NomineesCollection.whereEqualTo("nominator", nominator).get()
    }

    suspend fun registerFA(modelFA:ModelFA): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        modelFA.status= constants.INVESTOR_STATUS_INCOMPLETE
        modelFA.id= FACollection.document().id


        FACollection.add(modelFA).addOnSuccessListener { documents ->
modelFA.id=documents.id
            updateFA(modelFA,documents.id)
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }
    /*suspend fun addFaProfit(agentTransactionModel: AgentTransactionModel,modelFA: ModelFA): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()


        FAProfitCollection.add(agentTransactionModel).addOnSuccessListener { documents ->
            FAProfitCollection.document(modelFA.id).set(AgentTransactionModel(modelFA.id))
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }*/


    suspend fun addFaProfit(agentTransactionModel: AgentTransactionModel): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        // Use the specified document reference to set the data in FAProfitCollection
        AgentTransactionCollection.add(agentTransactionModel)
            .addOnSuccessListener {
                result.value = true
            }
            .addOnFailureListener {
                result.value = false
            }

        return result
    }


    fun updateFA(user: ModelFA,id:String): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        FACollection.document(id).set(user).addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }


    suspend fun registerUser(user: User): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        user.status=constants.INVESTOR_STATUS_INCOMPLETE
        InvestorsCollection.add(user).addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }




    suspend fun updateUser(user: User): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        InvestorsCollection.document(sharedPrefManager.getToken()).set(user).addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }
    suspend fun setUser(user: User): Task<Void> {
        return InvestorsCollection.document(user.id).set(user)
    }
    suspend fun registerBankAccount(bankAccount: ModelBankAccount): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        bankAccount.account_holder=sharedPrefManager.getToken()
        var documentRef= AccountsCollection.document()
        bankAccount.docID=documentRef.id

        documentRef.set(bankAccount).addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }
    suspend fun userAccounts(token: String ): Task<QuerySnapshot> {
        return AccountsCollection.whereEqualTo(constants.ACCOUNT_HOLDER, token).get()
    }
    suspend fun accounts(): Task<QuerySnapshot> {
        return AccountsCollection.get()
    }
    suspend fun users(): Task<QuerySnapshot> {
        return InvestorsCollection.get()
    }
    suspend fun nominees(): Task<QuerySnapshot> {
        return NomineesCollection.get()
    }
    suspend fun getFA(): Task<QuerySnapshot> {
        return FACollection.get()
    } suspend fun getAgentTransactions(): Task<QuerySnapshot> {
        return AgentTransactionCollection.get()
    }suspend fun getAgentWithdraw(): Task<QuerySnapshot> {
        return WithdrawCollection.get()
    }
    suspend fun registerNominee(nominee: ModelNominee): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()


        //nominee.docID= NomineesCollection.document().id
        nominee.docID= nominee.nominator
        NomineesCollection.document(nominee.docID).set(nominee).addOnSuccessListener { documents ->
            sharedPrefManager.saveNominee(nominee)// id overwrite
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }
    suspend fun updateNominee(nominee: ModelNominee): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        NomineesCollection.document(nominee.docID).set(nominee).addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }


    suspend fun uploadPhoto(imageUri: Uri, type:String): UploadTask {
        return storageRef.child(    type+"/"+sharedPrefManager.getToken()).putFile(imageUri)
    }

    suspend fun uploadPhotoRefrence(imageUri:Uri,type:String): StorageReference {

        return storageRef.child(    type+"/"+sharedPrefManager.getToken())
    }




    suspend fun getProfitTax(token: String  ): Task<QuerySnapshot> {
        return ProfitTaxCollection.whereEqualTo(constants.INVESTOR_ID, token).get()
    }


    suspend fun addTransactionReq(transactionModel: TransactionModel): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        TransactionsReqCollection.add(transactionModel)
            .addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }
    suspend fun deleteNotifications(notificationModel: NotificationModel): Task<Void> {

        return NotificationCollection.document(notificationModel.id).delete()

    }
    suspend fun deleteTransactionReq(transactionModel: TransactionModel): Task<Void> {

        return TransactionsReqCollection.document(transactionModel.id).delete()

    }
    suspend fun setTransactionReq(transactionModel: TransactionModel): Task<Void> {

        return TransactionsReqCollection.document(transactionModel.id).set(transactionModel)

        /*val result = MutableLiveData<Boolean>()
        TransactionsReqCollection.document(transactionModel.id).set(transactionModel)
            .addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result*/
    }
    suspend fun setInvestment(investment: InvestmentModel): Task<Void> {
        return InvestmentCollection.document(investment.investorID).set(investment)

    }
    suspend fun setAgentWithdraw(agentWithdrawModel: AgentWithdrawModel): Task<Void> {

        return WithdrawCollection.document(agentWithdrawModel.id).set(agentWithdrawModel)

    }
    suspend fun setAgentTransaction(agentTransactionModel: AgentTransactionModel): Task<DocumentReference> {
      /*  return AgentTransactionCollection.document(gentTransactionModel.fa_id).set(gentTransactionModel)*/
        return  AgentTransactionCollection.add(agentTransactionModel)
    }

    suspend fun addInvestment(investment: InvestmentModel): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        InvestmentCollection.document(investment.investorID).set(investment)
            .addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }



//    suspend fun uploadProfilePic(imageUri: Uri): LiveData<Uri> {
//        val uri = MutableLiveData<Uri>()
//        storageRef.child(sharedPrefManager.getDocID()).putFile(imageUri).addOnSuccessListener {
//            uri.value = it.uploadSessionUri
//        }.addOnFailureListener {
//            Toast.makeText(context, "Failed to upload profile pic", Toast.LENGTH_SHORT).show()
//        }
//
//        return uri
//    }

   /* suspend fun isInvestorExist(CNIC: String): LiveData<Pair<Boolean, String>> {
        val result = MutableLiveData<Pair<Boolean, String>>()

        InvestorsCollection.whereEqualTo(INVESTOR_CNIC, CNIC).get()
            .addOnSuccessListener {

                var user:User?=null
                for (document in it) user = document.toObject<User>()
                if(user?.status.equals(INVESTOR_STATUS_ACTIVE)) result.postValue(Pair(true, INVESTOR_CNIC_EXIST))
                else if(user?.status.equals(INVESTOR_STATUS_BLOCKED))result.postValue(Pair(false, INVESTOR_CNIC_BLOCKED))
                else if(it==null)result.postValue(Pair(false, INVESTOR_CNIC_NOT_EXIST))


            }
            .addOnFailureListener{
                result.postValue(Pair(false, INVESTOR_CNIC_NOT_EXIST))
            }
        return result
    }*/





/*

    suspend fun loginUser(CNIC: String, PIN: String): LiveData<Pair<Boolean, String>> {
        val result = MutableLiveData<Pair<Boolean, String>>()
        InvestorsCollection.whereEqualTo(INVESTOR_CNIC, CNIC).whereEqualTo(INVESTOR_PIN,PIN).get()
            .addOnSuccessListener { documents ->

                var user:User?= null
                for (document in documents) user = document.toObject<User>()
                result.value?.first ?: true
                result.value?.second ?: INVESTOR_LOGIN_MESSAGE
                if (user != null) sharedPrefManager.saveUser(user)


            }
            .addOnFailureListener{
                result.value?.first ?: false
                result.value?.second ?: INVESTOR_LOGIN_FAILURE_MESSAGE
            }
        return result
    }
*/










    /*private lateinit var constants: Constants()
    private val sharedPrefManager = SharedPrefManager(context)
    private val token = sharedPrefManager.getToken()



    ///////////////////////////   FIREBASE    //////////////////////////////////
    private val db = Firebase.firestore
    private val firebaseStorage = Firebase.storage
    private val storageRef = firebaseStorage.reference

    private var InvestorsCollection = db.collection(constants.INVESTOR_COLLECTION)
    private val NomineesCollection = db.collection(constants.NOMINEE_COLLECTION)
    private val InvestmentCollection = db.collection(constants.INVESTMENT_COLLECTION)
    private val ProfitCollection = db.collection(constants.PROFIT_COLLECTION)
    private val WithdrawCollection = db.collection(constants.WITHDRAW_COLLECTION)
    private val NotificationCollection = db.collection(constants.NOTIFICATION_COLLECTION)


    ///////////////////////////////////////////////////////////////////////////////

    init {
        constants = Constants()
    }

    fun initializeConstants() {
        constants = Constants() // Initialize the constants property
    }


    suspend fun isInvestorExist(CNIC: String): LiveData<Pair<Boolean, String>> {
        val result = MutableLiveData<Pair<Boolean, String>>()

        InvestorsCollection.whereEqualTo(constants.INVESTOR_CNIC, CNIC).get()
            .addOnSuccessListener {
                val user: User? = it.documents.firstOrNull()?.toObject(User::class.java)
                if(user?.status.equals(constants.INVESTOR_STATUS_ACTIVE)){
                    result.value?.first ?: true
                    result.value?.second ?: constants.INVESTOR_CNIC_EXIST
                }
                else if(user?.status.equals(constants.INVESTOR_STATUS_BLOCKED)){
                    result.value?.first ?: false
                    result.value?.second ?: constants.INVESTOR_CNIC_BLOCKED
                }
                else if(it==null){
                    result.value?.first ?: false
                    result.value?.second ?: constants.INVESTOR_CNIC_NOT_EXIST
                }

            }
            .addOnFailureListener{
                result.value?.first ?: false
                result.value?.second ?: constants.INVESTOR_CNIC_NOT_EXIST
            }

        return result
    }
    suspend fun registerUser(user: User): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        user.status=constants.INVESTOR_STATUS_ACTIVE
        InvestorsCollection.add(user).addOnSuccessListener { documents ->
            result.value =true
        }.addOnFailureListener {
            result.value = false
        }
        return result
    }

    suspend fun loginUser(CNIC: String, PIN: String): LiveData<Pair<Boolean, String>> {
        val result = MutableLiveData<Pair<Boolean, String>>()
        InvestorsCollection.whereEqualTo(constants.INVESTOR_CNIC, CNIC).whereEqualTo(constants.INVESTOR_PIN,PIN).get()
            .addOnSuccessListener {
                val user: User? = it.documents.firstOrNull()?.toObject(User::class.java)
                if (user != null) {
                    result.value?.first ?: true
                    result.value?.second ?: constants.INVESTOR_LOGIN_MESSAGE
                    sharedPrefManager.saveUser(user)
                }
            }
            .addOnFailureListener{
                result.value?.first ?: false
                result.value?.second ?: constants.INVESTOR_LOGIN_FAILURE_MESSAGE
            }
        return result
    }*/





    suspend fun getTransactionReq(status:String, type:String ): Task<QuerySnapshot> {
        return TransactionsReqCollection.whereEqualTo(constants.TRANSACTION_STATUS,status).whereEqualTo(constants.TRANSACTION_TYPE,type).get()
    }
    suspend fun getTransactionAgentReq( ): Task<QuerySnapshot> {
        return WithdrawCollection.get()
    }

    suspend fun getUserInvestment(ID:String): Task<DocumentSnapshot> {
        return TransactionsReqCollection.document(ID).get()
    }   suspend fun getAgentTransaction(): Task<QuerySnapshot> {
        return AgentTransactionCollection.get()
    }

    fun saveNotification(notification: NotificationModel): Task<DocumentReference> {

        return NotificationCollection.add(notification)

    }


}