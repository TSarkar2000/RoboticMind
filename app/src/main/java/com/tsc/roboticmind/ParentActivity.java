package com.tsc.roboticmind;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.tsc.roboticmind.utils.Prefs;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ParentActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        preferences = getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE);

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_PHONE_STATE
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            StringBuilder sb = new StringBuilder("These permissions were denied:\n");
                            for (PermissionDeniedResponse pr : multiplePermissionsReport.getDeniedPermissionResponses())
                                sb.append(pr.getPermissionName().replace("android.permission.", "")).append("\n");
                            sb.append("And hence, you can not proceed.");

                            AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity.this)
                                    .setIcon(ContextCompat.getDrawable(ParentActivity.this, R.drawable.info))
                                    .setTitle("Permissions denied!")
                                    .setMessage(sb.toString())
                                    .setPositiveButton("Grant Permissions", (dialog, which) -> {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .setNegativeButton("Quit", (dialog, which) -> finish());
                            builder.show();
                        } else new Worker().execute(preferences.getString(Prefs.KEY_HOST, Prefs.DEF_HOST));
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    class Worker extends AsyncTask<String, Void, Void> {

        private ProgressBar progressBar;
        private String str, url;
        private boolean connectionEstablished = false;

        @Override
        protected void onPreExecute() {
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.INVISIBLE);
            if(!connectionEstablished) {
                new AlertDialog.Builder(ParentActivity.this)
                        .setTitle("Error!")
                        .setMessage(str)
                        .setPositiveButton("Retry", (dialog, which) -> {
                            new Worker().execute(url);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            finish();
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else{
                Toast.makeText(ParentActivity.this, str, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ParentActivity.this, MainActivity.class));
                finish();
            }
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                url = strings[0];
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                httpURLConnection.setConnectTimeout(10000);
                httpURLConnection.setRequestMethod("HEAD");
                connectionEstablished = (httpURLConnection.getResponseCode() == 200);
                str = "Connected!";
            } catch (IOException e) {
                str = e.getMessage();
            }
            return null;
        }
    }
}