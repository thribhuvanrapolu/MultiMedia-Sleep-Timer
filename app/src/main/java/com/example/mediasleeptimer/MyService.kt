package com.example.mediasleeptimer

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import java.security.Provider
import android.os.IBinder

lateinit var notificationManager: NotificationManager

private val sharedPrefFile = "kotlinsharedpreference"

class MyService: Service() {
    override fun onBind(p0: Intent?): IBinder? {
//        throw UnsupportedOperationException("Not yet implemented")
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        notificationManager.cancel(sharedPreferences.getInt("notification_id",1))
        editor.putBoolean("stop", false)
        editor.putBoolean("pauseplaybutton",true)
        editor.putBoolean("extend", false)
        editor.apply()

        super.onTaskRemoved(rootIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}