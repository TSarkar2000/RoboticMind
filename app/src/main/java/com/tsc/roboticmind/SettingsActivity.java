package com.tsc.roboticmind;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tsc.roboticmind.utils.Prefs;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private EditText ipText, rotationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");

        ipText = findViewById(R.id.ipText);
        rotationText = findViewById(R.id.rotationText);

        preferences = getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE);

        ipText.setText(preferences.getString(Prefs.KEY_BROKER, Prefs.DEF_BROKER));
        rotationText.setText("" + preferences.getInt(Prefs.KEY_ROTATIONS, 20));

        // this would work given host key is not deleted while keeping others intact
        if(!preferences.contains(Prefs.KEY_HOST)) {
            editor = preferences.edit();
            editor.putString(Prefs.KEY_HOST, Prefs.DEF_HOST);
            editor.putInt(Prefs.KEY_ROTATIONS, Prefs.DEF_ROTATIONS);
            editor.apply();
        }

    }

    public void save(View v) {
        editor = preferences.edit();
        editor.putString(Prefs.KEY_BROKER, ipText.getText().toString());
        editor.putInt(Prefs.KEY_ROTATIONS, Integer.parseInt(rotationText.getText().toString()));
        editor.apply();
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}