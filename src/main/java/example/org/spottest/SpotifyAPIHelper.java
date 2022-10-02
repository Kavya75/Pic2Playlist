package example.org.spottest;

import android.app.ProgressDialog;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

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
 * Wrapper class for Spotify Web API calls.
 */
public class SpotifyAPIHelper {

    /** User ID of the current client. */
    private String userID;

    /** Authentication token of the current client. */
    private String authToken;

    /** List of tracks inputted by the client using OCR. */
    private List<Track> tracks;

    /** New playlist ID. */
    private String playlistID;

    /** New playlist name. */
    private String playlistName;

    /** Volly Queue for networking requests. */
    private RequestQueue queue;

    /** Volley Callback class. */
    private IResult iResult;

    /** Count of successfully fetched tracks. */
    private int successfulTracks = PhotoInfoHelper.playlistInfo.getTracks().size();

    /**
     * Constructor for the SpotifyAPIHelper
     *
     * @param userID            the user ID of the client
     * @param authToken         the authentication token of the client
     * @param tracks            the tracks inputted by the client
     * @param playlistName      the playlist name inputted by the client
     * @param queue             the volley request queue that handles networking requests
     */


    public SpotifyAPIHelper(String userID, String authToken, List<Track> tracks, String playlistName, RequestQueue queue, IResult iResult) {
        this.userID = userID;
        this.authToken = authToken;
        this.tracks = tracks;

        this.playlistID = null;
        this.playlistName = playlistName;

        this.queue = queue;
        this.iResult = iResult;
    }

