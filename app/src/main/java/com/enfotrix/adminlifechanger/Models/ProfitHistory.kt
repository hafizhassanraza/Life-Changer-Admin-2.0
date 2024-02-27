package com.enfotrix.adminlifechanger.Models

import com.google.firebase.Timestamp

data class ProfitHistory(
    var docID: String = "",
    var activeInvestment: String = "",
    var availableBalance: String = "",
    var inActiveInvestments: String = "",
    var investors: List<String> = emptyList(), // Add this line for the "investors" array
    var newProfitAmount: String = "",
    var newProfitPercen: String = "",
    var remarks: String = "",
    val createdAt: Timestamp = Timestamp.now()

    )
