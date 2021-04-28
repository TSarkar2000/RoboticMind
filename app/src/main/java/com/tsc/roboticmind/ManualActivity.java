package com.tsc.roboticmind;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;
import com.tsc.roboticmind.core.MqttRoboClient;
import com.tsc.roboticmind.core.RoboClient;
import com.tsc.roboticmind.core.Task;
import com.tsc.roboticmind.utils.Commands;
import com.tsc.roboticmind.utils.DataHolder;
import com.tsc.roboticmind.utils.Prefs;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ManualActivity extends AppCompatActivity implements View.OnTouchListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private Button upBtn;
    private Button rightBtn;
    private Button downBtn;
    private Button leftBtn;
    private ScrollView scrollView;
    private Slider slider;
    private TextView mainDisplay;

    private Task task;

    int rotAngle = 0;

    private void initWidgets() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Manual Mode");

        upBtn = findViewById(R.id.upBtn);
        rightBtn = findViewById(R.id.rightBtn);
        downBtn = findViewById(R.id.downBtn);
        leftBtn = findViewById(R.id.leftBtn);

        slider = findViewById(R.id.slider);

        mainDisplay = findViewById(R.id.manualDisplay);

        scrollView = findViewById(R.id.scrollView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);

        task = DataHolder.getInstance().getTask();
        task.setMqttEventListener(eventListener);

        initWidgets();

        rotAngle = preferences.getInt(Prefs.KEY_ROTATIONS, 20);

        upBtn.setOnTouchListener(this);
        rightBtn.setOnTouchListener(this);
        downBtn.setOnTouchListener(this);
        leftBtn.setOnTouchListener(this);

        slider.addOnSliderTouchListener(onSliderTouchListener);
    }

    private final Slider.OnSliderTouchListener onSliderTouchListener = new Slider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {

        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            // mcu.sendCommand(Commands.SET_MSPEED, (int) slider.getValue());
        }
    };

    private final StringBuilder sb = new StringBuilder();
    private final MqttRoboClient.EventListener eventListener = new MqttRoboClient.EventListener() {
        @Override
        public void connectionLost(Throwable cause) {
            onMessageEvent("Disconnected.");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            onMessageEvent("Arrived "+(topic)+": "+ message);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            try {
                onMessageEvent("Delivered:"+token.getMessage());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
           onMessageEvent("Connected to "+ serverURI);
           onMessageEvent("Client Id: "+task.reqClientId());
           task.subscribe(Prefs.FROM_ESP8266);
        }

        @Override
        public void onMessageEvent(String message) {
            sb.append(message).append("\n");
            mainDisplay.setText(sb.toString());
            scrollView.fullScroll(View.FOCUS_DOWN);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.autoMode:
                startActivity(new Intent(this, AutoActivity.class));
                finish();
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.clearLogs:
                mainDisplay.setText("");
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

    private Handler handler;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        TouchTask t = getTouchTask(v.getId());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.performClick();
                if (handler != null) return true;
                handler = new Handler();
                handler.postDelayed(t, 300);
                break;
            case MotionEvent.ACTION_UP:
                if (handler == null) return true;
                handler.removeCallbacks(t);
                handler = null;
                task.sendCommand(Commands.HALT, -1);
                break;
        }
        return false;
    }

    @SuppressLint("NonConstantResourceId")
    private TouchTask getTouchTask(int id) {
        TouchTask touchTask;
        switch (id) {
            case R.id.upBtn:
                touchTask = new TouchTask(Commands.MOVE_UP, -1);
                break;
            case R.id.rightBtn:
                touchTask = new TouchTask(Commands.MOVE_RIGHT, rotAngle);
                break;
            case R.id.downBtn:
                touchTask = new TouchTask(Commands.MOVE_DOWN, -1);
                break;
            case R.id.leftBtn:
                touchTask = new TouchTask(Commands.MOVE_LEFT, rotAngle);
                break;
            default:
                touchTask = new TouchTask(Commands.HALT, -1);
        }
        return touchTask;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(Prefs.KEY_ROTATIONS))
            rotAngle = sharedPreferences.getInt(key, 20);
    }

    private class TouchTask implements Runnable {

        private final String taskType;
        public final int value;

        public TouchTask(String taskType, int value) {
            this.taskType = taskType;
            this.value = value;
        }

        @Override
        public void run() {
            task.sendCommand(taskType, value);
            // handler.postDelayed(this, 300); // uncomment for continuous input
        }
    }
}