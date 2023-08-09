package com.enfotrix.adminlifechanger.Models

import com.google.firebase.Timestamp

class ModelAnnouncement @JvmOverloads constructor(
    var announcement: String = "",
    var status: String = "",
    val createdAt: Timestamp = Timestamp.now() // Creation timestamp
)