package example.org.spottest;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TrackListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TrackAdapter customAdapter;
    private Button continueButton;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        System.out.println("NUMBER OF TRACKS: " + PhotoInfoHelper.playlistInfo.getTracks().size());
        recyclerView = findViewById(R.id.recycler_view);
//        continueButton = findViewById(R.id.continue_btn);

        FloatingActionButton mailBtn = findViewById(R.id.fab);
//        Button mailBtn = findViewById(R.id.next_button);
        editText = findViewById(R.id.playlist_name_editText);

        if (PhotoInfoHelper.playlistName != null) {
            editText.setText(PhotoInfoHelper.playlistName);
        }

        if (PhotoInfoHelper.playlistInfo.getTracks().size() == 0) {
            Toast.makeText(getApplicationContext(),"No songs found. Please try another picture.", Toast.LENGTH_LONG).show();
            PhotoInfoHelper.photos.clear();
            PhotoInfoHelper.playlistInfo.cleanPlaylistInfo();
            Intent intent = new Intent(TrackListActivity.this, MainActivity.class);
            startActivity(intent);

        }

        customAdapter = new TrackAdapter(this, PhotoInfoHelper.playlistInfo.getTracks());
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));


        mailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 // Create intent.
                PhotoInfoHelper.playlistName = editText.getText().toString();
                System.out.println(PhotoInfoHelper.playlistName);

                if (TextUtils.isEmpty(editText.getText().toString())) {
                    editText.setError("Please enter a playlist name");
                } else {
                    PhotoInfoHelper.playlistInfo.removeEmptyTracks(); // removes empty tracks from global track list

                    if (PhotoInfoHelper.playlistInfo.getTracks().size() == 0) {
                        Toast.makeText(getApplicationContext(),"No songs found. Please try another picture.", Toast.LENGTH_SHORT).show();
                        PhotoInfoHelper.playlistInfo.cleanPlaylistInfo();
                        PhotoInfoHelper.photos.clear();

                    } else {
                        Intent intent = new Intent(TrackListActivity.this, ServiceToActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        PhotoInfoHelper.playlistInfo.cleanPlaylistInfo();
        startActivity(new Intent(TrackListActivity.this, ChoosePlaylistTypeActivity.class));
        finish();

    }
}
