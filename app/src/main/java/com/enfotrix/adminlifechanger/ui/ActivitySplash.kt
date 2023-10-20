package com.enfotrix.adminlifechanger.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.enfotrix.adminlifechanger.R
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.installStatus
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.math.log
import kotlin.time.Duration.Companion.seconds

class ActivitySplash : AppCompatActivity() {

    private lateinit var mContext : Context

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mContext = this@ActivitySplash


        Timer().schedule(1500) {
            appUpdateManager = AppUpdateManagerFactory.create(mContext)
            if (updateType == AppUpdateType.FLEXIBLE){
                appUpdateManager.registerListener(installStateUpdateListener)
            }
            checkForAppUpdates()
        }
    }

    private val installStateUpdateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {

            Log.d("installStatus",state.installStatus.toString())

            Toast.makeText(
                mContext,
                "Download successful. Restarting App in 5 seconds",
                Toast.LENGTH_LONG
            ).show()
            GlobalScope.launch {
                delay(5.seconds)

                Log.d("CompleteUpdate",appUpdateManager.completeUpdate().toString())

                appUpdateManager.completeUpdate()

            }
        }
    }

    private fun checkForAppUpdates(){

        Log.d("checkForUpdate","checkForUpdate Function Working")

        appUpdateManager.appUpdateInfo.addOnSuccessListener {info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

            Log.d("isUpdateAvailable",isUpdateAvailable.toString())

            val isUpdateAllowed = when(updateType){
                AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }
            if (isUpdateAvailable && isUpdateAllowed ){

                Log.d("UpdateAllowed",isUpdateAllowed.toString())
                Log.d("UpdateAvailable",isUpdateAvailable.toString())

                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateType,
                    this,
                    123
                )
            }
        }.addOnFailureListener{
            Log.d("Failure",it.message.toString())
            Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onResume() {
        super.onResume()
        appUpdateManager = AppUpdateManagerFactory.create(mContext)
        if (updateType == AppUpdateType.IMMEDIATE){
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if(info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){

                    Log.d("updateAvailability",info.updateAvailability().toString())

                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateType,
                        this,
                        123
                    )
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (updateType == AppUpdateType.FLEXIBLE){
            appUpdateManager.unregisterListener(installStateUpdateListener)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 ){

            Log.d("requestCode",resultCode.toString())

            if(resultCode != RESULT_OK){
                println("Something went wrong updating....")
            }
        }
    }

}


