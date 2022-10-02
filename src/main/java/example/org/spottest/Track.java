package example.org.spottest;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * Parcelable class for a Spotify Track.
 * We need this class to be Parcelable to package them into intents and send them into
 * subsequent activities.
 */
public class Track implements Parcelable {

    /** The title of the track. */
    private String title;

    /** The artist performing the track. */
    private String artist;

    /** The album that the track is in. */
    private String album;

    /** The Spotify ID of the track. */
    private String id;

    /** Holds whether the track was found with the set title, artist, and album using the Spotify
     *  Web API */
    private boolean wasFound;

    // Overloaded constructor for tracks that have an album name
    public Track(String title, String artist, String album, String id) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.id = id;
        wasFound = false;
    }

    public Track(Parcel parcel) {
        this.title = parcel.readString();
        this.artist = parcel.readString();
        this.album = parcel.readString();
        this.id = parcel.readString();
    }

    // Overloaded constructor for tracks that don't have an album name
    public Track(String title, String artist) {
        this.title = title;
        this.artist = artist;
        this.album = "";
        this.id = null;
        this.wasFound = false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(artist);
        parcel.writeString(album);
        parcel.writeString(id);
    }

    public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {

        @Override
        public Track createFromParcel(Parcel parcel) {
            return new Track(parcel);
        }

        @Override
        public Track[] newArray(int i) {
            return new Track[i];
        }

    };

    public boolean isEmpty() {
        return this.getTitle().isEmpty() && this.getArtist().isEmpty();
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean wasFound() {
        return wasFound;
    }

    public void setWasFound(boolean wasFound) {
        this.wasFound = wasFound;
    }

    @Override
    public String toString() {
        return "Track{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}