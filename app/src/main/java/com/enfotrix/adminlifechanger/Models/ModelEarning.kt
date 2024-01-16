package com.enfotrix.adminlifechanger.Models

import com.google.firebase.Timestamp
import com.google.gson.Gson

data class ModelEarning @JvmOverloads constructor(


    var docID: String = "",
    var amount: String = "",
    var balance: String = "",// old Balance
    var disc: String = "",
    var agentID: String="",
    var status: String = "", // Pending / Withdraw
    var withdrawAt: Timestamp? = null, // Creation timestamp
    val createdAt: Timestamp = Timestamp.now(), // Creation timestamp
//    val password: String = ""
) {

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    companion object {
        fun fromString(modelEarning: String): ModelEarning? {
            val gson = Gson()
            return try {
                gson.fromJson(modelEarning, ModelEarning::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

}