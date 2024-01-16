package com.enfotrix.adminlifechanger.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.R

class ActivityAgentNotification : AppCompatActivity() {

    private lateinit var modelFA: ModelFA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agent_notification)

        modelFA = ModelFA.fromString(intent.getStringExtra("Fa").toString())!!


        //var id = modelFA.id

        // hussain
        // get his ( id ) notification (recyclerview)
        // add his ( id ) Notification (floating Button)



    }
}