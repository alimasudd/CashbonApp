package com.wtm.cashbon.home.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.wtm.cashbon.R;
import com.wtm.cashbon.databinding.ActivityFormCutiBinding;
import com.wtm.cashbon.databinding.ActivityKunjunganBinding;
import com.wtm.cashbon.databinding.ActivityTugasBinding;
import com.wtm.cashbon.home.FormCutiActivity;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.GeneralHelper;
import com.wtm.cashbon.utils.GlobalToast;
import com.wtm.cashbon.utils.MediaProcess;
import com.wtm.cashbon.utils.SessionHelper;
import com.wtm.cashbon.utils.SessionManager;
import com.wtm.cashbon.utils.VolleyMultipartHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class KunjunganActivity extends AppCompatActivity {


    private Intent extra;

    private SessionManager sessionManager;
    private AlertDialog progressDialog;

    private ActivityKunjunganBinding binding;

    private int CAMERA_REQUEST_CODE = 1101;
    private int PERMISSION_ALL = 1105;
    private String[] PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    private HashMap<String, String> param = new HashMap<>();
    private File file, tmpFile;
    private ImageView imgCapture;
    private boolean isFoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityKunjunganBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        sessionManager = new SessionManager(this);
        extra = getIntent();
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);

        init();
    }

    private void init(){

        binding.txNamaTugas.setText(extra.getStringExtra("judul"));
        binding.txTanggalAwal.setText(extra.getStringExtra("tanggal_awal"));
        binding.txTanggalAkhir.setText(extra.getStringExtra("tanggal_akhir"));
        binding.txStatusKunjungan.setText(extra.getStringExtra("status"));

        if(!extra.getStringExtra("keterangan").trim().equals("")){
            binding.txDetailKunjungan.setText(extra.getStringExtra("keterangan"));
        }

        Log.d("TugasExtra", extra.getStringExtra("status"));

        String[] status = getResources().getStringArray(R.array.pilihan_status);
        ArrayAdapter<String> adapterS = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, R.id.text1, status);
        binding.spStatus.setAdapter(adapterS);

        binding.spStatus.setSelection(Integer.parseInt(extra.getStringExtra("idStatus")));

    }

    private class ImageProcessing extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap bitmap = null;
            try {

                bitmap = MediaStore.Images.Media.getBitmap(KunjunganActivity.this.getContentResolver(), new MediaProcess(KunjunganActivity.this).generateTimeStampPhotoUri(tmpFile));
                bitmap = MediaProcess.scaledBitmapV2(tmpFile.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {

                if (imgCapture == binding.ivFotoKunjungan) {
                    binding.tvFotoKunjungan.setVisibility(View.GONE);
                    file = tmpFile;
                    isFoto = true;
                }

                Glide.with(KunjunganActivity.this).load(tmpFile.getAbsolutePath()).into(imgCapture);

            }
            progressDialog.dismiss();
        }

    }

    private void captureImage() {

        tmpFile = new MediaProcess(KunjunganActivity.this).generateTimeStampPhotoFile();
        Uri mHighQualityImageUri = new MediaProcess(KunjunganActivity.this).generateTimeStampPhotoUri(tmpFile);
        Log.d("koneksivolleyuri", mHighQualityImageUri.toString());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mHighQualityImageUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            ImageProcessing processing = new ImageProcessing();
            processing.execute();
        }
    }

    private void postKunjungan() {
        progressDialog.show();

        //our custom volley request
        VolleyMultipartHttp volleyMultipartRequest = new VolleyMultipartHttp(Request.Method.POST, AppConf.URL_POST_KUNJUNGAN,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse nResponse) {
                        String response = new String(nResponse.data);

                        Log.d("responKunjungan", response);

                        try {

                            JSONObject jo = new JSONObject(response);
                            if (jo.has("response")) {
                                boolean result = jo.getBoolean("response");
                                if (result) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(KunjunganActivity.this);

                                    builder.setTitle("Terima Kasih");
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
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();

                        }


                        progressDialog.dismiss();
                    }


                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        SessionHelper.sessionManager(KunjunganActivity.this).checkError(error);

                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            Log.d("koneksivolley", jsonError);
                        }

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + "");

                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = param;

                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, VolleyMultipartHttp.DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                if (isFoto) {
                    params.put("foto_kunjungan", new DataPart(System.currentTimeMillis() + "_" + UUID.randomUUID() + ".jpg", MediaProcess.getFileToArray(file.getAbsolutePath())));
                }

                return params;
            }
        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                20 * 1000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //adding the request to volley
//        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(120000, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        volleyMultipartRequest.setTag(KunjunganActivity.class.getSimpleName());
        Volley.newRequestQueue(KunjunganActivity.this).add(volleyMultipartRequest);
    }


    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btBack, R.id.btSimpan, R.id.ivFotoKunjungan})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btBack:
                finish();
                break;

            case R.id.ivFotoKunjungan:
                imgCapture = binding.ivFotoKunjungan;
                tmpFile = file;
                if (!GeneralHelper.hasPermissions(KunjunganActivity.this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(KunjunganActivity.this, PERMISSIONS, PERMISSION_ALL);
                } else {
                    captureImage();
                }
                break;

            case R.id.btSimpan:
                if (binding.spStatus.getSelectedItemPosition() == 0) {
                    GlobalToast.ShowToast(getApplicationContext(), getString(R.string.warning_belum_lengkap));
                } else {
                    param = new HashMap<>();
                    param.put("token", sessionManager.getToken());
                    param.put("id_kunjungan", extra.getStringExtra("id"));
                    param.put("status_kunjungan", String.valueOf(binding.spStatus.getSelectedItemPosition()));
                    if (isFoto) {
                        param.put("foto_kunjungan", file.getAbsolutePath());
                    }
                    postKunjungan();
//                    Log.d("koneksivolley", file.getAbsolutePath());
//                    /data/user/0/com.wtm.cashbon/files/assets_file/1615951670697.jpg
                }
                break;
        }
    }
}