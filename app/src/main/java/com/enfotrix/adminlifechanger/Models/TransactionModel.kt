package com.enfotrix.lifechanger.Models

import com.google.firebase.Timestamp
import com.google.gson.Gson


data class TransactionModel @JvmOverloads constructor(
    var investorID: String = "",
    var type: String = "", //invest, withdraw
    var status: String = "", // pending ,approved , reject
    var amount: String = "",
    var receiverAccountID: String = "", // account id
    var previousBalance: String = "", //
    var senderAccountID: String = "",// account id
    var id: String = "",
    var newBalance: String = "", //
    var transactionAt: Timestamp? = null, // Creation timestamp
    val createdAt: Timestamp = Timestamp.now() // Creation timestamp
) {


    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    companion object {
        fun fromString(transactionModelString: String): TransactionModel? {
            val gson = Gson()
            return try {
                gson.fromJson(transactionModelString, TransactionModel::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }


}