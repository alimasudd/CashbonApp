package com.wtm.cashbon.home;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.wtm.cashbon.R;
import com.wtm.cashbon.databinding.ActivityAbsensiBinding;
import com.wtm.cashbon.databinding.ActivityFormCutiBinding;
import com.wtm.cashbon.home.absensi.CheckInActivity;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.DateTools;
import com.wtm.cashbon.utils.GlobalToast;
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

public class FormCutiActivity extends AppCompatActivity {


    private Intent extra;

    private SessionManager sessionManager;
    private AlertDialog progressDialog;

    private ActivityFormCutiBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFormCutiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        sessionManager = new SessionManager(this);
        extra = getIntent();
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);


        String[] cuti = getResources().getStringArray(R.array.pilihan_cuti);
        ArrayAdapter<String> adapterPt = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, R.id.text1, cuti);
        binding.spPilihCuti.setAdapter(adapterPt);
    }

    private void postCuti() {

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_POST_CUTI, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POSTCUTI", response);

                try {

                    JSONObject jo = new JSONObject(response);
                    boolean r = jo.getBoolean("response");

//                        Toast.makeText(getApplicationContext(), "Terima Kasih", Toast.LENGTH_LONG).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(FormCutiActivity.this);
                    if (r) {

                        builder.setTitle("Terima Kasih");
                        builder.setMessage(jo.getString("message"));
                    }else{

                        builder.setTitle("Maaf");
                        builder.setMessage(jo.getString("message"));
                    }

                    builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            finish();

                        }
                    });


                    AlertDialog alert = builder.create();
                    alert.setCancelable(false);
                    alert.show();
                } catch (JSONException e) {
                    e.printStackTrace();

                }
                progressDialog.dismiss();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                SessionHelper.sessionManager(FormCutiActivity.this).checkError(error);

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
                params.put("jenis_cuti", binding.spPilihCuti.getSelectedItem().toString().trim());
                params.put("tanggal_awal_cuti", binding.etTanggalMulai.getText().toString().trim());
                params.put("tanggal_akhir_cuti", binding.etTanggalAkhir.getText().toString().trim());
                params.put("alasan_cuti", binding.etALasanCuti.getText().toString().trim());
                return params;
            }
        };

        VolleyHttp.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btBack, R.id.btAjukan, R.id.etTanggalMulai, R.id.etTanggalAkhir})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btBack:
                finish();
                break;

            case R.id.etTanggalMulai:
                DateTools.dateDialog(FormCutiActivity.this, binding.etTanggalMulai);
                break;

            case R.id.etTanggalAkhir:
                DateTools.dateDialog(FormCutiActivity.this, binding.etTanggalAkhir);
                break;

            case R.id.btAjukan:
                if (binding.spPilihCuti.getSelectedItemPosition() == 0) {
                    GlobalToast.ShowToast(getApplicationContext(), getString(R.string.warning_belum_lengkap));
                } else if (binding.etTanggalMulai.getText().toString().trim().equals("")) {
                    GlobalToast.ShowToast(getApplicationContext(), getString(R.string.warning_belum_lengkap));
                } else if (binding.etTanggalAkhir.getText().toString().trim().equals("")) {
                    GlobalToast.ShowToast(getApplicationContext(), getString(R.string.warning_belum_lengkap));
                } else if (binding.etALasanCuti.getText().toString().trim().equals("")) {
                    Snackbar snackbar = Snackbar
                            .make(binding.btAjukan, getString(R.string.warning_belum_lengkap), Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    postCuti();
                }
                break;
        }
    }
}