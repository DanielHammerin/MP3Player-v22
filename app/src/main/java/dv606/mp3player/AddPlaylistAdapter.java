package dv606.mp3player;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Daniel on 2016-10-18.
 */
public class AddPlaylistAdapter extends ArrayAdapter<Song> {
    private AddPlaylistActivity addPlaylistActivity;
    private int viewPos;

    public AddPlaylistAdapter(AddPlaylistActivity addPlaylistActivity, Context context, ArrayList<Song> objects) {
        super(context, 0, objects);
        this.addPlaylistActivity = addPlaylistActivity;
    }

    public void setCurrentSongPos(int pos) {
        viewPos = pos;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        Song data = getItem(position);

        row = addPlaylistActivity.getLayoutInflater().inflate(R.layout.layout_row, parent, false);

        TextView name = (TextView) row.findViewById(R.id.label);
        name.setText(String.valueOf(data));
        row.setTag(data);

        if (viewPos == position) {
            if (!AddPlaylistActivity.isAdded) {
                row.setBackgroundColor(Color.parseColor("#ff9966"));
            }
            else if (row.getBackground().equals(Color.parseColor("#ff9966"))){
                row.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
        }
        return row;
    }
}
