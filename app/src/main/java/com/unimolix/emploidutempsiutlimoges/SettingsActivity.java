package com.unimolix.emploidutempsiutlimoges;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

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
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        preferences.edit().putInt("year", position).apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}