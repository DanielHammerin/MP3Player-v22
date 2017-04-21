package dv606.mp3player;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.MenuView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static dv606.mp3player.MusicService.notificationManager;


/**
 * <p/>
 * Created by Daniel Hammerin 2016-10-10.
 */
public class MP3Player extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    boolean playing = false;
    public static boolean isBound;
    private static MusicService musicservice;
    public SharedPreferences prefs;

    FloatingActionButton playPauseButton = null;
    FloatingActionButton prevButton = null;
    FloatingActionButton nextButton = null;

    public static SeekBar seekBar;
    public boolean updateBoolean = false;
    public Song loopingRealNext;
    private boolean bound = false;

    public static TextView currSongName = null;
    public static TextView currSongArtist = null;
    public static TextView currSongLength = null;
    public static TextView currSongTime = null;

    public static int currentSongPos;
    public static ArrayList<Song> songs = null;
    public static ArrayList<Song> currentPlaylist = null;
    public Menu navigationMenu;
    public NavigationView navigationView;

    private static View currentSongView;
    public static ListView listView;

    public static PlayListAdapter adapter;
    public static Equalizer equalizer;


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_player);
        playPauseButton = (FloatingActionButton) findViewById(R.id.play_pause);
        nextButton = (FloatingActionButton) findViewById(R.id.next_song);
        prevButton = (FloatingActionButton) findViewById(R.id.prev_song);

        currSongName = (TextView) findViewById(R.id.currentSongName);
        currSongArtist = (TextView) findViewById(R.id.currentSongArtist);

        currSongLength = (TextView) findViewById(R.id.songLength);
        currSongTime = (TextView) findViewById(R.id.currentSongTime);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        seekBar = (SeekBar) findViewById(R.id.seekbar);

        Intent intentt = new Intent(this, MusicService.class);
        startService(intentt);
        isBound = bindService(intentt, connection, Context.BIND_AUTO_CREATE);

        listView = (ListView) findViewById(R.id.list_view);
        listView.setTextFilterEnabled(true);
        songs = songList();

        adapter = new PlayListAdapter(this, this, songs);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationMenu = navigationView.getMenu();
        navigationView.setLongClickable(true);
