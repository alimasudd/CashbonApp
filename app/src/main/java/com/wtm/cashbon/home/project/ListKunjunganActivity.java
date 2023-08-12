package com.wtm.cashbon.home.project;

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
import com.wtm.cashbon.adapter.ListKunjunganAdapter;
import com.wtm.cashbon.R;
import com.wtm.cashbon.databinding.ActivityListKunjunganBinding;
import com.wtm.cashbon.model.KunjunganModel;
import com.wtm.cashbon.utils.AppConf;
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

public class ListKunjunganActivity extends AppCompatActivity {

    ListKunjunganAdapter adapter;
    private SessionManager sessionManager;
    private AlertDialog progressDialog;
    private ArrayList<KunjunganModel> dataSet;

    private ActivityListKunjunganBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListKunjunganBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        init();
    }

    private void init(){

        sessionManager = new SessionManager(this);
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);

        dataSet = new ArrayList<>();

        getKunjunganList();


    }

    @Override
    public void onResume() {
        super.onResume();

        getKunjunganList();

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

    private void getKunjunganList() {

        progressDialog.show();
        binding.lyNoConnection.setVisibility(View.GONE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_GET_KUNJUNGAN, new Response.Listener<String>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(String response) {


                if (SessionHelper.sessionManager(ListKunjunganActivity.this).checkResponse(response)) {

                    try {
                        Log.d("responsKunjungan", response);

                        if (response.equals("null")) {
                            binding.rvData.setVisibility(View.GONE);
                            binding.lyNoData.setVisibility(View.VISIBLE);
                        } else {
                            binding.rvData.setVisibility(View.VISIBLE);
                            binding.lyNoData.setVisibility(View.GONE);
                        }

                        dataSet.clear();

                        JSONArray js = new JSONArray(response);

                        for (int i = 0; i < js.length(); i++) {
                            JSONObject jos = js.getJSONObject(i);

                            String id = jos.getString("id_kunjungan");
                            String idStatus = jos.getString("id_status_kunjungan");
                            String status = jos.getString("status_kunjungan");
                            String keterangan = jos.getString("keterangan");
                            String tanggalMulai = jos.getString("tanggal_mulai");
                            String tanggalAkhir = jos.getString("tanggal_akhir");
                            String lokasiKunjungan = jos.getString("lokasi_kunjungan");

                            KunjunganModel dataKunjunganModel = new KunjunganModel();
                            dataKunjunganModel.setId(id);
                            dataKunjunganModel.setStatus_id(idStatus);
                            dataKunjunganModel.setJudul(lokasiKunjungan);
                            dataKunjunganModel.setKeterangan(keterangan);
                            dataKunjunganModel.setTanggalAwal(formatDate(tanggalMulai));
                            dataKunjunganModel.setTanggalAkhir(formatDate(tanggalAkhir));
                            dataKunjunganModel.setStatus(status);
                            dataSet.add(dataKunjunganModel);
                            Log.d("responsjson", status);
                            Log.d("looprespons", String.valueOf(i));


                        }
                        LinearLayoutManager llm = new LinearLayoutManager(ListKunjunganActivity.this);
                        llm.setOrientation(LinearLayoutManager.VERTICAL);
                        binding.rvData.setLayoutManager(llm);
                        adapter = new ListKunjunganAdapter(dataSet, ListKunjunganActivity.this);
                        binding.rvData.setAdapter(adapter);


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

//                checkError(error);

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

        stringRequest.setTag(ListKunjunganActivity.class.getSimpleName());
        VolleyHttp.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        Log.d("looprespons", "loop");

    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btBack, R.id.btRefresh})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btBack:
                finish();
                break;

            case R.id.btRefresh:
                getKunjunganList();
                break;
        }
    }
}