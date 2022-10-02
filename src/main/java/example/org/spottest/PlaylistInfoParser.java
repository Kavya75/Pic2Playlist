package example.org.spottest;

import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.text.TextBlock;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PlaylistInfoParser {

    // List of all songs
    private List<Track> tracks = new ArrayList<>();

    // Playlist name entered by user
    private String playlistName;

    // Parser for Amazon Music and Billboard
    public void parseAmazonAndBillboard(SparseArray<TextBlock> s) {

        String trackName = null;
        String artistName = null;

        // System.out.println("parseAmazonAndBillboard() called");

        // Get list of strings
        ArrayList<String> stringList = createNewStringList(s);

        // System.out.println("parseAmazonPL: size of array() called  " + stringList.size());

        // Iterate through string list and assign trackName and artistName
        for (int i = 0; i < stringList.size(); i++) {
            if (i % 3 == 0) {
                trackName = stringList.get(i);
            } else if (i % 3 == 1) {
                artistName = stringList.get(i);
                Track p = new Track(trackName, artistName);
                tracks.add(p);
            }
        }

        //printTracks();
    }

    // Parser for Spotify and Apple Music
    public void parseSpotifyAndApple(SparseArray<TextBlock> s) {
        String trackName = null;
        String artistName = null;

        ArrayList<String> appleStringList = createNewStringList(s);

        for (int i = 0; i < appleStringList.size(); i++) {
            if (i % 2 == 0) {
                trackName = appleStringList.get(i);
            } else if (i % 2 == 1) {
                artistName = appleStringList.get(i);

                // Create new Track object
                Track p = new Track(trackName, artistName);

                // Add new Track object to tracks array
                tracks.add(p);
            }
        }
    }

    // For testing & debugging
    public void printTracks() {
        System.out.println("printTracks() called");

        for (int i = 0; i < tracks.size(); i++) {
            Track t = tracks.get(i);
            System.out.println(t.getTitle());
            System.out.println(t.getArtist());
            System.out.println("--------");
        }
    }

    public ArrayList<String> createNewStringList(SparseArray<TextBlock> textblocks) {

        // System.out.println("*******createNewString() called********");

        ArrayList<String> flatten = new ArrayList<String>();


        for (int i = 0; i < textblocks.size(); i++) {

            // Get current textblock in sparse array
            String currTextBlock = textblocks.valueAt(i).getValue();

            // Split textblock in string array: every line is one string
            String[] splitTextBlocks = currTextBlock.split("\n");

//            if (splitTextBlocks.length == 1) {
//                continue;
//            }


            if ( (PhotoInfoHelper.playlistSource == PhotoInfoHelper.UserChoiceEnum.BILLBOARD) && (splitTextBlocks.length == 2) ) {
                splitTextBlocks = addThirdElement(splitTextBlocks, "-");
            }
            // ignores textblocks with only one item
            else if ( (PhotoInfoHelper.playlistSource == PhotoInfoHelper.UserChoiceEnum.BILLBOARD) && (splitTextBlocks.length == 1) ) {
                continue; // go to next textblock in loop
            }

            for (String splitText : splitTextBlocks) {

                if (splitText.contains("|")) {
                    splitText = splitText.replace('|', 'I');   // when the OCR mistakes I for |
                }

                flatten.add(splitText);
            }
        }

        // For testing
        System.out.println("------------ Printing flatten -----------");
        for (String text : flatten) {
            System.out.println(text);
        }
        System.out.println("------------ Printing flatten -----------");


        return flatten; // Return list of all the string lines in the textblock
    }


    public void removeEmptyTracks() {

        List<Track> tracksToRemove = new ArrayList<>();

        for (Track track : this.tracks) {
            if (track.isEmpty()) {
                tracksToRemove.add(track);
            }
        }

        this.tracks.removeAll(tracksToRemove);
    }

    private String[] addThirdElement(String[] originalArray, String newItem)
    {
        int currentSize = originalArray.length;
        int newSize = currentSize + 1;
        String[] tempArray = new String[ newSize ];

        for (int i = 0; i < currentSize; i++) {
            tempArray[i] = originalArray[i];
        }

        tempArray[newSize - 1] = newItem;
        return tempArray;
    }

    public void cleanPlaylistInfo() {
        tracks.clear();
        playlistName = null;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public void setTracks(ArrayList<Track> t) {
        this.tracks = t;
    }

    public List<Track> getTracks() {
        return this.tracks;
    }


//  public void parseBillboard(SparseArray<TextBlock> s) {
//        String trackName = null;
//        String artistName = null;
//
//        //System.out.println("****** billboard parser called *******");
//        ArrayList<String> bbStringList = createNewStringList(s);
//
//        //System.out.println("************** flattened ***********");
//
//        for (int i = 0; i < bbStringList.size(); i++) {
//            System.out.println(bbStringList.get(i));
//        }
//
//        for (int i = 0; i < bbStringList.size(); i++) {
//            if (i % 3 == 0) {
//                trackName = bbStringList.get(i);
//            } else if (i % 3 == 1) {
//                artistName = bbStringList.get(i);
//            } else {
//                Track p = new Track(trackName, artistName);
//                tracks.add(p);
//            }
//        }
//
//    }
}