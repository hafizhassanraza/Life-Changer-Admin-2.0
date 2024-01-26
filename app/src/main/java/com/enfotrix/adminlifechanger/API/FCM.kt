package com.enfotrix.adminlifechanger.API

import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class FCM {

    fun sendFCMNotification(targetDeviceToken: String, title: String, body: String): String {
        val serverKey = "AAAADogOHME:APA91bFkr6fFM-F7zCDWCzhLAwrQh7k2C2wwc17U2ANoTDax-8kO8DtyLWhxxCLNPFiUpKA0LnxSlGWD1wjrQYDxSwcaRtN5-185IvhvCwiDbD-M1Ur9UHiQNdfwVOgUlUFj3KYk0Wvq" // Replace with your FCM server key
        val url = "https://fcm.googleapis.com/fcm/send"

        val client = OkHttpClient()

        var mResponse= "NA"
        val json = JSONObject()
        json.put("to", targetDeviceToken)

        val notification = JSONObject()
        notification.put("title", title)
        notification.put("body", body)
        json.put("notification", notification)

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "key=$serverKey")
            .post(requestBody)
            .build()


        /*client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                mResponse=response.message.toString()
            }
        }*/


        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    mResponse=response.message.toString()
                    // Handle unsuccessful response
                    //println("Failed to send notification to device $deviceToken: ${response.code} ${response.message}")
                } else {
                    // Handle successful response
                    //println("Notification sent successfully to device $deviceToken")
                    mResponse="Notification sent successfully to device"
                }
            } catch (e: IOException) {
                // Handle IO exception
                //println("Error sending notification to device $deviceToken: ${e.message}")
            }
        }

        return mResponse

    }

    fun sendFCMNotificationToDevices(targetDeviceTokens: List<String>, title: String, body: String) {




        /*val targetDeviceTokens = listOf("DEVICE_TOKEN_1", "DEVICE_TOKEN_2", "DEVICE_TOKEN_3")
        val notificationTitle = "Hello"
        val notificationBody = "This is a test notification!"

        sendFCMNotificationToDevices(targetDeviceTokens, notificationTitle, notificationBody)*/


        val serverKey = "AAAADogOHME:APA91bFkr6fFM-F7zCDWCzhLAwrQh7k2C2wwc17U2ANoTDax-8kO8DtyLWhxxCLNPFiUpKA0LnxSlGWD1wjrQYDxSwcaRtN5-185IvhvCwiDbD-M1Ur9UHiQNdfwVOgUlUFj3KYk0Wvq" // Replace with your FCM server key
        val url = "https://fcm.googleapis.com/fcm/send"

        val client = OkHttpClient()

        val json = JSONObject()
        json.put("registration_ids", targetDeviceTokens)

        val notification = JSONObject()
        notification.put("title", title)
        notification.put("body", body)
        json.put("notification", notification)

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "key=$serverKey")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Failed to send notification: ${response.code} ${response.message}")
            } else {
                println("Notification sent successfully")
            }
        }
    }


}