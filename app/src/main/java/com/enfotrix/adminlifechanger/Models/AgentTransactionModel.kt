package com.enfotrix.adminlifechanger.Models

import com.enfotrix.lifechanger.Models.TransactionModel
import com.google.firebase.Timestamp
import com.google.gson.Gson


data class AgentTransactionModel @JvmOverloads constructor(
    var fa_id: String = "",
    var type: String = "", //invest, withdraw
    var salary: String = "", //salary
    var status: String = "", // pending ,approved , reject
    var amount: String = "0",
    var receiverAccountID: String = "", // account id
    var previousBalance: String = "0", //
    var senderAccountID: String = "",// account id
    var id: String = "",
    var newBalance: String = "0", //
    var remarks: String = "", //
    var transactionAt: Timestamp? = null, // Creation timestamp
    val createdAt: Timestamp ?=Timestamp.now() // Creation timestamp
)
{


    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    companion object {
        fun fromString(transactionModelString: String): AgentTransactionModel? {
            val gson = Gson()
            return try {
                gson.fromJson(transactionModelString, AgentTransactionModel::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }


}