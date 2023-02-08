package com.TimoEnSolo.emploidutempsiutlimoges

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var preferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val yearChoice = findViewById<Spinner>(R.id.yearChoice)
        yearChoice.onItemSelectedListener = this
        preferences = getSharedPreferences("settings", MODE_PRIVATE)
        yearChoice.setSelection(preferences!!.getInt("year", 0))
        val notifNewEdt = findViewById<Switch>(R.id.notifNewEdt)
        val notifEdtChanged = findViewById<Switch>(R.id.notifEdtChanged)
        val keepPositionOfEdt = findViewById<Switch>(R.id.keepPositionOfEdt)
        notifNewEdt.isChecked = preferences!!.getBoolean("notifNewEdt", true)
        notifEdtChanged.isChecked = preferences!!.getBoolean("notifEdtChanged", true)
        keepPositionOfEdt.isChecked = preferences!!.getBoolean("backToLastPosition", true)
        notifEdtChanged.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            preferences!!.edit().putBoolean("notifEdtChanged", notifEdtChanged.isChecked).apply()
        }
        notifNewEdt.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            preferences!!.edit().putBoolean("notifNewEdt", notifNewEdt.isChecked).apply()
        }
        keepPositionOfEdt.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            preferences!!.edit().putBoolean("backToLastPosition", keepPositionOfEdt.isChecked).apply()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        preferences!!.edit().putInt("year", position).apply()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}