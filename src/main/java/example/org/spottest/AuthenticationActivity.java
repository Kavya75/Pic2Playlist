package example.org.spottest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.apple.android.sdk.authentication.AuthenticationFactory;
import com.apple.android.sdk.authentication.AuthenticationManager;
import com.apple.android.sdk.authentication.TokenError;
import com.apple.android.sdk.authentication.TokenResult;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity view of all music platforms to create the playlist in.
 */
public class AuthenticationActivity extends AppCompatActivity {

    /** Client ID for this project. */
    final String SPOTIFY_CLIENT_ID = "0679547c1e764c9bb94a5d9e63fe4473";
 
    /** Redirect URI that Spotify uses to confirm auth. This is a random URI.*/
    final String SPOTIFY_REDIRECT_URI = "spot-test://callback";

    /** Request Code that Spotify uses to confirm auth with the client. This is a random integer. */
    final int SPOTIFY_AUTH_REQUEST_CODE = 1337;

    /** Authorization token that the client receives when they authenticate with Spotify. */
    String spotifyAuthToken = null;

    /** User ID of the client. */
    String spotifyUserID = null;

    /** Apple Music Authentication Request Code **/
    final int APPLE_MUSIC_AUTH_REQUEST_CODE = 5002;

    /** Apple Music Developer Token (needs to be updated in 6 months using https://github.com/pelauimagineering/apple-music-token-generator) **/
    final String APPLE_MUSIC_DEVELOPER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsImtpZCI6IkdBUk5NOTk3OTMifQ.eyJpc3MiOiJEVVk2SlFXSjZRIiwiZXhwIjoxNjAzNzc2MTI3LCJpYXQiOjE1ODgyMjQxMjd9.gS_Jid-DC0D1xZvJIzh7ktWfKN8DXbDdJnPzcTLPSo7Bt8pdG_JKnUzmK2xbksfUuv_noVfJJHQ7J7NzSo1u6g";

    /** Apple Music Authentication Manager **/
    AuthenticationManager appleAuthenticationManager = null;

    /** Apple Music Auth Token **/
    String appleMusicUserToken = null;

    /** List of tracks that the client provides through OCR. */
    List<Track> tracks;

    /** Volley Queue for networking requests. */
    RequestQueue queue;

    // success/failure textview
    private TextView textView = null;

    private Button goBackToWelcome = null;

    private ProgressDialog progressDialog;

    private IResult mResultCallback;

    /**
     * Creates the authentication page view.
     *
     * @param savedInstanceState    the generic saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        getSupportActionBar().hide();

        textView = findViewById(R.id.success_failure_textview);
        goBackToWelcome = findViewById(R.id.go_home);
        progressDialog = new ProgressDialog(AuthenticationActivity.this);
        progressDialog.setMessage("Creating playlist...");
        progressDialog.setCancelable(false);

        goBackToWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PhotoInfoHelper.playlistSource = PhotoInfoHelper.UserChoiceEnum.NONE;
                PhotoInfoHelper.photos.clear();
                PhotoInfoHelper.playlistName = null;
                PhotoInfoHelper.playlistDestination = PhotoInfoHelper.UserChoiceEnum.NONE;
                PhotoInfoHelper.playlistInfo.cleanPlaylistInfo();

                Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


        // Retrieve list of tracks and playlist name from the previous activity
        tracks = PhotoInfoHelper.playlistInfo.getTracks();

        // Initialize Volley Request Queue
        queue = Volley.newRequestQueue(this);

        mResultCallback = new IResult() {
            @Override
            public void notifySuccess(int successfulTracks) {
                StringBuffer sb = new StringBuffer("Success! ");
                sb.append(successfulTracks + " tracks out of " + PhotoInfoHelper.playlistInfo.getTracks().size() + " found. ");
                if (PhotoInfoHelper.playlistDestination == PhotoInfoHelper.UserChoiceEnum.SPOTIFY) {
                    sb.append("The playlist \""+ PhotoInfoHelper.playlistName + "\" was created in your Spotify account.");
                } else if (PhotoInfoHelper.playlistDestination == PhotoInfoHelper.UserChoiceEnum.APPLE) {
                    sb.append("The playlist \""+ PhotoInfoHelper.playlistName + "\" was created in your Apple account.");
                }
                textView.setText(sb);
                textView.setVisibility(View.VISIBLE);
                goBackToWelcome.setVisibility(View.VISIBLE);

                progressDialog.dismiss();

            }

            @Override
            public void notifyError() {
                StringBuffer sb = new StringBuffer("Something went wrong. Please try again! ");
                textView.setText(sb);
                textView.setVisibility(View.VISIBLE);
                goBackToWelcome.setVisibility(View.VISIBLE);

                progressDialog.dismiss();
            }
        };

        switch (PhotoInfoHelper.playlistDestination) {
            case SPOTIFY:
                authenticateSpotify();
                break;

            case APPLE:
                authenticateAppleMusic();
                break;
        }
    }

    /**
     * Helper function that authenticates Spotify by opening up a LoginActivity.
     */
    private void authenticateSpotify() {

        // Build a new Authentication Request using the Client ID and Redirect URI defined above
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(SPOTIFY_CLIENT_ID, AuthenticationResponse.Type.TOKEN, SPOTIFY_REDIRECT_URI);

        // Permissions of this Authorization Token
        // (etc. we want to modify playlist and search for tracks, so those are the scopes we define.
        builder.setScopes(new String[]{"playlist-modify-public,user-read-private"});

        AuthenticationRequest request = builder.build();

        // Open the LoginActivity for the client to enter their information
        AuthenticationClient.openLoginActivity(this, SPOTIFY_AUTH_REQUEST_CODE, request);
    }

