package com.example.repro.ui.servicios;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.repro.R;
import java.io.IOException;

public class MusicService extends Service {
    public static final String ACTION_PLAY = "com.example.repro.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.repro.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.example.repro.ACTION_STOP";
    public static final String ACTION_RESUME = "com.example.repro.ACTION_RESUME";
    public static final String ACTION_COMPLETED = "com.example.repro.ACTION_COMPLETED";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "MusicServiceChannel";
    public static MediaPlayer mediaPlayer;
    private String songUrl;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeMediaPlayer();
    }

    private void initializeMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(mp -> {
                sendBroadcast(new Intent(ACTION_COMPLETED));
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            handleAction(action, intent);
        }
        return START_STICKY;
    }

    private void handleAction(String action, Intent intent) {
        switch (action) {
            case ACTION_PLAY:
                playAudio(intent);
                break;
            case ACTION_PAUSE:
                pauseAudio();
                break;
            case ACTION_STOP:
                stopAudio();
                break;
            case ACTION_RESUME:
                resumeAudio();
                break;
        }
    }

    private void playAudio(Intent intent) {
        initializeMediaPlayer();
        String songUrl = intent.getStringExtra("songUrl");
        if (songUrl != null) {
            this.songUrl = songUrl;
        }

        if (this.songUrl != null) {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(this.songUrl);
                mediaPlayer.prepare();
                mediaPlayer.start();
                showNotification("Playing music", "Your music is playing in the background");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            stopSelf();
        }
    }

    private void pauseAudio() {
        initializeMediaPlayer();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            showNotification("Music paused", "Your music is paused");
        }
    }

    private void resumeAudio() {
        initializeMediaPlayer();
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            showNotification("Playing music", "Your music is playing in the background");
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            stopForeground(true);
            stopSelf();
        }
    }

    private void showNotification(String title, String content) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Music Service Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for Music Service");
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        // Intents for the actions
        PendingIntent playPendingIntent = createPendingIntent(ACTION_PLAY, this.songUrl);
        PendingIntent pausePendingIntent = createPendingIntent(ACTION_PAUSE, null);
        PendingIntent resumePendingIntent = createPendingIntent(ACTION_RESUME, null);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.icons8_nota_musical_50)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.icons8_play_30, "Play", playPendingIntent)
                .addAction(R.drawable.icons8_pausa_30, "Pause", pausePendingIntent)
                .addAction(R.drawable.icons8_resume_50, "Resume", resumePendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private PendingIntent createPendingIntent(String action, @Nullable String songUrl) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        if (songUrl != null) {
            intent.putExtra("songUrl", songUrl);
        }
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
