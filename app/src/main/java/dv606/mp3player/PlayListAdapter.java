package dv606.mp3player;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Daniel on 2016-10-18.
 */
public class PlayListAdapter extends ArrayAdapter<Song> {
    private MP3Player mp3Player;
    private int viewPos;

    public PlayListAdapter(MP3Player mp3Player, Context context, ArrayList<Song> objects) {
        super(context, 0, objects);
        this.mp3Player = mp3Player;
    }

    public void setCurrentSongPos(int pos) {
        viewPos = pos;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        Song data = getItem(position);

        row = mp3Player.getLayoutInflater().inflate(R.layout.layout_row, parent, false);

        TextView name = (TextView) row.findViewById(R.id.label);
        name.setText(String.valueOf(data));
        row.setTag(data);

        if (viewPos == position) {
            row.setBackgroundColor(Color.parseColor("#ff9966"));
        } else {
            row.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        return row;
    }
}
