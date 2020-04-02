package com.example.vipnotifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Button toggleButton;
    private EditText submitArea;
    private TextView allVIPsTextArea;
    private ArrayList<String> allVIPS = new ArrayList<>();
    private boolean toggleState;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private NotificationManager nm;

    private BuzzerService bs = new BuzzerService();
    public int SMS_PERMISSION_CODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toggleButton = findViewById(R.id.toggleButton);
        submitArea = findViewById(R.id.submitField);
        allVIPsTextArea = findViewById(R.id.allvips);
        nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        sharedPreferences = getSharedPreferences("USER_PREFERENCES", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        toggleState = sharedPreferences.getBoolean("ToggleState", false);
        askForPermission();
        unpackageSP();
        updateToggle();
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }
    public void askForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_CODE);
        }
        if (!nm.isNotificationPolicyAccessGranted()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Do Not Disturb Permissions")
                    .setMessage("Please enable Do Not Disturb Permissions for this app within your device's settings. I can redirect you there!")
                    .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent openSettings = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            startActivity(openSettings);
                        }
                    })
                    .setNegativeButton("No Thanks", null)
                    .setIcon(R.drawable.ic_launcher_foreground)
                    .show();
        }

    }
    public void switchToggle(View view) {
        toggleState = !toggleState;
        editor.putBoolean("ToggleState", toggleState);
        editor.commit();
        if(toggleState) {
            toggleButton.setText("DISABLE VIP ACCESS");
            toggleButton.setTextColor(Color.RED);
            startForegroundService(new Intent(this, BuzzerService.class));
        } else {
            toggleButton.setText("ENABLE VIP ACCESS");
            toggleButton.setTextColor(Color.GREEN);
            stopService(new Intent(this, BuzzerService.class));
        }
        BuzzerService.updateAbility(toggleState);
    }
    public void updateToggle() {
        if(toggleState) {
            toggleButton.setText("DISABLE VIP ACCESS");
            toggleButton.setTextColor(Color.RED);
            startForegroundService(new Intent(this, BuzzerService.class));
        } else {
            toggleButton.setText("ENABLE VIP ACCESS");
            toggleButton.setTextColor(Color.GREEN);
            stopService(new Intent(this, BuzzerService.class));
        }
        BuzzerService.updateAbility(toggleState);
    }
    public void submitVIP(View view) {
        if(!submitArea.getText().toString().trim().equals("")) allVIPS.add(submitArea.getText().toString().trim());
        updateVIP();
        submitArea.setText("");
    }
    public void removeVIP(View view) {
        if(!submitArea.getText().toString().trim().equals("")) allVIPS.remove(submitArea.getText().toString().trim());
        updateVIP();
        submitArea.setText("");
    }
    public void updateVIP() {
        allVIPsTextArea.setText("Current VIPs: \n" + allVIPS.toString());
        editor.putString("AllVIP", allVIPS.toString().substring(1, allVIPS.toString().length()-1).trim());
        editor.commit();
        bs.setVipsArr(allVIPS);
        BuzzerService.updateAbility(toggleState);
    }
    public void clearAll(View view) {
        editor.clear();
        editor.commit();
        allVIPS.clear();
        updateVIP();
    }
    public void unpackageSP() {
        String s = sharedPreferences.getString("AllVIP", "");
        String[] vipsarr = s.split(",");
        allVIPS.clear();
        for (String value : vipsarr) {
            allVIPS.add(value.trim());
        }
        updateVIP();
    }
}
