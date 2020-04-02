package com.example.vipnotifier;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class BuzzerService extends Service {
    private String CHANNEL_ID = "BuzzerService";
    private static ArrayList<String> allVIPS = new ArrayList<>();
    private static boolean VIPEnabled;
    private Context mContext;
    private AudioManager audioManager;
    Timer timer = new Timer();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        createNotificationChannel();
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        makeForegroundNotification();
        return START_STICKY;
    }
    public void putData(String message, String number, Context c) {
        mContext = c;
        if(allVIPS.contains(number) && VIPEnabled) {
            makeNotification(number, message);
        }
    }
    public void setVipsArr(ArrayList<String> arr) {
        allVIPS = arr;
    }
    public static void updateAbility(boolean ability) {
        VIPEnabled = ability;
    }
    public void makeNotification(String number, String message) {
        createUrgentNotification(0, number, message);
    }
    private void makePhoneBuzz() {
        audioManager = (AudioManager) mContext.getSystemService(AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        timer.schedule(new WaitToSilence(), 1000 * 3);
    }
    class WaitToSilence extends TimerTask {
        @Override
        public void run() {
            silencePhone();
        }
    }
    public void silencePhone() {
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }
    private void makeForegroundNotification() {
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        createNotificationChannel();
        Notification foregroundNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("VIP Access Allowed")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pi)
                .build();
        startForeground(99, foregroundNotification);
    }
    private void createUrgentNotification(int notificationID, String number, String message) {
        makePhoneBuzz();
        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(mContext, 0, openAppIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification urgentNotification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle("URGENT MESSAGE")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("VIP: " + number + "\nMessage: " + message))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(notificationID, urgentNotification);
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
