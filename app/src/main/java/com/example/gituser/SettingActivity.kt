package com.example.gituser

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class SettingActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var reminderReceiver: ReminderReceiver
    private lateinit var btnSetReminder: Button
    private lateinit var btnCancelReminder: Button
    private lateinit var reminderStatus: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        btnSetReminder = findViewById(R.id.turn_on_reminder_button)
        btnCancelReminder = findViewById(R.id.turn_off_reminder_button)
        reminderStatus = findViewById(R.id.reminder_status)
        btnSetReminder.setOnClickListener(this)
        btnCancelReminder.setOnClickListener(this)
        reminderReceiver = ReminderReceiver()
        reminderStatus.text = if(reminderReceiver.isReminderSet(this)) "ON" else "OFF"
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.turn_on_reminder_button->{
                val repeatTime = "17:40"
                val repeatMessage = "Time to Back to the Applicaton"
                if(!reminderReceiver.isReminderSet(this)){
                    reminderReceiver.setRepeatingReminder(this, ReminderReceiver.TYPE_REPEATING, repeatTime, repeatMessage)
                    reminderStatus.text = "ON"
                }else{
                    Toast.makeText(this, "Daily Reminder is ON", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.turn_off_reminder_button->{
                if(reminderReceiver.isReminderSet(this)){
                    reminderReceiver.cancelAlarm(this, ReminderReceiver.TYPE_REPEATING)
                    reminderStatus.text = "OFF"
                }else{
                    Toast.makeText(this, "Daily Reminder is OFF", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}