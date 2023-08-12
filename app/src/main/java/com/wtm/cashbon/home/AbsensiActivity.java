package com.wtm.cashbon.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.wtm.cashbon.R;
import com.wtm.cashbon.databinding.ActivityAbsensiBinding;
import com.wtm.cashbon.home.absensi.CheckInActivity;
import com.wtm.cashbon.home.absensi.CheckOutActivity;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.DecimalsFormat;
import com.wtm.cashbon.utils.SessionHelper;
import com.wtm.cashbon.utils.SessionManager;
import com.wtm.cashbon.utils.VolleyHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class AbsensiActivity extends AppCompatActivity {


    private Intent extra;

    private SessionManager sessionManager;
    private AlertDialog progressDialog;

    private ActivityAbsensiBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAbsensiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        sessionManager = new SessionManager(this);
        extra = getIntent();
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);

        binding.txName.setText(sessionManager.getNama());
        Glide.with(AbsensiActivity.this).load(sessionManager.getImageProfile()).into(binding.ivPhoto);
        getStatusAbsensi(0);
    }

    @Override
    public void onResume() {
        super.onResume();

        getStatusAbsensi(0);

    }

    private void getStatusAbsensi(int check) {

        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_GET_STATUSABSEN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("responsStatus", response);

                if (SessionHelper.sessionManager(AbsensiActivity.this).checkResponse(response)) {
                    try {

                        JSONObject jo = new JSONObject(response);
                        binding.txStatus.setText("Status : "+jo.getString("status_absen"));

                        if(check == 1){
                            if(jo.getString("status_absen").toLowerCase().equals("masuk")){

                                AlertDialog.Builder builder = new AlertDialog.Builder(AbsensiActivity.this);

                                builder.setTitle("Maaf");
                                builder.setMessage("Anda sudah check in, silahkan check out");

                                builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });


                                AlertDialog alert = builder.create();
                                alert.setCancelable(false);
                                alert.show();
                            }else if(jo.getString("status_absen").toLowerCase().equals("selesai")){

                                AlertDialog.Builder builder = new AlertDialog.Builder(AbsensiActivity.this);

                                builder.setTitle("Maaf");
                                builder.setMessage("Anda sudah selesai absen hari ini");

                                builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });


                                AlertDialog alert = builder.create();
                                alert.setCancelable(false);
                                alert.show();
                            }else{
                                startActivity(new Intent(getApplicationContext(), CheckInActivity.class));
                            }
                        }else if (check == 2){
                            if(jo.getString("status_absen").toLowerCase().equals("belum masuk")){

                                AlertDialog.Builder builder = new AlertDialog.Builder(AbsensiActivity.this);

                                builder.setTitle("Maaf");
                                builder.setMessage("Anda belum check in, silahkan check in terlebih dahulu");

                                builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });


                                AlertDialog alert = builder.create();
                                alert.setCancelable(false);
                                alert.show();
                            }else if(jo.getString("status_absen").toLowerCase().equals("selesai")){

                                AlertDialog.Builder builder = new AlertDialog.Builder(AbsensiActivity.this);

                                builder.setTitle("Maaf");
                                builder.setMessage("Anda sudah selesai absen hari ini");

                                builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });


                                AlertDialog alert = builder.create();
                                alert.setCancelable(false);
                                alert.show();
                            }else{
                                startActivity(new Intent(getApplicationContext(), CheckOutActivity.class));
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                }

                progressDialog.dismiss();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressDialog.dismiss();
                Log.d("koneksivolley", error.toString());
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
                return params;
            }
        };

        VolleyHttp.getInstance(AbsensiActivity.this).addToRequestQueue(stringRequest);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btBack, R.id.relativ_checkin, R.id.relativ_checkout})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btBack:
                finish();
                break;

            case R.id.relativ_checkin:
                getStatusAbsensi(1);
                break;

            case R.id.relativ_checkout:
                getStatusAbsensi(2);
                break;
        }
    }
}