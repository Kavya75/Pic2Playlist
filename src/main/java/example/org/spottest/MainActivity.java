package example.org.spottest;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.Button;
import android.graphics.Bitmap;

import java.io.InputStream;

import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    static final int PERMISSION_REQUEST = 0;
    int imagesQuantity = 40;
    TextView textTargetUri;
    ImageView targetImage;
    Button importPhotosButton;
    FloatingActionButton helpButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        importPhotosButton = findViewById(R.id.button);
        targetImage = findViewById(R.id.targetimage);
        textTargetUri  = findViewById(R.id.targeturi);
        helpButton = findViewById(R.id.help_button);

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, HelpScreenActivity.class);
                startActivity(intent);
            }
        });

        // Click the IMPORT PHOTOS button
        importPhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    Toast toast = Toast.makeText(getApplicationContext(),"Click on 'import photos'",Toast.LENGTH_LONG);
//                    toast.show();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(intent, 0);
            }
        });
    }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            // TODO Auto-generated method stub
            super.onActivityResult(requestCode, resultCode, data);
            System.out.println("*********** onActivityResult called ************");
            if (requestCode == 0 && resultCode == RESULT_OK){

                ClipData clipData = data.getClipData();
                System.out.println("******** success *********");
                // Uri targetUri = data.getData();

                if(clipData != null) {

                    for(int i = 0; i < clipData.getItemCount(); i++) {

                        Uri targetUri = clipData.getItemAt(i).getUri();

                        try {
                            InputStream is = getContentResolver().openInputStream(targetUri);

                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            PhotoInfoHelper.photos.add(bitmap);
                            System.out.println("***********Multiple photos selected***************");
                            System.out.println(PhotoInfoHelper.photos.size());
                            startChoosePlaylistTypeActivity();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        //System.out.println("targetUri =" + targetUri.toString());
                        //textTargetUri.setText(targetUri.toString());
                    }

                } else {
                    Uri targetUri = data.getData();
                    try {
                        InputStream is = getContentResolver().openInputStream(targetUri);

                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        PhotoInfoHelper.photos.add(bitmap);
                        System.out.println("***********Single photo selected***************");

                        startChoosePlaylistTypeActivity();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

//                Bitmap bitmap;

//                try {
//                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
//                    targetImage.setImageBitmap(bitmap);
//                    PhotoInfoHelper.photos.add(bitmap);
//                    startImageToTextActivity();
//
//                } catch (FileNotFoundException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
            }

            /*File curFile = new File("path-to-file"); // ... This is an image file from my device.
            Bitmap rotatedBitmap;
            try {
                Bitmap bitmap = null;
                ExifInterface exif = new ExifInterface(curFile.getPath());
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int rotationInDegrees = exifToDegrees(rotation);
                Matrix matrix = new Matrix();
                if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}
                    for(int i=0; i< photos.size(); i++) {
                        Bitmap nextButton = photos.get(i);
                        nextButton = Bitmap.createBitmap(bitmap,0,0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    }



            }catch(IOException ex){
                Log.e("tag", "Failed to get Exif data", ex);
            }*/




        }


        private void startChoosePlaylistTypeActivity() {
            // Create intent.
            Intent intent = new Intent(MainActivity.this, ChoosePlaylistTypeActivity.class);

            // Package all information for the next activity
            //Bundle bundle = new Bundle();

            // Package the track list into the next activity
//            bundle.putParcelableArrayList("BITMAPS", photos);
            //intent.putExtras(bundle);
            //System.out.println("*********START ACTIVITY**************");
            startActivity(intent);
        }









}
