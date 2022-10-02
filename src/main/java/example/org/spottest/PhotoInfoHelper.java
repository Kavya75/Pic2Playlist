package example.org.spottest;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class PhotoInfoHelper {

    public static enum UserChoiceEnum {
        SPOTIFY,
        AMAZON,
        APPLE,
        BILLBOARD,
        NONE;
    }

    public static UserChoiceEnum playlistSource = UserChoiceEnum.NONE;

    public static UserChoiceEnum playlistDestination = UserChoiceEnum.NONE;

    public static ArrayList<Bitmap> photos = new ArrayList<>();

    public static PlaylistInfoParser playlistInfo = new PlaylistInfoParser();

    public static String playlistName = null;

}
