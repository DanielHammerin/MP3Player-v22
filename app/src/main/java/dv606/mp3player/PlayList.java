package dv606.mp3player;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Daniel on 2016-10-18.
 */
public class PlayList implements Serializable {
    private String playListName;
    private ArrayList<Song> songsInPlayList;

    public PlayList() {

    }

    public String getPlayListName() {
        return playListName;
    }

    public void setPlayListName(String playListName) {
        this.playListName = playListName;
    }

    public ArrayList<Song> getSongsInPlayList() {
        return songsInPlayList;
    }

    public void setSongsInPlayList(ArrayList<Song> songsInPlayList) {
        this.songsInPlayList = songsInPlayList;
    }
}
