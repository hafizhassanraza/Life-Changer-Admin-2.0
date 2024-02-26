package com.enfotrix.adminlifechanger.Models

import com.google.firebase.Timestamp

data class ProfitModel (
   var docID:String="",
    var previousProfit: String = "",
    var newProfit: String = "",
    var remarks: String = "",
    val createdAt: Timestamp = Timestamp.now()

    )

{

}
