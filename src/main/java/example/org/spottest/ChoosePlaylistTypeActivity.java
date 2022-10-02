package example.org.spottest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;


public class ChoosePlaylistTypeActivity extends AppCompatActivity {

    Button nextButton;
    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_playlist_type);
        getSupportActionBar().hide();

        nextButton = findViewById(R.id.button2);
        radioGroup = findViewById(R.id.radio_group);

        if (PhotoInfoHelper.playlistSource != PhotoInfoHelper.UserChoiceEnum.NONE) {
            switch (PhotoInfoHelper.playlistSource) {

                case AMAZON:
                    radioGroup.check(R.id.amazon_music);
                    break;

                case SPOTIFY:
                    radioGroup.check(R.id.spotify);
                    break;

                case APPLE:
                    radioGroup.check(R.id.apple_music);
                    break;

                case BILLBOARD:
                    radioGroup.check(R.id.billboard);
                    break;
            }
        }

        nextButton.setOnClickListener(new View.OnClickListener() {

            Intent intent = null;

            @Override
            public void onClick(View arg0) {

                switch (PhotoInfoHelper.playlistSource) {

                    case AMAZON:
                    case SPOTIFY:
                    case APPLE:
                    case BILLBOARD: // all cases are same except for none
                        intent = new Intent(ChoosePlaylistTypeActivity.this, ImageToTextActivity.class);
                        startActivity(intent);
                        break;

                    case NONE:
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Please choose a playlist source",
                                Toast.LENGTH_SHORT);
                        toast.show();
                        break;

                }
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the importPhotosButton now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio importPhotosButton was clicked
        switch(view.getId()) {
            case R.id.amazon_music:
                if (checked) {
                    PhotoInfoHelper.playlistSource = PhotoInfoHelper.UserChoiceEnum.AMAZON;
                    break;
                }

            case R.id.spotify:
                if (checked) {
                    PhotoInfoHelper.playlistSource = PhotoInfoHelper.UserChoiceEnum.SPOTIFY;
                    break;
                }

            case R.id.apple_music:
                if (checked) {
                    PhotoInfoHelper.playlistSource = PhotoInfoHelper.UserChoiceEnum.APPLE;
                    break;
                }

            case R.id.billboard:
                if (checked) {
                    PhotoInfoHelper.playlistSource = PhotoInfoHelper.UserChoiceEnum.BILLBOARD;
                    break;
                }
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        // clear all
        PhotoInfoHelper.playlistSource = PhotoInfoHelper.UserChoiceEnum.NONE;
        PhotoInfoHelper.photos.clear();
        PhotoInfoHelper.playlistName = null;
        PhotoInfoHelper.playlistDestination = PhotoInfoHelper.UserChoiceEnum.NONE;
        PhotoInfoHelper.playlistInfo.cleanPlaylistInfo();

        startActivity(new Intent(ChoosePlaylistTypeActivity.this, MainActivity.class));
        finish();

    }
}
