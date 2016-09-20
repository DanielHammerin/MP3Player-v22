package dv606.mp3player;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.MediaController.MediaPlayerControl;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple MP3 player skeleton for 2DV606 Assignment 2.
 * <p/>
 * Created by Oleksandr Shpak in 2013.
 * Ported to Android Studio by Kostiantyn Kucher in 2015.
 * Last modified by Kostiantyn Kucher on 04/04/2016.
 */
public class MP3Player extends AppCompatActivity {

    // This is an oversimplified approach which you should improve
    // Currently, if you exit/re-enter activity, a new instance of player is created
    // and you can't, e.g., stop the playback for the previous instance,
    // and if you click a song, you will hear another audio stream started
    public final MediaPlayer mediaPlayer = new MediaPlayer();
    boolean playing = false;
    MusicService musicservice;
    FloatingActionButton playPauseButton = null;
    FloatingActionButton prevButton = null;
    FloatingActionButton nextButton = null;
    private int currentSongPos;
    ArrayList<Song> songs = null;
    private static MP3Player instance;

    @Override
    public void onStart() {
        super.onStart();
        instance = this;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_player);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Intent intentt = new Intent(this, MusicService.class);
        this.startService(intentt);

        final ListView listView = (ListView) findViewById(R.id.list_view);
        songs = songList();

        listView.setAdapter(new PlayListAdapter(this, songs));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long arg3) {
                currentSongPos = pos;
                play(songs.get(pos));
            }
        });

        NotificationManager alarmNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MP3Player.class), 0);

        NotificationCompat.Builder alarmNotificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("PLAYER")
                .setContentText("")
                .setAutoCancel(true)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(contentIntent);


        alarmNotificationBuilder.setContentIntent(contentIntent);
        alarmNotificationManager.notify(1, alarmNotificationBuilder.build());

        Intent intent = new Intent(this, MP3Player.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(intent);
        //musicservice.startService();
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        playPauseButton = (FloatingActionButton) findViewById(R.id.play_pause);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    playing = false;
                    playPauseButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),android.R.drawable.ic_media_play, null));
                    pause();
                }
                else {
                    playing = true;
                    playPauseButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),android.R.drawable.ic_media_pause, null));
                    resume();
                }
            }
        });

        nextButton = (FloatingActionButton) findViewById(R.id.next_song);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });

        prevButton = (FloatingActionButton) findViewById(R.id.prev_song);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previous();
            }
        });
    }

    private class PlayListAdapter extends ArrayAdapter<Song> {
        public PlayListAdapter(Context context, ArrayList<Song> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            Song data = getItem(position);

            row = getLayoutInflater().inflate(R.layout.layout_row, parent, false);

            TextView name = (TextView) row.findViewById(R.id.label);
            name.setText(String.valueOf(data));
            row.setTag(data);

            return row;
        }
    }

    /**
     * Checks the state of media storage. True if mounted;
     *
     * @return
     */
    private boolean isStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Reads song list from media storage.
     *
     * @return
     */
    private ArrayList<Song> songList() {
        ArrayList<Song> songs = new ArrayList<Song>();

        if (!isStorageAvailable()) // Check for media storage
        {
            Toast.makeText(this, R.string.nosd, Toast.LENGTH_SHORT).show();
            return songs;
        }

        Cursor music = getContentResolver().query( // using content resolver to read music from media storage
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DATA},
                MediaStore.Audio.Media.IS_MUSIC + " > 0 ",
                null, null
        );

        if (music.getCount() > 0) {
            music.moveToFirst();
            Song prev = null;
            do {
                Song song = new Song(music.getString(0), music.getString(1), music.getString(2), music.getString(3));

                if (prev != null) // play the songs in a playlist, if possible
                    prev.setNext(song);

                prev = song;
                songs.add(song);
            }
            while (music.moveToNext());

            prev.setNext(songs.get(0)); // play in loop
        }
        music.close();

        return songs;
    }

    /**
     * Uses mediaPlayer to play the selected song.
     * The sequence of media player operations is crucial for it to work.
     *
     * @param song
     */
    private void play(final Song song) {
        if (song == null) return;
        playing = true;
        playPauseButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(),android.R.drawable.ic_media_pause, null));
        currentSongPos = songs.indexOf(song);
        song.setPrev(songs.get(currentSongPos - 1));
        song.setNext(songs.get(currentSongPos + 1));

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
    }

    private void resume() {
        try {
            mediaPlayer.start();

        }catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void pause() {
        try {
            if (mediaPlayer.isPlaying()) mediaPlayer.pause(); // pause the current song

        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void next() {
        try {
            if (mediaPlayer.isPlaying()) play(songs.get(currentSongPos).getNext());
        }catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void previous() {
        try {
            if (mediaPlayer.isPlaying()) play(songs.get(currentSongPos).getPrev());
        }catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mp3_player, menu);
        return super.onCreateOptionsMenu(menu);
    }
/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return false;
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static MP3Player getInstance() {
        return instance;
    }
}
