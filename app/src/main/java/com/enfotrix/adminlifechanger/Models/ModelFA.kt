package com.enfotrix.adminlifechanger.Models

import com.enfotrix.lifechanger.Models.TransactionModel
import com.google.firebase.Timestamp
import com.google.gson.Gson

data class ModelFA @JvmOverloads constructor(
    var cnic: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var address: String = "",
    var phone: String = "",
    var status: String = "",
    var photo: String = "",
    var cnic_front: String = "",
    var cnic_back: String = "",
    var pin: String = "",
    var id: String = "",
    var designantion: String = "",
    val createdAt: Timestamp = Timestamp.now(), // Creation timestamp
    val password: String = ""
) {

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    companion object {
        fun fromString(modelFA: String): ModelFA? {
            val gson = Gson()
            return try {
                gson.fromJson(modelFA, ModelFA::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}