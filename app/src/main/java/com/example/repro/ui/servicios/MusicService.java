package com.example.repro.ui.servicios;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.example.repro.R;
import java.io.IOException;

public class MusicService extends Service {
    public static final String ACTION_PLAY = "com.example.repro.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.repro.ACTION_PAUSE";
    public static final String ACTION_REWIND = "com.example.repro.ACTION_REWIND";
    public static final String ACTION_FORWARD = "com.example.repro.ACTION_FORWARD";
    public static final String ACTION_STOP = "com.example.repro.ACTION_STOP";
    public static final String ACTION_COMPLETED = "com.example.repro.ACTION_COMPLETED";
    public static final String ACTION_RESUME = "com.example.repro.ACTION_RESUME";

    public static MediaPlayer mediaPlayer;
    private String songUrl;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            stopSelf();
            sendBroadcast(new Intent(ACTION_COMPLETED));
        });
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

            case ACTION_REWIND:
                if (songUrl.equals(songUrl)) {
                    rewindAudio();
                }
                break;

            case ACTION_FORWARD:
                if (this.songUrl.equals(songUrl)) {
                    forwardAudio();
                }
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
        String songUrl = intent.getStringExtra("songUrl");
        this.songUrl = songUrl;
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            showNotification("Playing music", "Your music is playing in the background");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pauseAudio() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            showNotification("Music paused", "Your music is paused");
        }
    }

    private void resumeAudio() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            showNotification("Playing music", "Your music is playing in the background");
        }
    }

    private void stopAudio() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            stopForeground(true);
            stopSelf();
        }
    }

    private void rewindAudio() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int rewindPosition = currentPosition - 30000;
            mediaPlayer.seekTo(Math.max(rewindPosition, 0));
        }
    }

    private void forwardAudio() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int forwardPosition = currentPosition + 30000;
            mediaPlayer.seekTo(Math.min(forwardPosition, mediaPlayer.getDuration()));
        }
    }

    private void showNotification(String title, String content) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "MusicServiceChannel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Music Service Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for Music Service");
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.icons8_nota_musical_50)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        startForeground(1, notification);
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
