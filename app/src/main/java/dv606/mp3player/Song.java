package dv606.mp3player;

/**
 * A simple class representing MP3 songs (tracks) in a playlist.
 *
 * Created by Oleksandr Shpak in 2013.
 * Ported to Android Studio by Kostiantyn Kucher in 2015.
 * Last modified by Kostiantyn Kucher on 10/09/2015.
 */
public class Song {

    private final String artist;
    private final String album;
    private final String name;
    private final String path;

    private final long duration;
    private Song next = null;
    private Song prev = null;

    public Song(String artist, String album, String name, String path, long dur)
    {
        this.artist = artist;
        this.album = album;
        this.name = name;
        this.path = path;
        this.duration = dur;
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

        if (album != null)
            result = result + " (" + album + ")";

        return result;
    }
}
