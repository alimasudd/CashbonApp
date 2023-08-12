package com.wtm.cashbon.home.absensi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.common.util.concurrent.ListenableFuture;
import com.wtm.cashbon.R;
import com.wtm.cashbon.home.absensi.qrcode.QRCodeFoundListener;
import com.wtm.cashbon.home.absensi.qrcode.QRCodeImageAnalyzer;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.SessionHelper;
import com.wtm.cashbon.utils.SessionManager;
import com.wtm.cashbon.utils.VolleyHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class CheckOutActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 0;

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private Button qrCodeFoundButton;
    private String qrCode;

    private SessionManager sessionManager;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);
        ButterKnife.bind(this);

        sessionManager = new SessionManager(this);
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);

        previewView = findViewById(R.id.activity_main_previewView);

        qrCodeFoundButton = findViewById(R.id.activity_main_qrCodeFoundButton);
        qrCodeFoundButton.setVisibility(View.INVISIBLE);
        qrCodeFoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), qrCode, Toast.LENGTH_SHORT).show();
                Log.i(CheckOutActivity.class.getSimpleName(), "QR Code Found: " + qrCode);
            }
        });
        Log.d("QRCODE", "CODE "+ qrCode);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        requestCamera();
    }
    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(CheckOutActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
//                        .setTargetResolution(new Size(2000, 2000))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
            @Override
            public void onQRCodeFound(String _qrCode) {
                qrCode = _qrCode;
                Log.d("QRCODE", "CODE "+ qrCode);
                postCheckout(qrCode);

                imageAnalysis.clearAnalyzer();
            }

            @Override
            public void qrCodeNotFound() {
//                qrCodeFoundButton.setVisibility(View.INVISIBLE);
            }
        }));

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }

    private void postCheckout(String qr_code) {

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_POST_CHECKOUT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POSTCHECK", response);
                String title="";

                try {

                    JSONObject jo = new JSONObject(response);
                    boolean r = jo.getBoolean("response");
                    AlertDialog.Builder builder = new AlertDialog.Builder(CheckOutActivity.this);
                    if (r) {
                        title = "Terima kasih";

                        Intent intent = new Intent(CheckOutActivity.this, ViewCheckoutActivity.class);
                        intent.putExtra("jam_kerja", jo.getString("jam_kerja"));
                        intent.putExtra("jam_masuk", jo.getString("jam_masuk"));
                        intent.putExtra("jam_keluar", jo.getString("jam_keluar"));
                        intent.putExtra("jam", jo.getString("total_jam"));
                        intent.putExtra("menit", jo.getString("total_menit"));
                        startActivity(intent);
                        finish();

                    } else {
                        title = "Maaf";

                        builder.setTitle(title);
                        builder.setMessage(jo.getString("message"));

                        builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                finish();

                            }
                        });


                        AlertDialog alert = builder.create();
                        alert.setCancelable(false);
                        alert.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }
                progressDialog.dismiss();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                SessionHelper.sessionManager(CheckOutActivity.this).checkError(error);

                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Log.d("koneksivolley", jsonError);
                }
            }

        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("token", sessionManager.getToken());
                params.put("qrcode_id", qr_code);
                return params;
            }
        };

        VolleyHttp.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

    }


    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btBack})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btBack:
                finish();
                break;
        }
    }
}