    /**
     * Helper function that authenticates Apple Music by opening up the Apple Music LoginActivity.
     */
    private void authenticateAppleMusic() {

        AuthenticationFactory authenticationFactory = new AuthenticationFactory();
        appleAuthenticationManager = authenticationFactory.createAuthenticationManager(getApplicationContext());

        Intent appleMusicIntent = appleAuthenticationManager.createIntentBuilder(APPLE_MUSIC_DEVELOPER_TOKEN)
                .setStartScreenMessage("Connect this application to Apple Music.")
                .build();

        startActivityForResult(appleMusicIntent, APPLE_MUSIC_AUTH_REQUEST_CODE);
    }

    /**
     * Callback from Authentication method. This function stores the authentication class variable
     * to use in later Spotify calls. It also retrieves the spotifyUserID via the getUserID() function.
     *
     * @param requestCode   the request code of the authentication request
     * @param resultCode    the result code of the authentication response
     * @param intent        the intent that isn't used
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct request
        if (requestCode == SPOTIFY_AUTH_REQUEST_CODE && PhotoInfoHelper.playlistDestination == PhotoInfoHelper.UserChoiceEnum.SPOTIFY) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    // Store Authentication Token
                    spotifyAuthToken = response.getAccessToken();

                    Log.d("SPOTIFYAPP_authToken", spotifyAuthToken);
                    progressDialog.show();

                    // Retrieve User ID
                    getUserId();

                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Log.e("SPOTIFYAPP_authError", "UNSUCCESSFUL AUTH");
                    goBackToWelcome.setVisibility(View.VISIBLE);
                    textView.setText("Something went wrong. Please try again!");
                    textView.setVisibility(View.VISIBLE);
                    progressDialog.dismiss();
                    System.out.println(response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        } else if (requestCode == APPLE_MUSIC_AUTH_REQUEST_CODE && PhotoInfoHelper.playlistDestination == PhotoInfoHelper.UserChoiceEnum.APPLE) {
            TokenResult tokenResult = appleAuthenticationManager.handleTokenResult(intent);

            if (!tokenResult.isError()) {
                appleMusicUserToken = tokenResult.getMusicUserToken();
                progressDialog.show();
            } else {
                // Error Handling here

                if (TokenError.TOKEN_FETCH_ERROR.getErrorCode() == tokenResult.getError().getErrorCode()) {
                    // Regenerate developer token
                }

                textView.setText("Something went wrong. Please try again!");
                textView.setVisibility(View.VISIBLE);
                progressDialog.dismiss();
                System.out.println("ERROR: " + tokenResult.getError() + ", ERROR " + tokenResult.getError().getErrorCode());
            }

            AppleMusicAPIHelper helper = new AppleMusicAPIHelper(appleMusicUserToken, APPLE_MUSIC_DEVELOPER_TOKEN, tracks, PhotoInfoHelper.playlistName, queue, mResultCallback);

            try {
                helper.createPlaylist();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Retrieve the User ID of the current client who is using the app.
     */
    private void getUserId() {

        // Web API to utilize
        String getUserUri = "https://api.spotify.com/v1/me";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, getUserUri, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            // Retrieve spotifyUserID from the JSONObject response
                            spotifyUserID = response.getString("id");
                            Log.d("SPOTIFYAPP_userID", spotifyUserID);

                            // Create the Playlist as a callback function
                            createPlaylistFromTracks();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SPOTIFY_getUserIDError", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + spotifyAuthToken);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    /**
     * Top level function that creates a playlist from the tracks inputted by the client using
     * Spotify Web APIs
     */
    private void createPlaylistFromTracks() {
        System.out.println("********** " + PhotoInfoHelper.playlistName + " **********");

        SpotifyAPIHelper helper = new SpotifyAPIHelper(spotifyUserID, spotifyAuthToken, tracks, PhotoInfoHelper.playlistName, queue, mResultCallback);

        // Create playlist
        try {
            helper.createPlaylist();
        } catch (Exception e) {
            textView.setText("Failed to create playlist. ");
            e.printStackTrace();
        }
    }
}
