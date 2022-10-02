package example.org.spottest;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper class for Apple Music Web API calls.
 */

public class AppleMusicAPIHelper {
    /** Apple Music user token for API calls. */
    private String musicUserToken;

    /** Apple Music developer token for API calls. */
    private String developerToken;

    /** List of tracks inputted by the client using OCR. */
    private List<Track> tracks;

    /** New playlist name. */
    private String playlistName;

    /** Volley Queue for networking requests. */
    private RequestQueue queue;

    /** Volley Callback class. */
    private IResult iResult;

    /** Count of successfully fetched tracks. */
    private int successfulTracks = PhotoInfoHelper.playlistInfo.getTracks().size();

    /**
     * Constructor for AppleMusicAPIHelper
     *
     * @param musicUserToken    the music user token of the client
     * @param developerToken    the developer token of the developer
     * @param tracks            the tracks to create a playlist for
     * @param playlistName      the name of the new playlist
     * @param queue             the volley request queue that handles networking requests
     */
    public AppleMusicAPIHelper(String musicUserToken, String developerToken, List<Track> tracks, String playlistName, RequestQueue queue, IResult iResult) {
        this.musicUserToken = musicUserToken;
        this.developerToken = developerToken;
        this.tracks = tracks;
        this.playlistName = playlistName;
        this.queue = queue;
        this.iResult = iResult;
    }

    /**
     * Wrapper method to create a playlist for all the tracks.
     */
    public void createPlaylist() {
        getTracks(0);
    }

    /**
     * Creates a playlist for all the tracks by calling the Apple Music API.
     *
     * @throws JSONException    if there is an error processing the JSON
     */
    private void createPlaylistFromTracks() throws JSONException {

        String createPlaylistUri = "https://api.music.apple.com/v1/me/library/playlists";

        JSONObject attributes = new JSONObject()
                .put("name", playlistName)
                .put("description", "");

        JSONObject relationships = new JSONObject();
        JSONObject tracks = new JSONObject();
        JSONArray data = new JSONArray();

        for (int i = 0; i < this.tracks.size(); i++) {

            if (this.tracks.get(i).wasFound()) {

                JSONObject song = new JSONObject()
                        .put("id", this.tracks.get(i).getId())
                        .put("type", "songs");

                data.put(song);
            }
        }

        tracks.put("data", data);
        relationships.put("tracks", tracks);

        JSONObject payload = new JSONObject()
                .put("attributes", attributes)
                .put("relationships", relationships);

        System.out.println("PAYLOAD: " + payload);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, createPlaylistUri, payload, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Playlist created!
                        iResult.notifySuccess(successfulTracks);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("createApplePlaylist", error.toString());
                        iResult.notifyError();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + developerToken);
                headers.put("Music-User-Token", musicUserToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    /**
     * Fetches the Apple Music IDs of all the inputted tracks.
     *
     * @param index     the index of the track in the track list
     */
    private void getTracks(final int index) {

        final String getTrackUri = generateTrackURI(tracks.get(index));
        System.out.println(getTrackUri);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, getTrackUri, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            tracks.get(index).setId(null);
                            tracks.get(index).setWasFound(false);

                            // Parse JSON to retrieve track ID
                            System.out.println("TRACK " + index + ": " + response.toString());
                            JSONObject results = response.getJSONObject("results");
                            JSONObject songs = results.getJSONObject("songs");
                            JSONArray data = songs.getJSONArray("data");
                            JSONObject song = data.getJSONObject(0);

                            String songID = song.getString("id");
                            // Set the ID of the track
                            tracks.get(index).setId(songID);
                            tracks.get(index).setWasFound(true);

                            Log.d("SUCCESS** APPLEMUSIC_songID", getTrackUri);

                        } catch (JSONException e) {
                            System.out.println("TRACK URI NOT FOUND: " + getTrackUri);
                            successfulTracks--;
                            e.printStackTrace();
                        }

                        // If the current index is less than the track list size, get the next track
                        // Otherwise, add all songs to the playlist
                        if (index < tracks.size() - 1) {
                            getTracks(index + 1);
                        } else {

                            Log.d("APPLE_getLastTrack", "Creating playlist!");

                            try {
                                createPlaylistFromTracks();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("APPLE_getTracksError", error.toString());

                        successfulTracks--;

                        // If the current index is less than the track list size, get the next track
                        // Otherwise, add all songs to the playlist
                        if (index < tracks.size() - 1) {
                            getTracks(index + 1);
                        } else {

                            Log.d("APPLE_getLastTrack", "Creating playlist!");

                            try {
                                createPlaylistFromTracks();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + developerToken);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    /**
     * Create the Search URI for a specific track.
     * Example URI:
     * https://api.music.apple.com/v1/catalog/us/search?term=after+hours+the+weeknd&limit=1&types=songs
     *
     * @param track     the track to create the search URI for
     * @return          the Search URI for the track
     */
    private String generateTrackURI(Track track) {

        // Error handling for empty strings

        String titleString = track.getTitle().trim();
        String artistString = track.getArtist().trim();

        System.out.println(titleString);
        System.out.println(artistString);


        // Index Of returns -1 when the character is not found in the string
        // Remove Parenthesis and Brackets
        int indexOfParenthesis = titleString.indexOf("(");

        if (indexOfParenthesis != -1) {
            titleString = titleString.substring(0, indexOfParenthesis).trim();
        }

        int indexOfBracket = titleString.indexOf("[");

        if (indexOfBracket != -1) {
            titleString = titleString.substring(0, indexOfBracket).trim();
        }

        String[] titleArray = titleString.split(" ");
        String[] artistArray = artistString.split(" ");

        StringBuilder sb = new StringBuilder();
        sb.append("https://api.music.apple.com/v1/catalog/us/search?term=");

        for (String titleComponent : titleArray) {

            if (titleComponent.contains("&") || titleComponent.toLowerCase().contains("feat")) {
                continue;
            }

            titleComponent = replaceStringPunctuation(titleComponent);

            sb.append(titleComponent);
            sb.append("+");
        }

        for (String artistComponent : artistArray) {

            if (artistComponent.contains("&") || artistComponent.toLowerCase().contains("feat")) {
                continue;
            }

            artistComponent = replaceStringPunctuation(artistComponent);

            sb.append(artistComponent);
            sb.append("+");
        }

        // Delete the extra '+'
        sb.deleteCharAt(sb.length() - 1);

        sb.append("&limit=1&types=songs");

        return sb.toString();
    }

    private String replaceStringPunctuation(String word) {
        word = word.replace(",", "");
        word = word.replace("'", "");
//        word = word.replace("-", "");
        word = word.replace("(", "");
        word = word.replace(")", "");
        word = word.replace("[", "");
        word = word.replace("]", "");

        return word;
    }
}
