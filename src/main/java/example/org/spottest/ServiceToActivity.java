package example.org.spottest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ServiceToActivity extends AppCompatActivity {

    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_to);
        getSupportActionBar().hide();

        nextButton = findViewById(R.id.button3);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(ServiceToActivity.this, AuthenticationActivity.class);

                if (PhotoInfoHelper.playlistDestination == PhotoInfoHelper.UserChoiceEnum.NONE) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Please choose a playlist destination",
                            Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        PhotoInfoHelper.playlistDestination = PhotoInfoHelper.UserChoiceEnum.NONE;
        startActivity(new Intent(ServiceToActivity.this, TrackListActivity.class));
        finish();

    }

    public void onRadioButtonClicked(View view) {
        // Is the importPhotosButton now checked?
        boolean isChecked = ((RadioButton) view).isChecked();

        // Check which radio importPhotosButton was clicked

        switch(view.getId()) {
            case R.id.amazon_music:
                PhotoInfoHelper.playlistDestination = PhotoInfoHelper.UserChoiceEnum.AMAZON;
                break;

            case R.id.spotify:
                PhotoInfoHelper.playlistDestination = PhotoInfoHelper.UserChoiceEnum.SPOTIFY;
                break;

            case R.id.apple_music:
                PhotoInfoHelper.playlistDestination = PhotoInfoHelper.UserChoiceEnum.APPLE;
                break;

        }


    }

}
