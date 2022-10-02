package example.org.spottest;

        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Matrix;
        import android.graphics.drawable.BitmapDrawable;
        import android.graphics.drawable.Drawable;
        import android.os.Bundle;
        import android.view.Display;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;

        import androidx.appcompat.app.AppCompatActivity;

public class HelpScreenActivity extends AppCompatActivity {

    Button okButton;

    ImageView firstImage;

    ImageView secondImage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_box);

        getSupportActionBar().hide();

        okButton = findViewById(R.id.ok_button);

        ImageView firstImage = findViewById(R.id.firstImage);
//        ImageView secondImage = findViewById(R.id.secondImage);

//        firstImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.original));
//
//        secondImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cropped));

//        firstImage.setImageDrawable(resizeImage(R.drawable.original));
//        secondImage.setImageDrawable(resizeImage(R.drawable.cropped));


        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(HelpScreenActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public Drawable resizeImage(int imageResource) {// R.drawable.large_image
        // Get device dimensions
        Display display = getWindowManager().getDefaultDisplay();
        double deviceWidth = display.getWidth();

        BitmapDrawable bd = (BitmapDrawable) this.getResources().getDrawable(
                imageResource);
        double imageHeight = bd.getBitmap().getHeight();
        double imageWidth = bd.getBitmap().getWidth();

        double ratio = deviceWidth / imageWidth;
        int newImageHeight = (int) (imageHeight * ratio);

        Bitmap bMap = BitmapFactory.decodeResource(getResources(), imageResource);
        Drawable drawable = new BitmapDrawable(this.getResources(),
                getResizedBitmap(bMap, newImageHeight, (int) deviceWidth));

        return drawable;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }

}
