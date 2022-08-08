package com.example.mediasleeptimer



import android.app.IntentService
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import java.util.*


class Alarmservice : IntentService(Alarmservice::class.java.simpleName) {
    private val notificationId = System.currentTimeMillis().toInt()

    private val sharedPrefFile = "kotlinsharedpreference"

    override fun onHandleIntent(intent: Intent?) {
        val action=intent!!.action


        //stop alarm sound
        if(action == ACTION_STOP_ALARM){
            stopAlarm()
        }
        //snooze
        else if(action== ACTION_EXTEND_ALARM){
            extendAlarm()
        }
    }

    private fun stopAlarm(){

        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor =  sharedPreferences.edit()
        editor.putBoolean("stop",true)
        editor.apply()

    }


    private fun extendAlarm() {

        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor =  sharedPreferences.edit()
        editor.putBoolean("extend",true)
        editor.apply()

    }


}