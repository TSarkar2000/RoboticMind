package com.tsc.roboticmind;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.tsc.roboticmind.core.MqttRoboClient;
import com.tsc.roboticmind.core.RoboClient;
import com.tsc.roboticmind.core.Task;
import com.tsc.roboticmind.utils.DataHolder;
import com.tsc.roboticmind.utils.Prefs;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Objects;

public class AutoActivity extends AppCompatActivity {

    EditText latText, lonText;
    ScrollView scrollView;
    TextView autoDisplay;

    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Auto Mode");

        latText = findViewById(R.id.latText);
        lonText = findViewById(R.id.lonText);
        scrollView = findViewById(R.id.scrollView2);
        autoDisplay = findViewById(R.id.autoDisplay);

        task = DataHolder.getInstance().getTask();
        task.setMqttEventListener(eventListener);
    }

    private final StringBuilder sb = new StringBuilder();
    private final MqttRoboClient.EventListener eventListener = new MqttRoboClient.EventListener() {

        @Override
        public void connectionLost(Throwable cause) {
            onMessageEvent("Disconnected.");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            onMessageEvent("Arrived " + (topic) + ": " + message);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            try {
                onMessageEvent("Delivered:" + token.getMessage());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            onMessageEvent("Connected to " + serverURI);
            onMessageEvent("Client Id: " + task.reqClientId());
            task.subscribe(Prefs.FROM_ESP8266);
        }

        @Override
        public void onMessageEvent(String message) {
            sb.append(message).append("\n");
            autoDisplay.setText(sb.toString());
            scrollView.fullScroll(View.FOCUS_DOWN);
        }
    };

    public void onClearClick(View v) {
        latText.setText("");
        lonText.setText("");
    }

    public void onGoClick(View v) {
        String lat = latText.getText().toString();
        String lon = lonText.getText().toString();
        if (lat.length() > 0 && lon.length() > 0) {
            task.sendCommand("G" + lat + "," + lon, -1);
        } else {
            Toast.makeText(this, "Invalid coordinates!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.manualMode:
                startActivity(new Intent(this, ManualActivity.class));
                finish();
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.clearLogs:
                autoDisplay.setText("");
                sb.delete(0, sb.length());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Do you really want to exit ?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    task.stop();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}