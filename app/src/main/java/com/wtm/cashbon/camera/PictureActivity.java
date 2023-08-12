package com.wtm.cashbon.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.wtm.cashbon.R;

public class PictureActivity extends AppCompatActivity {


    private ImageView btnCancel, btnOke;
    private FaceOverlayView imageView;
    private TextView txtFaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        imageView = findViewById(R.id.img);
        btnCancel = findViewById(R.id.btnCancel);
        btnOke = findViewById(R.id.btnOke);
        txtFaces= findViewById(R.id.txtFaces);

//        imageView.setRotation((float) -90.0);

//        imageView.setImageBitmap(CameraActivity.bitmap);
        setBitmapToImageview(getResizedBitmap(CameraActivity.bitmap, 300));
        Log.d("prosesGambar", "bismillah2");

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void blinkMultiplefaces(TextView txtFaces, String text){
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(50); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        txtFaces.startAnimation(anim);
        txtFaces.setTextColor(Color.parseColor("#FF0000"));
        txtFaces.setText(text);
    }

    private void setBitmapToImageview(Bitmap data){
//        Bitmap photo = (Bitmap) data.getExtras().get("data");
        int mDetectedFaces=imageView.setBitmap(data);
        Log.d("prosesGambar", "bismillah1");

        if(mDetectedFaces==0){
            blinkMultiplefaces(txtFaces,"Error : Wajah tidak terdeteksi.");
        }
        else if(mDetectedFaces==1){
            txtFaces.clearAnimation();
            txtFaces.setTextColor(Color.parseColor("#149414"));
            txtFaces.setText("Wajah terdeteksi");
            Log.d("prosesGambar", "bismillah3");
            btnOke.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(CameraActivity.FINISH_ALERT);
                    sendBroadcast(i);
                    finish();
                }
            });
        }
        else if(mDetectedFaces>1){
            blinkMultiplefaces(txtFaces,"Error : Terdeteksi lebih dari satu wajah.");
        }

    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}