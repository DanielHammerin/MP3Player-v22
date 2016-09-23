package dv606.mp3player;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRouter;
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

import java.sql.Time;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Daniel on 2016-09-20.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {

    Notification notification;
    public static NotificationManager notificationManager;
    private Song currentSongPlaying = null;
    public MediaPlayer mediaPlayer;
    private MusicBinder musicBinder = null;
    int NOTIFICATION_ID = 1337;
    private AudioManager audioManager = null;

    public void onCreate() {
        super.onCreate();
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        musicBinder = new MusicBinder();
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        headphoneBroadcastReceiver receiver = new headphoneBroadcastReceiver();
        registerReceiver( receiver, receiverFilter );

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
    public void tearDownNotification() {
        stopForeground(true);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    /**
     * Uses mediaPlayer to play the selected song.
     * The sequence of media player operations is crucial for it to work.
     *
     * @param song
     */
    public void play(final Song song) {
        long songDuration;
        if (song == null) return;
        currentSongPlaying = song;
        MP3Player.setCurrentSongData(currentSongPlaying.getName(), currentSongPlaying.getArtist());

        songDuration = currentSongPlaying.getDuration();
        MP3Player.currSongLength.setText(
        String.format(Locale.getDefault(), "%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(songDuration),
                TimeUnit.MILLISECONDS.toSeconds(songDuration) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songDuration))
        ));

        try {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop(); // stop the current song

            mediaPlayer.reset(); // reset the resource of player
            mediaPlayer.setDataSource(this, Uri.parse(song.getPath())); // set the song to play
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // select the audio stream
            mediaPlayer.prepare(); // prepare the resource
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() // handle the completion
            {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    play(song.getNext());
                    MP3Player.seekBar.setProgress(0);
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
        stopForeground(true);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onDestroy() {
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

    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                //if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }
    public class headphoneBroadcastReceiver extends BroadcastReceiver {
        private boolean headsetConnected = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")){
                if (headsetConnected && intent.getIntExtra("state", 0) == 0){
                    headsetConnected = false;
                    if (mediaPlayer.isPlaying()){
                        mediaPlayer.pause();

                    }
                } else if (!headsetConnected && intent.getIntExtra("state", 0) == 1){

                }
            }
        }
    }
}
