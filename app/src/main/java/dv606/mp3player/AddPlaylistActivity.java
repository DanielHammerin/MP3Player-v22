package dv606.mp3player;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static dv606.mp3player.MP3Player.adapter;
import static dv606.mp3player.MP3Player.songs;
import static java.security.AccessController.getContext;

/**
 * Created by Daniel on 2016-10-10.
 */
public class AddPlaylistActivity extends AppCompatActivity {

    private ArrayList allsongslist = MP3Player.songs;
    private ArrayList<Song> newPlaylistSongsArray;
    private AddPlaylistAdapter adapter;
    private String playlistName;
    public static boolean isAdded;
    public EditText searchBar;
    /*
    public AddPlaylistActivity(ArrayList<Song> existingSongs) {
        this.newPlaylistSongsArray = existingSongs;
    }
    */

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.add_playlist_activity);

        ListView searchResultList = (ListView) findViewById(R.id.searchResults);
        adapter = new AddPlaylistAdapter(this, this, allsongslist);
        searchResultList.setAdapter(adapter);
        searchResultList.clearChoices();
        searchBar = (EditText) findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(filterWatcher);

        newPlaylistSongsArray = new ArrayList<>();

        searchResultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long arg3) {

                isAdded = false;
                for (Song s : newPlaylistSongsArray) {
                    if (s.equals(allsongslist.get(pos))) {
                        isAdded = true;
                    }
                    else {
                        isAdded = false;
                    }
                }
                if (!isAdded) {
                    //view.setPressed(true);
                    adapter.setCurrentSongPos(pos);
                    adapter.notifyDataSetChanged();
                    newPlaylistSongsArray.add((Song) allsongslist.get(pos));
                    //view.setBackgroundColor(Color.parseColor("#ff9966"));
                    Snackbar.make(view, "Added!", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
                else {
                    //view.setPressed(false);
                    adapter.setCurrentSongPos(pos);
                    adapter.notifyDataSetChanged();
                    newPlaylistSongsArray.remove(allsongslist.get(pos));
                    //view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    Snackbar.make(view, "Removed!", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
                //adapter.setCurrentSongPos(pos);
                //adapter.notifyDataSetChanged();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.saveButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder inputAlert = new AlertDialog.Builder(AddPlaylistActivity.this);
                inputAlert.setTitle("Playlist Name:");
                //inputAlert.setMessage("We need your name to proceed");
                final EditText userInput = new EditText(AddPlaylistActivity.this);
                inputAlert.setView(userInput);
                inputAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playlistName = userInput.getText().toString();
                        createPlaylist(playlistName, newPlaylistSongsArray);
                    }
                });
                inputAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = inputAlert.create();
                alertDialog.show();
                /*
                Snackbar.make(view, "Something went wrong!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                 */
            }
        });
    }
    public void createPlaylist(String playlistName, ArrayList<Song> newPlaylistSongsArray) {
        Intent returnIntent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable("array", newPlaylistSongsArray);
        returnIntent.putExtra("playListName", playlistName);
        returnIntent.putExtras(b);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    private TextWatcher filterWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.getFilter().filter(s);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
