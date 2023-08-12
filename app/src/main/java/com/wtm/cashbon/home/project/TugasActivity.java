package com.wtm.cashbon.home.project;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.wtm.cashbon.R;
import com.wtm.cashbon.databinding.ActivityFormCutiBinding;
import com.wtm.cashbon.databinding.ActivityTugasBinding;
import com.wtm.cashbon.home.FormCutiActivity;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.DateTools;
import com.wtm.cashbon.utils.GlobalToast;
import com.wtm.cashbon.utils.MediaProcess;
import com.wtm.cashbon.utils.SessionHelper;
import com.wtm.cashbon.utils.SessionManager;
import com.wtm.cashbon.utils.VolleyHttp;
import com.wtm.cashbon.utils.VolleyMultipartHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class TugasActivity extends AppCompatActivity {


    private Intent extra;

    private SessionManager sessionManager;
    private AlertDialog progressDialog;

    private ActivityTugasBinding binding;
    String[] mediaColumns = {MediaStore.Video.Media._ID};
    String realname, mediaPath1;
    File file, tmpFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTugasBinding.inflate(getLayoutInflater());
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
        binding.txStatusTugas.setText(extra.getStringExtra("status"));
        binding.txPersentase.setText("Persentase "+extra.getStringExtra("persentase")+"%");
        binding.seekBar.setProgress(Integer.parseInt(extra.getStringExtra("persentase")));

        if(!extra.getStringExtra("keterangan").trim().equals("")){
            binding.txDetailTugas.setText(extra.getStringExtra("keterangan"));
        }

        Log.d("TugasExtra", extra.getStringExtra("idPrioritas"));

        String[] status = getResources().getStringArray(R.array.pilihan_status);
        ArrayAdapter<String> adapterS = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, R.id.text1, status);
        binding.spStatus.setAdapter(adapterS);

        String[] prioritas = getResources().getStringArray(R.array.pilihan_prioritas);
        ArrayAdapter<String> adapterP = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, R.id.text1, prioritas);
        binding.spPrioritas.setAdapter(adapterP);

        binding.spPrioritas.setSelection(Integer.parseInt(extra.getStringExtra("idPrioritas")));
        binding.spStatus.setSelection(Integer.parseInt(extra.getStringExtra("idStatus")));

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.txPersentase.setText("Persentase "+progress+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void postTugas() {

        progressDialog.show();

        VolleyMultipartHttp volleyMultipartRequest = new VolleyMultipartHttp(Request.Method.POST, AppConf.URL_POST_TUGAS,
                new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse nResponse) {
                String response = new String(nResponse.data);
                Log.d("POSTTUGAS", response);

                try {

                    JSONObject jo = new JSONObject(response);
                    boolean r = jo.getBoolean("response");
                    if (r) {
//                        Toast.makeText(getApplicationContext(), "Terima Kasih", Toast.LENGTH_LONG).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(TugasActivity.this);

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
                } catch (JSONException e) {
                    e.printStackTrace();

                }
                progressDialog.dismiss();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                SessionHelper.sessionManager(TugasActivity.this).checkError(error);

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

                Map<String, String> params = new HashMap<String, String>();
                params.put("token", sessionManager.getToken());
                params.put("id_tugas", extra.getStringExtra("id"));
                params.put("prioritas_tugas", String.valueOf(binding.spPrioritas.getSelectedItemPosition()));
                params.put("status_proses_tugas", String.valueOf(binding.spStatus.getSelectedItemPosition()));
//                params.put("keterangan_tugas", binding.etKeterangan.getText().toString().trim());
                params.put("prosentase_progress_tugas", String.valueOf(binding.seekBar.getProgress()));
//                params.put("file", file.getAbsolutePath());
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
//                if (!isFotoselfie) {
//                    params.put("file", new DataPart(System.currentTimeMillis() + "_" + UUID.randomUUID() + "."+realname, MediaProcess.getFileToArray(file.getAbsolutePath())));
//                }

//                if (fileSurat != null) {
//                    params.put("userfile_skck", new DataPart(System.currentTimeMillis() + "_" + UUID.randomUUID() + ".jpg", MediaProcess.getFileToArray(fileSurat.getAbsolutePath())));
//                }

                return params;
            }
        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                20 * 1000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //adding the request to volley
//        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(120000, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        volleyMultipartRequest.setTag(TugasActivity.class.getSimpleName());
        Volley.newRequestQueue(TugasActivity.this).add(volleyMultipartRequest);

    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Pilih File"),
                    20211);
            setResult(Activity.RESULT_OK);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "Silahkan install File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 20211) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                file = new File(uri.getPath());
                Log.d("koneksivolleyuri1", file.getAbsolutePath());

                tmpFile = new MediaProcess(TugasActivity.this).generateTimeStampPhotoFile();
                Uri mHighQualityImageUri = new MediaProcess(TugasActivity.this).generateTimeStampPhotoUri(tmpFile);

                //file extension
                ContentResolver cR = getContentResolver();
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String type = mime.getExtensionFromMimeType(cR.getType(uri));

                //file name
                String path = new File(data.getData().getPath()).getAbsolutePath();

                if(path != null){
                    uri = data.getData();

                    String filename;
                    Cursor cursor = getContentResolver().query(uri,null,null,null,null);

                    if(cursor == null) filename=uri.getPath();
                    else{
                        cursor.moveToFirst();
                        int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                        filename = cursor.getString(idx);
                        cursor.close();
                    }

                    realname = filename.substring(0,filename.lastIndexOf("."));
                    String extension = filename.substring(filename.lastIndexOf(".")+1);

                    Log.d("koneksivolleyuri", mHighQualityImageUri.toString()+extension);

                    binding.txAttach.setText(realname+"."+extension);
                }
            }
        }
    }


    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btBack, R.id.btSimpan, R.id.ivAttach, R.id.btTambahCatatan})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btBack:
                finish();
                break;

            case R.id.ivAttach:
                showFileChooser();
                break;

            case R.id.btTambahCatatan:
                Intent i = new Intent(TugasActivity.this, CatatanTugasActivity.class);
                i.putExtra("id_tugas", extra.getStringExtra("id"));
                startActivity(i);
                break;

            case R.id.btSimpan:
                if (binding.spStatus.getSelectedItemPosition() == 0) {
                    GlobalToast.ShowToast(getApplicationContext(), getString(R.string.warning_belum_lengkap));
                } else if (binding.spPrioritas.getSelectedItemPosition() == 0) {
                    GlobalToast.ShowToast(getApplicationContext(), getString(R.string.warning_belum_lengkap));
                }
//                else if (binding.etKeterangan.getText().toString().trim().equals("")) {
//                    GlobalToast.ShowToast(getApplicationContext(), getString(R.string.warning_belum_lengkap));
//                }
                else {
                    postTugas();
//                    Log.d("koneksivolley", tmpFile.getAbsolutePath());
                }
                break;
        }
    }
}