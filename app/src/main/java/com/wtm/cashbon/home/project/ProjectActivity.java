package com.wtm.cashbon.home.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wtm.cashbon.R;
import com.wtm.cashbon.databinding.ActivityKunjunganBinding;
import com.wtm.cashbon.databinding.ActivityProjectBinding;
import com.wtm.cashbon.home.project.ProjectActivity;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.GeneralHelper;
import com.wtm.cashbon.utils.GlobalToast;
import com.wtm.cashbon.utils.MediaProcess;
import com.wtm.cashbon.utils.SessionHelper;
import com.wtm.cashbon.utils.SessionManager;
import com.wtm.cashbon.utils.VolleyHttp;
import com.wtm.cashbon.utils.VolleyMultipartHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class ProjectActivity extends AppCompatActivity {


    private Intent extra;

    private SessionManager sessionManager;
    private AlertDialog progressDialog;

    private ActivityProjectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        sessionManager = new SessionManager(this);
        extra = getIntent();
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);

        init();
    }

    private void init(){

        binding.txNamaProject.setText(extra.getStringExtra("judul"));
        binding.txTanggalAwal.setText(extra.getStringExtra("tanggal_awal"));
        binding.txTanggalAkhir.setText(extra.getStringExtra("tanggal_akhir"));
        binding.txStatusProject.setText(extra.getStringExtra("status"));

        if(!extra.getStringExtra("keterangan").trim().equals("")){
            binding.txDetailProject.setText(extra.getStringExtra("keterangan"));
        }

        Log.d("TugasExtra", extra.getStringExtra("status"));

        String[] status = getResources().getStringArray(R.array.pilihan_status);
        ArrayAdapter<String> adapterS = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, R.id.text1, status);
        binding.spStatus.setAdapter(adapterS);

        binding.spStatus.setSelection(Integer.parseInt(extra.getStringExtra("idStatus")));

    }

    private void postProject() {

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_POST_PROJECT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POSTPROJECT", response);

                try {

                    JSONObject jo = new JSONObject(response);
                    boolean r = jo.getBoolean("response");
                    if (r) {
//                        Toast.makeText(getApplicationContext(), "Terima Kasih", Toast.LENGTH_LONG).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectActivity.this);

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
                SessionHelper.sessionManager(ProjectActivity.this).checkError(error);

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
                params.put("id_project", extra.getStringExtra("id"));
                params.put("status_project", String.valueOf(binding.spStatus.getSelectedItemPosition()));
                return params;
            }
        };

        VolleyHttp.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btBack, R.id.btSimpan})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btBack:
                finish();
                break;

            case R.id.btSimpan:
                if (binding.spStatus.getSelectedItemPosition() == 0) {
                    GlobalToast.ShowToast(getApplicationContext(), getString(R.string.warning_belum_lengkap));
                } else {
                    postProject();
                }
                break;
        }
    }
}