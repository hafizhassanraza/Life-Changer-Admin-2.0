package com.enfotrix.lifechanger.Models

import com.google.firebase.Timestamp

data class ModelProfitTax @JvmOverloads constructor(
    var investorID: String = "",
    var type: String = "", //profit, tax
    var amount: String = "",
    var previousBalance: String = "", //
    var newBalance: String = "", //
    var createdAt: Timestamp? = null, // Creation timestamp
    var id: String = ""

)