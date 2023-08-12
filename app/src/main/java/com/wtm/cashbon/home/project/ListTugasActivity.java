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
import com.wtm.cashbon.adapter.ListTugasAdapter;
import com.wtm.cashbon.R;
import com.wtm.cashbon.databinding.ActivityListTugasBinding;
import com.wtm.cashbon.model.TugasModel;
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

public class ListTugasActivity extends AppCompatActivity {

    ListTugasAdapter adapter;
    private SessionManager sessionManager;
    private AlertDialog progressDialog;
    private ArrayList<TugasModel> dataSet;

    private ActivityListTugasBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListTugasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        init();
    }

    private void init(){

        sessionManager = new SessionManager(this);
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);

        dataSet = new ArrayList<>();

        getTugasList();


    }

    @Override
    public void onResume() {
        super.onResume();

        getTugasList();

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

    private void getTugasList() {

        progressDialog.show();
        binding.lyNoConnection.setVisibility(View.GONE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_GET_TUGAS, new Response.Listener<String>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(String response) {


                if (SessionHelper.sessionManager(ListTugasActivity.this).checkResponse(response)) {

                    try {
                        Log.d("responsTugas", response);

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

                            String id = jos.getString("id_tugas");
                            String idStatus = jos.getString("id_status_tugas");
                            String idPrioritas = jos.getString("id_prioritas_tugas");
                            String nama = jos.getString("nama_tugas");
                            String status = jos.getString("status_tugas");
                            String keterangan = jos.getString("keterangan");
                            String tanggalMulai = jos.getString("tanggal_mulai");
                            String tanggalAkhir = jos.getString("tanggal_akhir");
                            String prioritas = jos.getString("prioritas_tugas");
                            String prosentase = jos.getString("prosentase_tugas");

                            TugasModel tugasModel = new TugasModel();
                            tugasModel.setId(id);
                            tugasModel.setStatus_id(idStatus);
                            tugasModel.setPrioritas_id(idPrioritas);
                            tugasModel.setJudul(nama);
                            tugasModel.setKeterangan(keterangan);
                            tugasModel.setTanggalAwal(formatDate(tanggalMulai));
                            tugasModel.setTanggalAkhir(formatDate(tanggalAkhir));
                            tugasModel.setStatus(status);
                            tugasModel.setPrioritas(prioritas);
                            tugasModel.setProsentase(prosentase);
                            dataSet.add(tugasModel);
                            Log.d("responsjson", status);
                            Log.d("looprespons", String.valueOf(i));


                        }
                        LinearLayoutManager llm = new LinearLayoutManager(ListTugasActivity.this);
                        llm.setOrientation(LinearLayoutManager.VERTICAL);
                        binding.rvData.setLayoutManager(llm);
                        adapter = new ListTugasAdapter(dataSet, ListTugasActivity.this);
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

        stringRequest.setTag(ListTugasActivity.class.getSimpleName());
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
                getTugasList();
                break;
        }
    }

}