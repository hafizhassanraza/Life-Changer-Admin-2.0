package com.enfotrix.adminlifechanger.Models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.lifechanger.Data.Repo
import com.enfotrix.lifechanger.SharedPrefManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference

class NotificationViewModel(context: Application) : AndroidViewModel(context) {
    private val userRepo = Repo(context)
    private val sharedPrefManager = SharedPrefManager(context)

    private var constants= Constants()
    suspend fun setNotification(notification:NotificationModel): Task<DocumentReference> {
        return userRepo.saveNotification(notification)
    }


}
