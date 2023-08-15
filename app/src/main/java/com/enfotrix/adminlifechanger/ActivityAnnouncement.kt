package com.enfotrix.adminlifechanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.enfotrix.adminlifechanger.Fragments.DashboardFragment
import com.enfotrix.adminlifechanger.Models.ModelAnnouncement
import com.enfotrix.lifechanger.Utils
import com.google.firebase.firestore.FirebaseFirestore

class ActivityAnnouncement : AppCompatActivity() {

    lateinit var submit: Button
    lateinit var news: EditText



    lateinit var utils: Utils

    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement)
        submit = findViewById(R.id.btnsubmit)
        news = findViewById(R.id.etnews)

        firestore = FirebaseFirestore.getInstance()
utils=Utils(this@ActivityAnnouncement)



        submit.setOnClickListener {
            utils.startLoadingAnimation()
            val new: String = news.text.toString()
            val modelAnnouncement = ModelAnnouncement(new + "", "")
            firestore.collection("Admin Announcement").document("Rx3xDtgwOH7hMdWxkf94")
                .set(modelAnnouncement)
                .addOnCompleteListener { task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@ActivityAnnouncement,
                            "Announcement Submitted",
                            Toast.LENGTH_SHORT
                        ).show()

                        }
                }
                .addOnCanceledListener {
                    Toast.makeText(this, "Failed To Submit Announcement", Toast.LENGTH_SHORT).show()
                }

        }
    }
}