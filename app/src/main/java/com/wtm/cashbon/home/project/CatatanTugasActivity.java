package com.wtm.cashbon.home.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.wtm.cashbon.R;
import com.wtm.cashbon.adapter.CatatanAdapter;
import com.wtm.cashbon.adapter.ListProjectAdapter;
import com.wtm.cashbon.databinding.ActivityCatatanTugasBinding;
import com.wtm.cashbon.databinding.ActivityProjectBinding;
import com.wtm.cashbon.model.CatatanModel;
import com.wtm.cashbon.model.ProjectModel;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.GlobalToast;
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

public class CatatanTugasActivity extends AppCompatActivity {


    private Intent extra;

    CatatanAdapter adapter;
    private SessionManager sessionManager;
    private AlertDialog progressDialog;
    private ArrayList<CatatanModel> dataSet;

    private ActivityCatatanTugasBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCatatanTugasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        init();
    }

    private void init(){

        sessionManager = new SessionManager(this);
        extra = getIntent();
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);

        dataSet = new ArrayList<>();

        getCatatanList();

    }

    @Override
    public void onResume() {
        super.onResume();

        getCatatanList();

    }

    public String formatDate(String format) {
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        DateFormat targetFormat = new SimpleDateFormat("dd MMMM yyyy hh:mm:ss");
        Date date = null;
        try {
            date = originalFormat.parse(format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);
        return formattedDate;
    }

    private void getCatatanList() {
        progressDialog.show();
        binding.lyNoData.setVisibility(View.GONE);
        binding.lyNoConnection.setVisibility(View.GONE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_GET_CATATAN, new Response.Listener<String>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(String response) {


                if (SessionHelper.sessionManager(CatatanTugasActivity.this).checkResponse(response)) {

                    try {
                        Log.d("responsProject", response);

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


                            String catatan = jos.getString("catatan");
                            String tanggal = jos.getString("tanggal");
                            String nama = jos.getString("nama");

                            CatatanModel dataModel = new CatatanModel();
                            dataModel.setNama(nama);
                            dataModel.setCatatan(catatan);
                            dataModel.setTanggal(formatDate(tanggal));
                            dataSet.add(dataModel);
                            Log.d("looprespons", String.valueOf(i));


                        }
                        LinearLayoutManager llm = new LinearLayoutManager(CatatanTugasActivity.this);
                        llm.setOrientation(LinearLayoutManager.VERTICAL);
                        binding.rvData.setLayoutManager(llm);
                        adapter = new CatatanAdapter(dataSet, CatatanTugasActivity.this);
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

                checkError(error);

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
                params.put("id_tugas", extra.getStringExtra("id_tugas"));
                return params;
            }
        };

        stringRequest.setTag(CatatanTugasActivity.class.getSimpleName());
        VolleyHttp.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        Log.d("looprespons", "loop");

    }

    private void postCatatan() {

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_POST_CATATAN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POSTCATATAN", response);
                Toast.makeText(CatatanTugasActivity.this, "Catatan ditambahkan", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                binding.etIsiCatatan.setText("");
                getCatatanList();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                SessionHelper.sessionManager(CatatanTugasActivity.this).checkError(error);

                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Log.d("catatanTulis", jsonError);
                }
            }

        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("token", sessionManager.getToken());
                params.put("id_tugas", extra.getStringExtra("id_tugas"));
                params.put("catatan", binding.etIsiCatatan.getText().toString().trim());
                return params;
            }
        };

        VolleyHttp.getInstance(CatatanTugasActivity.this).addToRequestQueue(stringRequest);

    }

    public void checkError(VolleyError error) {

        if (error instanceof TimeoutError) {
            binding.lyNoConnection.setVisibility(View.VISIBLE);
            binding.txNoConnection.setText(getString(R.string.timeout));
        } else if (error instanceof NoConnectionError) {
            binding.lyNoConnection.setVisibility(View.VISIBLE);
            binding.txNoConnection.setText(getString(R.string.no_connection));
        } else {
            binding.lyNoConnection.setVisibility(View.VISIBLE);
            binding.txNoConnection.setText(getString(R.string.gagal_server));
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btBack, R.id.btSimpanCatatan})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btBack:
                finish();
                break;

            case R.id.btSimpanCatatan:
                if(binding.etIsiCatatan.getText().toString().trim().equals("")){
                    GlobalToast.ShowToast(getApplicationContext(), getString(R.string.warning_belum_lengkap));
                }else{
                    postCatatan();
                }
                break;
        }
    }
}