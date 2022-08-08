package com.example.mediasleeptimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import java.text.DecimalFormat
import java.text.NumberFormat

const val ACTION_STOP_ALARM ="ACTION_STOP_ALARM"
const val ACTION_EXTEND_ALARM ="ACTION_EXTEND_ALARM"

class MainActivity : AppCompatActivity() {


    private val sharedPrefFile = "kotlinsharedpreference"


    //buttons initialize
    lateinit var btnEnable: Button //permission button
    lateinit var pauseplaybtn: ImageButton //pause play button

    //screen lock permission
    lateinit var deviceManger: DevicePolicyManager
    lateinit var compName: ComponentName
    private val enableResult = 1

    lateinit var minutes: NumberPicker
    private var time_extend: Long = 0


    private var pauseplaybool: Boolean = true //makes sure when button is paused and played

    //countdown
    lateinit var countdown: CountDownTimer
    //for stopping video from playing
    lateinit var handler: Handler

    //notification variables
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notification"
    private val description = "Test notification"


    //ads
    lateinit var adView: AdView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adsfuntion()


        startService(Intent(this, MyService::class.java))


        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()


        deviceManger = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, DeviceAdmin::class.java)


        btnEnable = findViewById(R.id.lockscreen_button)//permission button
        pauseplaybtn = findViewById(R.id.pauseplay)//permission button

        //check is permission already enabled
        val active = deviceManger.isAdminActive(compName)
        if (active) {
            btnEnable.visibility = View.GONE
            editor.putBoolean("lock_screen",true)
            editor.apply()

        }
        else {
            btnEnable.visibility = View.VISIBLE
        }






        minutes_numpicker() //create minutes number picker

        extendTime() //extendTime Drop-Down menu





    }
    fun adsfuntion(){
        MobileAds.initialize(this){}
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adView.adListener = object: AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                super.onAdClicked()
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                super.onAdFailedToLoad(adError)
                adView.loadAd(adRequest)
                // Code to be executed when an ad request fails.
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                super.onAdLoaded()
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                super.onAdOpened()
            }
        }
    }


    //when clicked on permission button
    fun enablepermission(view: View) {
        val active = deviceManger.isAdminActive(compName)
        if (!active) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Please enable this permission so that we can turn off the screen when you are asleep")
            startActivityForResult(intent, enableResult)
        }
    }

    //permission code
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            enableResult -> {
                if (resultCode == RESULT_OK) {

                    btnEnable.visibility = View.GONE

                    Toast.makeText(applicationContext, "Lock Screen Permission Enabled ", Toast.LENGTH_SHORT).show()

                    val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor =  sharedPreferences.edit()
                    editor.putBoolean("lock_screen",true)
                    editor.apply()

                } else {

                    Toast.makeText(applicationContext, "Failed!", Toast.LENGTH_SHORT).show()

                    val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor =  sharedPreferences.edit()
                    editor.putBoolean("lock_screen",false)
                    editor.apply()
                }
                return
            }
        }
    }



    fun minutes_numpicker(){

        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        minutes = findViewById(R.id.minutes)

        minutes.minValue = 2
        minutes.maxValue = 180

        minutes.value=sharedPreferences.getInt("minutes",5)

        minutes.setOnValueChangedListener { picker, oldVal, newVal ->

            editor.putInt("minutes",newVal)
            editor.apply()


        }


    }






     fun extendTime() {
        val extend_time_values = resources.getStringArray(R.array.extend_time)

        // access the spinner
        val spinner = findViewById<Spinner>(R.id.spinner)
        if (spinner != null) {

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, extend_time_values)
            spinner.adapter = adapter

            //set previously selected option
            val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
            spinner.setSelection(sharedPreferences.getInt("extend_time",0))

            //when option is selected
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long)
                {
                    save_option(position)
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                    println("here")
                }
            }
        }
    }

    //store extend time option
    private fun save_option(position: Int){
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putInt("extend_time",position)
        editor.apply()
    }


    //when clicked on pause play button
    fun pauseplaybutton(view: View){
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()



        if (sharedPreferences.getBoolean("pauseplaybutton",true)) {

            val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

            if(sharedPreferences.getBoolean("lock_screen", false)){
                pauseplaybtn.setImageResource(R.drawable.pause)
                pauseplaybool = false

                minutes.isEnabled=false


                println("here")

                editor.putBoolean("pauseplaybutton",false)
                editor.apply()

                var min=sharedPreferences.getInt("minutes",5)
                min *= 60000

                editor.putInt("notification_id",(1000..9999).random())
                editor.apply()

                countdown_timer(min.toLong())

            }

            else{
                Toast.makeText(applicationContext, "Please enable lock screen permission ", Toast.LENGTH_SHORT).show()
            }

        }
        else {
            alarmStop()
        }
    }


    //countdown timer function
    private fun countdown_timer(timer: Long) {

        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()


        editor.putBoolean("stop", false)
        editor.putBoolean("extend", false)
        editor.apply()



        timernotification()

        println("countdown_here")

        countdown = object : CountDownTimer(timer, 1000) {


            override fun onTick(millisUntilFinished: Long) {
                val f: NumberFormat = DecimalFormat("00")
                val hour = millisUntilFinished / 3600000 % 24
                val min = millisUntilFinished / 60000 % 60
                val sec = millisUntilFinished / 1000 % 60
                builder.setContentText(
                    "Ends in " + f.format(hour) + ":" + f.format(min) + ":" + f.format(sec)
                )
                notificationManager.notify(sharedPreferences.getInt("notification_id",1), builder.build())


                if (sharedPreferences.getBoolean("stop", false)) {
                    countdown.cancel()
                    alarmStop()
                }


                if (sharedPreferences.getBoolean("extend",false)) {
                    countdown.cancel()

                    when(sharedPreferences.getInt("extend_time",-1)){
                        0->time_extend=5*60000
                        1->time_extend=10*60000
                        2->time_extend=15*60000
                        3->time_extend=20*60000
                        4->time_extend=30*60000
                    }
                    println(time_extend)
                    editor.putBoolean("extend",false)
                    editor.apply()
                    countdown_timer(millisUntilFinished + time_extend)
                }

                if (sec.toInt() == 1 && min.toInt() == 1 && hour.toInt() == 0) {
                    notificationManager.cancel(sharedPreferences.getInt("notification_id",1))
                }
            }

            //when timer finishes
            override fun onFinish() {

                alarmStop()

                //calls pause audio
                var audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

                val result: Int = audioManager.requestAudioFocus(
                    afChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN
                )

                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    println("Success")
                }

                deviceManger.lockNow() //locks screen


            }

        }
        countdown.start()
    }

    //pauses audio
    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss of audio focus
                // Pause playback immediately
                mediaController.transportControls.pause()
                // Wait 30 seconds before stopping playback
                handler.postDelayed(delayedStopRunnable, 5000)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Pause playback
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower the volume, keep playing
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Your app has been granted audio focus again
                // Raise volume to normal, restart playback if necessary
            }
        }
    }

    //stops audio
    private var delayedStopRunnable = Runnable {
        println("handler here")
        mediaController.transportControls.stop()
    }






    //notification

    private fun timernotification() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN

            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)


            builder = Notification.Builder(this, channelId)
                .setSmallIcon(R.drawable.pause)
                .setContentTitle("Timer Set")
                .setContentText("Timer Set ")
                .addAction(
                    R.drawable.pause, "Stop",
                    notification_stopAlarm(this)
                )
                .addAction(
                    R.drawable.play, "Extend",
                    notification_extendAlarm(this)
                )
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)


        } else {

            builder = Notification.Builder(this)
                .setSmallIcon(R.drawable.play)
                .setContentTitle("Timer Set")
                .setContentText("Timer Set")
                .addAction(
                    R.drawable.pause, "Stop",
                    notification_stopAlarm(this)
                )
                .addAction(
                    R.drawable.play, "Extend",
                    notification_extendAlarm(this)
                )
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)

        }

        notificationManager.notify(sharedPreferences.getInt("notification_id",1), builder.build())


    }

    //stop alarm
    private fun notification_stopAlarm(context: Context): PendingIntent {
        val stopAlarmIntent = Intent(context, Alarmservice::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        return PendingIntent.getService(
            this, 1, stopAlarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    }


    //extend alarm
    private fun notification_extendAlarm(context: Context): PendingIntent {
        val extendIntent = Intent(
            context,
            Alarmservice::class.java
        ).apply {
            action = ACTION_EXTEND_ALARM
        }
        return PendingIntent.getService(
            this, 1, extendIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }


    //when countdown timer is stopped
    private fun alarmStop() {

        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("stop", false)
        editor.putBoolean("pauseplaybutton",true)
        editor.apply()

        countdown.cancel()


        notificationManager.cancel(sharedPreferences.getInt("notification_id",1))
        pauseplaybtn.setImageResource(R.drawable.play)
//        pauseplaybool = true

        minutes.isEnabled=true

    }


    fun info(view: View){
        println("here")
        val url = "https://thribhuvanrapolu.github.io/project_blogs/sleeptimer-app"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }


}