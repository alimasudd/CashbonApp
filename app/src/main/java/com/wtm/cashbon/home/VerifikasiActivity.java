package com.wtm.cashbon.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.wtm.cashbon.R;
import com.wtm.cashbon.adapter.ListStatusKasbonAdapter;
import com.wtm.cashbon.databinding.ActivityBatalBinding;
import com.wtm.cashbon.model.StatusKasbonModel;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.DecimalsFormat;
import com.wtm.cashbon.utils.SessionHelper;
import com.wtm.cashbon.utils.SessionManager;
import com.wtm.cashbon.utils.VolleyHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class VerifikasiActivity extends AppCompatActivity {

    ListStatusKasbonAdapter adapterStatus;
    private SessionManager sessionManager;
    private AlertDialog progressDialog;
    private ArrayList<StatusKasbonModel> dataStatus;

    private ActivityBatalBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBatalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        sessionManager = new SessionManager(this);
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);

        dataStatus = new ArrayList<>();
        adapterStatus = new ListStatusKasbonAdapter(dataStatus, this);

        getKasbonStatus();
    }

    public String formatDate(String format) {
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat targetFormat = new SimpleDateFormat("dd MMMM yyyy");
        Date date = null;
        try {
            date = originalFormat.parse(format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);
        return formattedDate;
    }

    @Override
    public void onResume() {
        super.onResume();

        getKasbonStatus();
    }

    private void getKasbonStatus() {

        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_GET_CASHBON_STATUS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("responsKasbon", response);

                if (SessionHelper.sessionManager(VerifikasiActivity.this).checkResponse(response)) {

                    try {

                        dataStatus.clear();

                        JSONArray js = new JSONArray(response);

                        for (int i = 0; i < js.length(); i++) {
                            JSONObject jos = js.getJSONObject(i);

                            String status = jos.getString("status_kasbon");
                            String tanggal = "Tanggal Pengajuan : " + formatDate(jos.getString("kasbon_tanggal_pengajuan"));
                            String nominal = jos.getString("kasbon_nominal");
                            String noPinjaman = "No Pengajuan : " + jos.getString("kasbon_no_pinjaman");
                            String idPengajuan = jos.getString("pjww_id_pengajuan");
                            String idKlien = jos.getString("pjww_id_klien");
                            String isSigned = jos.getString("pjww_is_signed");
                            String signStatus = jos.getString("pjww_status");
                            String signRegistered = jos.getString("is_digisign_registered");
                            sessionManager.setIDKlien(idKlien);

                            StatusKasbonModel dataKasbonModel = new StatusKasbonModel();
                            dataKasbonModel.setNomer(i);
                            dataKasbonModel.setNoPinjaman(noPinjaman);
                            dataKasbonModel.setTanggal(tanggal);
                            dataKasbonModel.setSignRegistered(signRegistered);
                            dataKasbonModel.setStatus(status);
                            dataKasbonModel.setNominal(DecimalsFormat.priceWithoutDecimal(nominal));
                            dataKasbonModel.setIdPengajuan(idPengajuan);
                            dataKasbonModel.setIdKlien(idKlien);
                            dataKasbonModel.setIsSigned(isSigned);
                            dataKasbonModel.setSignStatus(signStatus);
                            dataStatus.add(dataKasbonModel);
                            Log.d("responsjson", status);
                            Log.d("looprespons", String.valueOf(i));


                        }
                        LinearLayoutManager llm = new LinearLayoutManager(VerifikasiActivity.this);
                        llm.setOrientation(LinearLayoutManager.VERTICAL);
                        binding.rvStatus.setLayoutManager(llm);
                        binding.rvStatus.setAdapter(adapterStatus);


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
//                params.put("jenis", "3");
                return params;
            }
        };

        VolleyHttp.getInstance(VerifikasiActivity.this).addToRequestQueue(stringRequest);
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