    /**
     * Top level function call for creating the playlist for the given tracks.
     *
     * @throws JSONException    if there is an error processing the JSON
     */
    public void createPlaylist() throws JSONException {

        // Web API to call
        String createPlaylistUri = "https://api.spotify.com/v1/users/" + userID + "/playlists";

        // Payload of the playlist name
        JSONObject payload = new JSONObject();
        payload.put("name", playlistName);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, createPlaylistUri, payload, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("SPOTIFYAPP", "PLAYLIST " + "\"" + playlistName + "\" CREATED");

                        // Set the playlistID variable
                        try {
                            playlistID = response.getString("id");
                            Log.d("SPOTIFYAPP_playlistID", playlistID);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Get the first track
                        getTracks(0);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SPOTIFYAPP_createPlaylist", error.toString());
                        iResult.notifyError();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    /**
     * Get the current indexed track ID using the Spotify Web API.
     *
     * @param index     the index of the track in the track list
     */
    private void getTracks(final int index) {

        // Web API to call
        String getTrackUri = generateTrackURI(tracks.get(index));

        System.out.println("GET TRACK URI for Index " + index + ": " + getTrackUri);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, getTrackUri, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            tracks.get(index).setId(null);
                            tracks.get(index).setWasFound(false);

                            // Parse JSON to retrieve track ID
                            JSONObject jsonTracks = response.getJSONObject("tracks");
                            JSONArray items = jsonTracks.getJSONArray("items");
                            JSONObject firstItem = items.getJSONObject(0);
                            String songID = firstItem.getString("id");

                            // Set the ID of the track
                            tracks.get(index).setId(songID);
                            tracks.get(index).setWasFound(true);

                            Log.d("SPOTIFYAPP_songID", songID);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            successfulTracks--;
                        }

                        // If the current index is less than the track list size, get the next track
                        // Otherwise, add all songs to the playlist
                        if (index < tracks.size() - 1) {
                            getTracks(index + 1);
                        } else {

                            Log.d("SPOTIFYAPP_getTracksOnResponse", "Adding songs to playlist!");

                            try {
                                addSongsToPlaylist();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SPOTIFYAPP_getTracksError", error.toString());
                        successfulTracks--;

                        if (index < tracks.size() - 1) {
                            getTracks(index + 1);
                        } else {

                            Log.d("SPOTIFYAPP_getTracksOnResponse", "Adding songs to playlist!");

                            try {
                                addSongsToPlaylist();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    /**
     * Add all tracks to the new playlist that was created.
     *
     * @throws JSONException    if JSON parsing goes wrong
     */
    private void addSongsToPlaylist() throws JSONException {

        // Web API to call
        String addToPlaylistUri = "https://api.spotify.com/v1/playlists/" + playlistID + "/tracks";

        // Payload of Spotify Track URIs in the form of "spotify:track:{track ID here}"
        JSONObject payload = new JSONObject();
        JSONArray uris = new JSONArray();

        for (Track track : tracks) {

            // Only add the URI to the payload if it was found during the search.
            if (track.wasFound()) {
                uris.put("spotify:track:" + track.getId());
            }
        }

        payload.put("uris", uris);

        Log.d("SPOTIFYAPP_playlistPayload", payload.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, addToPlaylistUri, payload, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("SPOTIFYAPP_addSongsOnResponse", "Added songs to playlist.");
                        iResult.notifySuccess(successfulTracks);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SPOTIFYAPP_addSongsToPlaylistError", error.toString());
                        iResult.notifyError();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    /**
     * Create the Search URI for a specific track.
     * Example URI:
     * https://api.spotify.com/v1/search?q=track:out+west+artist:jackboys&type=track&limit=1
     *
     * @param track     the track to create the search URI for
     * @return          the Search URI for the track
     */
    private String generateTrackURI(Track track) {

        // All spaces in the URI need to be "+" symbols
//        String trackTitleQuery = track.getTitle().replaceAll(" ", "+").toLowerCase();
//        String trackArtistQuery = track.getArtist().replaceAll(" ", "+").toLowerCase();
//        String trackAlbumQuery = track.getAlbum().replaceAll(" ", "+").toLowerCase();

        String titleString = track.getTitle().trim();
        String artistString = track.getArtist().trim();
        String albumString = track.getAlbum().trim();

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
        String[] albumArray = albumString.split(" ");

        StringBuilder uriSB = new StringBuilder();

        uriSB.append("https://api.spotify.com/v1/search?q=");

        // There are some cases where the album or artist name is empty.
        // Don't add the artist/album query attributes in this case.
        if (!titleString.isEmpty()) {
            uriSB.append("track:");

            for (String titleComponent : titleArray) {

                if (titleComponent.contains("&") || titleComponent.toLowerCase().contains("feat")) {
                    continue;
                }

                titleComponent = replaceStringPunctuation(titleComponent);

                uriSB.append(titleComponent);
                uriSB.append("+");
            }
        }

        if (!artistString.isEmpty()) {
            uriSB.append("artist:");

            for (String artistComponent : artistArray) {

                if (artistComponent.contains("&") || artistComponent.toLowerCase().contains("feat")) {
                    continue;
                }

                artistComponent = replaceStringPunctuation(artistComponent);

                uriSB.append(artistComponent);
                uriSB.append("+");
            }
        }

        uriSB.deleteCharAt(uriSB.length() - 1);

        if (!albumString.isEmpty()) {
            uriSB.append("+album:");

            for (String albumComponent : albumArray) {

                if (albumComponent.contains("&") || albumComponent.toLowerCase().contains("feat")) {
                    continue;
                }

                albumComponent = replaceStringPunctuation(albumComponent);

                uriSB.append(albumComponent);
                uriSB.append("+");
            }

            uriSB.deleteCharAt(uriSB.length() - 1);
        }

        uriSB.append("&type=track&limit=1");

        return uriSB.toString();
    }

    private String replaceStringPunctuation(String word) {
        word = word.replace(",", "");
//        word = word.replace("-", "");
        word = word.replace("'", "");
        word = word.replace("(", "");
        word = word.replace(")", "");
        word = word.replace("[", "");
        word = word.replace("]", "");

        return word;
    }
}
