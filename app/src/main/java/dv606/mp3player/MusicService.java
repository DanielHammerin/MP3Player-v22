package dv606.mp3player;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Daniel on 2016-09-20.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    Notification notification;
    public static NotificationManager notificationManager;
    private Song currentSongPlaying = null;
    public MediaPlayer mediaPlayer;
    private MusicBinder musicBinder = null;
    int NOTIFICATION_ID = 1337;
    public void onCreate() {
        super.onCreate();
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        musicBinder = new MusicBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void setupNotification() {
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0,
                new Intent(getApplicationContext(), MP3Player.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        notificationManager = ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE));
        notification = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setContentTitle(currentSongPlaying.getName())
                .setContentText(currentSongPlaying.getArtist())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * Uses mediaPlayer to play the selected song.
     * The sequence of media player operations is crucial for it to work.
     *
     * @param song
     */
    public void play(final Song song) {
        if (song == null) return;
        currentSongPlaying = song;
        MP3Player.setCurrentSongData(currentSongPlaying.getName(), currentSongPlaying.getArtist());

        try {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop(); // stop the current song

            mediaPlayer.reset(); // reset the resource of player
            mediaPlayer.setDataSource(this, Uri.parse(song.getPath())); // set the song to play
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION); // select the audio stream
            mediaPlayer.prepare(); // prepare the resource
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() // handle the completion
            {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    play(song.getNext());
                }
            });
            mediaPlayer.start(); // play!
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        setupNotification();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onDestroy() {
        notificationManager.cancelAll();
        notificationManager.cancel(NOTIFICATION_ID);
        stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public static NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public Song getCurrentSongPlaying() {
        return currentSongPlaying;
    }
}
