package example.org.spottest;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import android.widget.ProgressBar;

import java.util.concurrent.atomic.AtomicBoolean;

public class ImageToTextActivity extends AppCompatActivity {

    ImageView image;

    TextView text;

    Button button2;

    private RecyclerView recyclerView;

    private TrackAdapter customAdapter;

    private ProgressBar progressBar;

    private Handler handler = new Handler();

    public int progressStatus = 0;

    private int pStatusIncrement = 100 / PhotoInfoHelper.photos.size();

    private Thread progressThread;

    private AtomicBoolean running = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_to_text);
        getSupportActionBar().hide();

        progressBar = findViewById(R.id.determinateBar);

        progressThread = new Thread(new Runnable() {
            @Override
            public void run() {

                running.set(true);

                for (int i = 0; i < PhotoInfoHelper.photos.size(); i++) {
                    if (!running.get()) {
                        return;
                    } else {
                        processOnePhoto(i);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(progressStatus);
                            }
                        });
                    }
                }

                if (running.get()) {
                    Intent intent = new Intent(ImageToTextActivity.this, TrackListActivity.class);
                    startActivity(intent);
                }
            }
        });

        progressThread.start();

    } // end onCreate

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        running.set(false);
        try {
            progressThread.join();  // joins with main thread; waits for progressThread to finish
        } catch (Exception e) {};

        PhotoInfoHelper.playlistInfo.cleanPlaylistInfo(); // cleans up after progressThread has stopped

        startActivity(new Intent(ImageToTextActivity.this, ChoosePlaylistTypeActivity.class));
        finish();

    }

    private void processOnePhoto(int i) {

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Toast.makeText(getApplicationContext(), "Unable to identify text. Please try again.", Toast.LENGTH_SHORT).show();
            IntentFilter lsf = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hls = registerReceiver(null, lsf) != null;
            if (hls)
                Toast.makeText(this, "low storage", Toast.LENGTH_LONG).show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(PhotoInfoHelper.photos.get(i)).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);

            if (PhotoInfoHelper.playlistSource.equals(PhotoInfoHelper.UserChoiceEnum.AMAZON)) {
                printOCRTextArray(items);
                PhotoInfoHelper.playlistInfo.parseAmazonAndBillboard(items);
            } else if (PhotoInfoHelper.playlistSource.equals(PhotoInfoHelper.UserChoiceEnum.SPOTIFY)) {
                System.out.println("********* printOCRTextArray() *********");
                printOCRTextArray(items);
                PhotoInfoHelper.playlistInfo.parseSpotifyAndApple(items);
                //PhotoInfoHelper.playlistInfo.parseSpotify(items);
            } else if (PhotoInfoHelper.playlistSource.equals(PhotoInfoHelper.UserChoiceEnum.APPLE)) {
                printOCRTextArray(items);
                PhotoInfoHelper.playlistInfo.parseSpotifyAndApple(items);
            } else if (PhotoInfoHelper.playlistSource.equals(PhotoInfoHelper.UserChoiceEnum.BILLBOARD)) {
                System.out.println("********* printOCRTextArray() *********");
                printOCRTextArray(items);
                PhotoInfoHelper.playlistInfo.parseAmazonAndBillboard(items);
            }
        }

        progressStatus += pStatusIncrement; //increments bar by 100 / number of photos
    }

    // prints SparseArray of textblocks generated by OCR.
    public void printOCRTextArray(SparseArray<TextBlock> trackTextBlock) {

        System.out.println("**********TEXTBLOCKS***********");

        for(int i = 0; i < trackTextBlock.size(); i++) {
            int key = trackTextBlock.keyAt(i);
            // get the object by the key.
            TextBlock tb = trackTextBlock.get(key);
            System.out.println(tb.getValue());
            System.out.println("---------------");
        }

        System.out.println("*********************");
    }

}