/*
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("PLAYLISTS", null);
        prefsEditor.apply();
*/
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        updatePreferences();

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long arg3) {
                currentSongPos = pos;
                //if(currentSongView != null) {
                //    currentSongView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                //}
                adapter.setCurrentSongPos(pos);
                currentSongView = view;
                musicservice.play(songs.get(pos));
                view.setSelected(true);
                playPauseButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_pause, null));
                //currentSongView.setBackgroundColor(Color.parseColor("#ff9966"));
                adapter.setCurrentSongPos(pos);
                adapter.notifyDataSetChanged();
                updateSeekBar();
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSongList(navigationView.indexOfChild(v));
            }
        });

        /*
        for (int i = 2; i < navigationMenu.size(); i++) {
            MenuItem navMenuItem = navigationMenu.getItem(i);
            navMenuItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MP3Player.this);
                    //builder.setTitle("Pick a color");
                    final CharSequence options[] = new CharSequence[] {"Add Songs", "Delete Playlist"};
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {

                            }
                        }
                    });
                    AlertDialog alertBox = builder.create();
                    alertBox.show();
                    return true;
                }
            });
        }
        */

    }

    public void allSongs() {
        adapter = new PlayListAdapter(this, this, songs);
        listView.setAdapter(adapter);
    }

    /**
     * This method updates the songlist array for the playlist selected in the
     * navigation view bar.
     * It sets the playlist and adapter to the songs in the playlist.
     * @param pos
     */
    public void updateSongList(int pos) {
        String playListsInMenu = prefs.getString("PLAYLISTS", null);
        Type type = new TypeToken<ArrayList<PlayList>>(){}.getType();
        ArrayList<PlayList> allPlayLists;
        Gson gson = new Gson();
        allPlayLists = gson.fromJson(playListsInMenu,type);
        for (PlayList p : allPlayLists) {
            System.out.println(p.getPlayListName());
        }

        PlayList playListTobeOpened = allPlayLists.get(pos + 1);
        System.out.println(playListTobeOpened.getPlayListName());

        ArrayList<Song> tempList = playListTobeOpened.getSongsInPlayList();
        for (Song s : tempList) {
            if (tempList.indexOf(s) == 0) {
                s.setNext(tempList.get(1));
                s.setPrev(tempList.get(tempList.size()-1));
            }
            else if (tempList.indexOf(s) == tempList.size() - 1) {
                s.setNext(tempList.get(0));
                s.setPrev(tempList.get(tempList.indexOf(s) - 1));
            }
            else {
                s.setPrev(tempList.get(tempList.indexOf(s) - 1));
                s.setNext(tempList.get(tempList.indexOf(s) + 1));
            }
        }
        playListTobeOpened.setSongsInPlayList(tempList);
        System.out.println(tempList.size());
/*
        for (Song s : playListTobeOpened.getSongsInPlayList()) {
            System.out.println(s.getName());
            if (playListTobeOpened.getSongsInPlayList().indexOf(s) == 0) {
                s.setPrev(playListTobeOpened.getSongsInPlayList().get(playListTobeOpened.getSongsInPlayList().size()-1));
            }
            else {
                s.setPrev(playListTobeOpened.getSongsInPlayList().get(playListTobeOpened.getSongsInPlayList().indexOf(s) - 1));
            }
            if (playListTobeOpened.getSongsInPlayList().indexOf(s) == playListTobeOpened.getSongsInPlayList().size() - 1) {
                s.setNext(playListTobeOpened.getSongsInPlayList().get(0));
            }
            else {
                s.setNext(playListTobeOpened.getSongsInPlayList().get(playListTobeOpened.getSongsInPlayList().indexOf(s) + 1));
            }
        }
*/
        /*
        for (Song s : playListTobeOpened.getSongsInPlayList()) {
            System.out.println(s.getName());
        }
        System.out.println(playListTobeOpened.getSongsInPlayList().size());
        */
        currentPlaylist = playListTobeOpened.getSongsInPlayList();
        adapter = new PlayListAdapter(this, this, currentPlaylist);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }

    public void updatePreferences() {
        String playListsInMenu = prefs.getString("PLAYLISTS", null);
        System.out.println(playListsInMenu);
        if (playListsInMenu != null) {
            Type type = new TypeToken<ArrayList<PlayList>>(){}.getType();
            ArrayList<PlayList> allplaylists = new ArrayList<>();
            Gson gson = new Gson();
            allplaylists = gson.fromJson(playListsInMenu,type);

            for (PlayList playList: allplaylists) {
                ArrayList<Song> tempList = playList.getSongsInPlayList();
                for (Song s : tempList) {
                    if (tempList.indexOf(s) == 0) {
                        s.setNext(tempList.get(1));
                        s.setPrev(tempList.get(tempList.size()-1));
                    }
                    else if (tempList.indexOf(s) == tempList.size() - 1) {
                        s.setNext(tempList.get(0));
                        s.setPrev(tempList.get(tempList.indexOf(s) - 1));
                    }
                    else {
                        s.setPrev(tempList.get(tempList.indexOf(s) - 1));
                        s.setNext(tempList.get(tempList.indexOf(s) + 1));
                    }
                }
                playList.setSongsInPlayList(tempList);
                MenuItem newPlaylistItem = navigationMenu.add(playList.getPlayListName());
                newPlaylistItem.setIcon((ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_menu_save, null)));
                navigationView.setNavigationItemSelectedListener(this);
            }
        }
    }

    public void updateSeekBar() {
        seekBar.setProgress(0);
        final Handler mHandler = new Handler();
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(musicservice.getMediaPlayer() != null){
                    int mCurrentPosition = musicservice.getMediaPlayer().getCurrentPosition();
                    seekBar.setMax((int) musicservice.getCurrentSongPlaying().getDuration() / 1000);
                    seekBar.setProgress(mCurrentPosition / 1000);

                    currSongTime.setText(String.format(Locale.getDefault(), "%d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition),
                            TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition))
                    ));
                    currSongLength.setText(
                            String.format(Locale.getDefault(), "%d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(musicservice.getCurrentSongPlaying().getDuration()),
                                    TimeUnit.MILLISECONDS.toSeconds(musicservice.getCurrentSongPlaying().getDuration()) -
                                            TimeUnit.MINUTES.toSeconds(
                                                    TimeUnit.MILLISECONDS.toMinutes(
                                                            musicservice.getCurrentSongPlaying().getDuration()))
                            ));

                    setCurrentSongData(musicservice.getCurrentSongPlaying().getName(), musicservice.getCurrentSongPlaying().getArtist());

                }
                mHandler.postDelayed(this, 1000);
            }
        });
    }

    public static ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicservice = ((MusicService.MusicBinder) service).getService();
            equalizer = new Equalizer(1, musicservice.getMpSessionID());
            equalizer.getNumberOfBands();
            short[] bandRange = equalizer.getBandLevelRange();

            equalizer.setBandLevel((short) 0, bandRange[0]);
            equalizer.setBandLevel((short) 1, bandRange[0]);
            equalizer.setBandLevel((short) 2, bandRange[0]);
            equalizer.setBandLevel((short) 3, bandRange[0]);
            equalizer.setBandLevel((short) 4, bandRange[0]);

            equalizer.setEnabled(true);
            equalizer.getNumberOfPresets();
            equalizer.setParameterListener(EQlistener);
            //equalizer.usePreset((short) 11);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicservice = null;
            equalizer.release();
        }
    };

    public static Equalizer.OnParameterChangeListener EQlistener = new Equalizer.OnParameterChangeListener() {
        @Override
        public void onParameterChange(Equalizer effect, int status, int param1, int param2, int value) {
            System.out.println("EQ param changed.");
            if (equalizer != null) {
                equalizer.release();
                equalizer = new Equalizer(1, musicservice.getMpSessionID());
                equalizer.setEnabled(true);
                equalizer.getNumberOfBands();
                equalizer.getBandLevelRange();
                equalizer.getNumberOfPresets();
                equalizer.setParameterListener(EQlistener);
                //effect.setBandLevel((short) param1, (short) value);
            }
        }
    };

    public static void setCurrentSongData(String name, String artist) {
        currSongName.setText(name);
        currSongArtist.setText(artist);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appTerminated(isBound, connection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        playPauseButton = (FloatingActionButton) findViewById(R.id.play_pause);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicservice.getMediaPlayer().isPlaying()) {
                    playing = false;
                    playPauseButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_play, null));
                    pause();
                } else {
                    playing = true;
                    playPauseButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_pause, null));
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

        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicservice.mediaPlayer != null && fromUser) {
                    musicservice.mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                resume();
            }
        });
    }

    private void resume() {
        try {
            musicservice.getMediaPlayer().start();

        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void pause() {
        try {
            if (musicservice.getMediaPlayer().isPlaying())
                musicservice.getMediaPlayer().pause(); // pause the current song

        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void next() {
        try {
            if (musicservice.getMediaPlayer().isPlaying())
                musicservice.play(musicservice.getCurrentSongPlaying().getNext());
            if(currentSongView != null) {
                if (currentSongPos == songs.size()-1) {
                    currentSongPos = 0;
                    listView.setSelection(currentSongPos + 10);
                    listView.smoothScrollToPositionFromTop(currentSongPos, 0, 10);
                }
                else {
                    currentSongPos++;
                }
                adapter.setCurrentSongPos(currentSongPos);
                adapter.notifyDataSetChanged();
                /*
                currentSongView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                currentSongView = listView.getChildAt(currentSongPos);
                currentSongView.setBackgroundColor(Color.parseColor("#ff9966"));
                */
            }
            seekBar.setProgress(0);
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void previous() {
        try {
            if (musicservice.getMediaPlayer().isPlaying())
                musicservice.play(musicservice.getCurrentSongPlaying().getPrev());
            if(currentSongView != null) {
                if (currentSongPos == 0) {
                    currentSongPos = songs.size()-1;
                    listView.setSelection(currentSongPos - 10);
                    listView.smoothScrollToPositionFromTop(currentSongPos, 0, 10);
                }
                else {
                    currentSongPos--;
                }
                adapter.setCurrentSongPos(currentSongPos);
                adapter.notifyDataSetChanged();
                /*
                currentSongView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                currentSongView = listView.getChildAt(currentSongPos);
                currentSongView.setBackgroundColor(Color.parseColor("#ff9966"));
                */
            }
            seekBar.setProgress(0);
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            System.out.println(e);
        }
    }

    public static void updateSongColor() {
        if (currentSongPos == songs.size()-1) {
            currentSongPos = 0;
        }
        else {
            currentSongPos++;
        }
        adapter.setCurrentSongPos(currentSongPos);
        adapter.notifyDataSetChanged();
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
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.DURATION},
                MediaStore.Audio.Media.IS_MUSIC + " > 0 ",
                null, null
        );

        if (music.getCount() > 0) {
            music.moveToFirst();
            Song prev = null;
            do {
                Song song = new Song(music.getString(0), music.getString(1), music.getString(2), music.getString(3), music.getLong(4));

                if (prev != null) { // play the songs in a playlist, if possible
                    prev.setNext(song);
                    song.setPrev(prev);
                }
                prev = song;
                songs.add(song);
            }
            while (music.moveToNext());

            prev.setNext(songs.get(0)); // play in loop
        } else {
            Toast.makeText(MP3Player.this, "No Music on this device.", Toast.LENGTH_SHORT).show();
        }
        music.close();
        songs.get(0).setPrev(songs.get(songs.size() - 1));
        songs.get(songs.size() - 1).setNext(songs.get(0));
        return songs;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String newPlaylistName = data.getStringExtra("playListName");
                Bundle b = data.getExtras();
                final PlayList newPlaylist = new PlayList();
                newPlaylist.setSongsInPlayList((ArrayList<Song>) b.getSerializable("array"));
                newPlaylist.setPlayListName(newPlaylistName);

                /*
                When storing a new playlist, the next and previous song references
                needs to be nulled in order for them to be able to be saved in the
                preferences.
                These next and prev references are restored when a playlist is loaded
                again from the preferences.
                 */
                for (Song s : newPlaylist.getSongsInPlayList()) {
                    s.setNext(null);
                    s.setPrev(null);
                }

                /*
                Add the new playlist to the navigation view and give it an icon
                and a click listener.
                 */
                MenuItem newPlaylistItem = navigationMenu.add(newPlaylistName);
                newPlaylistItem.setIcon((ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_menu_save, null)));
                navigationView.setNavigationItemSelectedListener(MP3Player.this);

                /*
                Now, store the new playlist in the shared preferences.
                 */
                SharedPreferences.Editor prefsEditor = prefs.edit();
                Gson gson = new Gson();

                prefsEditor.putString("PLAYLISTS", null);
                prefsEditor.apply();

                ArrayList<PlayList> playListsInMenu;
                String playlistString = prefs.getString("PLAYLISTS", null);
                /*
                Check shared prefs for existing playlists.w
                 */
                if (playlistString == null) {
                    playListsInMenu = new ArrayList<>();
                    playListsInMenu.add(newPlaylist);
                } else {
                    Type type = new TypeToken<ArrayList<PlayList>>() {
                    }.getType();
                    playListsInMenu = gson.fromJson(playlistString, type);
                    playListsInMenu.add(newPlaylist);

                }
                String json = gson.toJson(playListsInMenu);
                prefsEditor.putString("PLAYLISTS", json);
                prefsEditor.apply();
                Toast.makeText(MP3Player.this, "New Playlist Created", Toast.LENGTH_LONG).show();
                return;
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(MP3Player.this, "Shit went wrong yo!", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                if (musicservice.getMpSessionID() != AudioManager.ERROR) {
                    equalizer = new Equalizer(1, musicservice.getMpSessionID());
                    equalizer.setEnabled(true);
                    equalizer.getNumberOfBands();
                    equalizer.getBandLevelRange();
                    equalizer.getNumberOfPresets();
                    equalizer.setParameterListener(EQlistener);
                    equalizer.setBandLevel((short) 0, equalizer.getBandLevel((short) 0));
                    equalizer.setBandLevel((short) 1, equalizer.getBandLevel((short) 1));
                    equalizer.setBandLevel((short) 2, equalizer.getBandLevel((short) 2));
                    equalizer.setBandLevel((short) 3, equalizer.getBandLevel((short) 3));
                    equalizer.setBandLevel((short) 4, equalizer.getBandLevel((short) 4));

                    Toast.makeText(MP3Player.this, "EQ set.", Toast.LENGTH_LONG).show();
                }

            }
            else {
                Toast.makeText(MP3Player.this, "Couldn't set equalizer.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mp3_player, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.addPlaylist) {
            Intent intent = new Intent(MP3Player.this, AddPlaylistActivity.class);
            //startActivity(intent);
            startActivityForResult(intent, 1);
            return true;
        }
        else if (id == R.id.allSongs) {
            allSongs();
        }
        else {
            updateSongList(navigationView.indexOfChild(menuItem.getActionView()));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.closeApp) {
            musicservice.tearDownNotification();
            appTerminated(isBound, connection);
            this.finish();
        } else if (id == R.id.shareSong) {
            if (!currSongName.getText().toString().equals("")) {
                String songName = currSongName.getText().toString();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "I am listening to: " + songName + " on Daniels sick mp3");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            } else {
                Toast.makeText(this, "No song playing.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.loopSong) {
            if (!currSongName.getText().toString().equals("")) {
                loopSong(item);
            }
            else {
                Toast.makeText(this, "No song playing.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.equalizer) {
            Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
            //intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION,);
            if ((intent.resolveActivity(getPackageManager()) != null)) {
                startActivityForResult(intent, 2);
            }
            else {
                Toast.makeText(this, "No default equalizer on device.", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void loopSong(MenuItem item) {
        if (item.getTitle().toString().equals("Loop current song")) {
            loopingRealNext = musicservice.getCurrentSongPlaying().getNext();
            musicservice.getCurrentSongPlaying().setNext(musicservice.getCurrentSongPlaying());
            item.setTitle("Stop looping");
        }
        else if (item.getTitle().toString().equals("Stop looping")) {
            musicservice.getCurrentSongPlaying().setNext(loopingRealNext);
            item.setTitle("Loop current song");
        }
    }

    public static void appTerminated(Boolean b, ServiceConnection c) {
        if (b && c != null && musicservice != null) {
            musicservice.unbindService(c);
            musicservice.stopSelf();

        }
        //musicservice.stopForeground(true);
    }

    /**
     * Async task to handle creation and saving of new playlists.
     * ##DOES NOT WORK YET##
     */
    public abstract class MyAsyncTask extends AsyncTask<Object , Object , Object > {
        protected boolean doInBackground(Intent data, Object... urls) {
            String newPlaylistName = data.getStringExtra("playListName");
            Bundle b = data.getExtras();
            final PlayList newPlaylist = new PlayList();
            newPlaylist.setSongsInPlayList((ArrayList<Song>) b.getSerializable("array"));
            newPlaylist.setPlayListName(newPlaylistName);

                /*
                When storing a new playlist, the next and previous song references
                needs to be nulled in order for them to be able to be saved in the
                preferences.
                These next and prev references are restored when a playlist is loaded
                again from the preferences.
                 */
            for (Song s : newPlaylist.getSongsInPlayList()) {
                s.setNext(null);
                s.setPrev(null);
            }

                /*
                Add the new playlist to the navigation view and give it an icon
                and a click listener.
                 */
            MenuItem newPlaylistItem = navigationMenu.add(newPlaylistName);
            newPlaylistItem.setIcon((ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_menu_save, null)));
            navigationView.setNavigationItemSelectedListener(MP3Player.this);

                /*
                Now, store the new playlist in the shared preferences.
                 */
            SharedPreferences.Editor prefsEditor = prefs.edit();
            Gson gson = new Gson();

            prefsEditor.putString("PLAYLISTS", null);
            prefsEditor.apply();

            ArrayList<PlayList> playListsInMenu;
            String playlistString = prefs.getString("PLAYLISTS", null);
                /*
                Check shared prefs for existing playlists.w
                 */
            if (playlistString == null) {
                playListsInMenu = new ArrayList<>();
                playListsInMenu.add(newPlaylist);
            } else {
                Type type = new TypeToken<ArrayList<PlayList>>() {
                }.getType();
                playListsInMenu = gson.fromJson(playlistString, type);
                playListsInMenu.add(newPlaylist);

            }
            String json = gson.toJson(playListsInMenu);
            prefsEditor.putString("PLAYLISTS", json);
            prefsEditor.apply();
            return true;
        }
        @Override
        protected void onPreExecute() {

            // this will execute on main thread before Method doInBackground()

            super.onPreExecute();
        }

        protected void onPostExecute(Object result) {
            // this will execute on main thread after Method doInBackground()

        }
    }
}
