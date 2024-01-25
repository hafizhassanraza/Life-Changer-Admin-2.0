package com.enfotrix.adminlifechanger.Models

import android.text.format.Time
import com.google.firebase.Timestamp
import com.google.gson.Gson

data class AgentWithdrawModel @JvmOverloads constructor(
    var fa_ID: String = "",
    var withdrawBalance: String = "",
    var totalWithdrawBalance: String = "",
    var withdrawApprovedDate: Timestamp? = null,
    var lastWithdrawReqDate: Timestamp = Timestamp.now(),
    var lastWithdrawBalance: String = "",
    var status: String = "",
    var id:String=""

)
{
    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
    companion object {
        fun fromString(agentWithdrawModel_String: String): AgentWithdrawModel? {
            val gson = Gson()
            return try {
                gson.fromJson(agentWithdrawModel_String, AgentWithdrawModel::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}