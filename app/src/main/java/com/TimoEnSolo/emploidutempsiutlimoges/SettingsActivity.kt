package com.TimoEnSolo.emploidutempsiutlimoges

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var preferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val yearChoice = findViewById<Spinner>(R.id.yearChoice)
        yearChoice.onItemSelectedListener = this
        preferences = getSharedPreferences("settings", MODE_PRIVATE)
        yearChoice.setSelection(preferences!!.getInt("year", 0))
        val notifNewEdt = findViewById<SwitchCompat>(R.id.notifNewEdt)
        val notifEdtChanged = findViewById<SwitchCompat>(R.id.notifEdtChanged)
        val keepPositionOfEdt = findViewById<SwitchCompat>(R.id.keepPositionOfEdt)

        if (Notifications.hasNotificationPermission(this)) {
            notifNewEdt.isChecked = preferences!!.getBoolean("notifNewEdt", true)
            notifEdtChanged.isChecked = preferences!!.getBoolean("notifEdtChanged", true)
        } else {
            notifNewEdt.isChecked = false
            notifEdtChanged.isChecked = false
        }

        keepPositionOfEdt.isChecked = preferences!!.getBoolean("backToLastPosition", true)


        notifEdtChanged.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (Notifications.forceNotificationPermission(this))
                notifEdtChanged.isChecked = false
            else
                preferences!!.edit().putBoolean("notifEdtChanged", isChecked).apply()
        }


        notifNewEdt.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (Notifications.forceNotificationPermission(this))
                notifNewEdt.isChecked = false
            else
                preferences!!.edit().putBoolean("notifNewEdt", isChecked).apply()
        }


        keepPositionOfEdt.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            preferences!!.edit().putBoolean("backToLastPosition", isChecked).apply()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        preferences!!.edit().putInt("year", position).apply()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}