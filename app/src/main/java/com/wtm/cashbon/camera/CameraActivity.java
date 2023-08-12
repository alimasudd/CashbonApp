package com.wtm.cashbon.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wtm.cashbon.R;
import com.wtm.cashbon.home.KasbonActivity;
import com.wtm.cashbon.utils.MediaProcess;

import java.io.File;

public class CameraActivity extends AppCompatActivity {

    private static Camera mCamera = null;
    public static boolean safeToTakePicture = false;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    private ImageView capture, switchCamera;
    private Context myContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;
    public static Bitmap bitmap;
    private String tmpFile;

    public static final String FINISH_ALERT = "finish_alert";
    public static String EXTRA_SELECTED_VALUE = "extra_selected_value";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;

        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
//        mCamera.setDisplayOrientation(90);
        setCameraDisplayOrientation(CameraActivity.this, 1, mCamera);
        cameraPreview = (LinearLayout) findViewById(R.id.cPreview);
        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);

        capture = (ImageView) findViewById(R.id.btnCam);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (safeToTakePicture) {
                    mCamera.takePicture(null, null, mPicture);
                    safeToTakePicture = false;
                }
            }
        });

//        switchCamera = (Button) findViewById(R.id.btnSwitch);
//        switchCamera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //get the number of cameras
//                int camerasNumber = Camera.getNumberOfCameras();
//                if (camerasNumber > 1) {
//                    //release the old camera instance
//                    //switch camera, from the front and the back and vice versa
//
//                    releaseCamera();
//                    chooseCamera();
//                } else {
//
//                }
//            }
//        });

        mCamera.startPreview();
        safeToTakePicture = true;

        releaseCamera();

        chooseCamera();

        this.registerReceiver(this.finishAlert, new IntentFilter(FINISH_ALERT));

    }

    BroadcastReceiver finishAlert = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            setintent(tmpFile);
            CameraActivity.this.finish();
        }
    };

    @Override
    public void onDestroy() {

        super.onDestroy();
        this.unregisterReceiver(finishAlert);
    }

    void setintent(String value) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_VALUE, value);
        setResult(KasbonActivity.SELFIE_REQUEST_CODE, resultIntent);
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();

        android.hardware.Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private int findFrontFacingCamera() {

        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
//                cameraFront = true;
                break;
            }
        }
        return cameraId;

    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;

            }

        }
        return cameraId;
    }

    public void onResume() {

        super.onResume();
        if (mCamera == null) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            setCameraDisplayOrientation(CameraActivity.this, 1, mCamera);
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
            Log.d("nu", "null");
        } else {
            Log.d("nu", "no null");
        }

    }

    public void chooseCamera() {
        //if the camera preview is the front
//        if (cameraFront) {
//            int cameraId = findBackFacingCamera();
//            if (cameraId >= 0) {
//                //open the backFacingCamera
//                //set a picture callback
//                //refresh the preview
//
//                mCamera = Camera.open(cameraId);
//                mCamera.setDisplayOrientation(90);
//                mPicture = getPictureCallback();
//                mPreview.refreshCamera(mCamera);
//            }
//        } else {
//            int cameraId = findFrontFacingCamera();
//            if (cameraId >= 0) {
        //open the backFacingCamera
        //set a picture callback
        //refresh the preview
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        setCameraDisplayOrientation(CameraActivity.this, 1, mCamera);
        mPicture = getPictureCallback();
        mPreview.refreshCamera(mCamera);
//            }
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
        chooseCamera();
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                bitmap = RotateBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), -90);


                Log.d("prosesGambar", "bismillah4");


                File newFile = new MediaProcess(CameraActivity.this).generateTimeStampPhotoFile();
                MediaProcess.bitmapToFile(bitmap,newFile.getAbsolutePath(),60);

                Log.d("imagePathSurvace", newFile.getAbsolutePath());

                tmpFile = newFile.getAbsolutePath();

                Log.d("prosesGambar", "bismillah5");

//                Intent moveForResultIntent = new Intent(CameraActivity.this, PictureActivity.class);
//                startActivityForResult(moveForResultIntent, RepeatOrderActivity.SELFIE_REQUEST_CODE);

                startActivity(new Intent(CameraActivity.this, PictureActivity.class));

                Log.d("prosesGambar", "bismillah6");

//                Intent resultIntent = new Intent();
//                setResult(RepeatOrderActivity.SELFIE_REQUEST_CODE, resultIntent);
//                finish();
                safeToTakePicture = true;
            }
        };
        return picture;
    }
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}