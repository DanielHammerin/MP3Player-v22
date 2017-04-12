package dv606.mp3player;

import java.io.Serializable;

/**
 * A simple class representing MP3 songs (tracks) in a playlist.
 *
 * Created by Oleksandr Shpak in 2013.
 * Ported to Android Studio by Kostiantyn Kucher in 2015.
 * Last modified by Kostiantyn Kucher on 10/09/2015.
 */
public class Song implements Serializable{

    private final String artist;
    private final String album;
    private final String name;
    private final String path;

    private final long duration;
    private Song next = null;
    private Song prev = null;

    public Song(String artist, String album, String name, String path, long dur) {

        String trimmedName = name;
        if (trimmedName.contains(".mp3") || trimmedName.contains(".MP3")) {
            trimmedName = name.replace(".mp3","").replace(".MP3","");
        }
        if (trimmedName.contains("Lyrics") || name.contains("(lyrics)")) {
            trimmedName = name.replace("Lyrics", "").replace("(lyrics)", "");
        }
        if (album.contains("Music")) {
            String trimmedAlbum;
            trimmedAlbum = album.replace("Music","");
            this.album = trimmedAlbum;
        }
        else {
            this.album = album;
        }
        this.name = trimmedName;
        this.artist = artist;
        this.duration = dur;
        this.path = path;
    }
    public Song() {
        artist = "";
        album = "";
        name = "";
        path = "";
        duration = 0;
    }

    public String getArtist()
    {
        return artist;
    }

    public String getAlbum()
    {
        return album;
    }

    public String getName()
    {
        return name;
    }

    public String getPath()
    {
        return path;
    }

    public void setNext(Song song)
    {
        next = song;
    }

    public Song getNext()
    {
        return next;
    }

    public void setPrev(Song song) {prev = song;}

    public Song getPrev() {return prev;}

    public long getDuration() {
        return duration;
    }

    @Override
    public String toString()
    {
        String result = null;
        if (path != null)
            result = path;

        if (name != null)
            result = name;

        if (artist != null)
            result = artist + " - " + result;

        if (album != null && !album.equals("")) {
            result = result + " (" + album + ")";
        }
        else if (album != null && album.equals("")) {
            result = result + "";
        }

        return result;
    }
}
