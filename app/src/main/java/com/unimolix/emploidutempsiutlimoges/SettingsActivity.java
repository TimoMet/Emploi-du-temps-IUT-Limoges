package com.unimolix.emploidutempsiutlimoges;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Spinner yearChoice = findViewById(R.id.yearChoice);
        yearChoice.setOnItemSelectedListener(this);
        preferences = getSharedPreferences("settings", MODE_PRIVATE);
        yearChoice.setSelection(preferences.getInt("year", 0));

        Switch notifNewEdt = findViewById(R.id.notifNewEdt);
        Switch notifEdtChanged = findViewById(R.id.notifEdtChanged);

        notifNewEdt.setChecked(preferences.getBoolean("notifNewEdt", true));
        notifEdtChanged.setChecked(preferences.getBoolean("notifEdtChanged", true));

        notifEdtChanged.setOnCheckedChangeListener( (buttonView, isChecked) ->
                preferences.edit().putBoolean("notifEdtChanged", notifEdtChanged.isChecked()).apply());

        notifNewEdt.setOnCheckedChangeListener( (buttonView, isChecked) ->
                preferences.edit().putBoolean("notifNewEdt", notifNewEdt.isChecked()).apply());

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        preferences.edit().putInt("year", position).apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}