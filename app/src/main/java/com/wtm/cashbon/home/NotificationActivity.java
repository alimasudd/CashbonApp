package com.wtm.cashbon.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import com.wtm.cashbon.R;
import com.wtm.cashbon.adapter.NotificationAdapter;
import com.wtm.cashbon.databinding.ActivityFormCutiBinding;
import com.wtm.cashbon.databinding.ActivityNotificationBinding;
import com.wtm.cashbon.home.absensi.CheckOutActivity;
import com.wtm.cashbon.model.NotifModel;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.SessionHelper;
import com.wtm.cashbon.utils.SessionManager;
import com.wtm.cashbon.utils.VolleyHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class NotificationActivity extends AppCompatActivity {


    private Intent extra;

    NotificationAdapter adapter;
    private ArrayList<NotifModel> dataSet;

    private SessionManager sessionManager;
    private AlertDialog progressDialog;

    private ActivityNotificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        sessionManager = new SessionManager(this);
        extra = getIntent();
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);

        init();
    }

    private void init(){

        dataSet = new ArrayList<>();
        getNotif(1);

    }

    private void getNotif(int tipe) {

        progressDialog.show();
        binding.lyNoConnection.setVisibility(View.GONE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_GET_NOTIF, new Response.Listener<String>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(String response) {


                if (SessionHelper.sessionManager(NotificationActivity.this).checkResponse(response)) {

                    try {
                        Log.d("responsjsonnotif", response);

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

                            String id = jos.getString("notif_id");
                            String judul = jos.getString("judul_notif");
                            String tanggal = jos.getString("tanggal");
                            String isRead = jos.getString("status_notif");
                            String isi = jos.getString("isi_notif");

                            NotifModel notifModel = new NotifModel();
                            notifModel.setId(id);
                            notifModel.setJudul(judul);
                            notifModel.setTanggal(tanggal);
                            notifModel.setIsi(isi);
                            notifModel.setIs_read(isRead);
                            dataSet.add(notifModel);
                            Log.d("responsjson", judul);
                            Log.d("loopnotif", String.valueOf(i));

                        }
                        LinearLayoutManager llm = new LinearLayoutManager(NotificationActivity.this);
                        llm.setOrientation(LinearLayoutManager.VERTICAL);
                        binding.rvData.setLayoutManager(llm);
                        adapter = new NotificationAdapter(dataSet, NotificationActivity.this);
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

                SessionHelper.sessionManager(NotificationActivity.this).checkError(error);

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
                params.put("jenis_notif", String.valueOf(tipe));
                return params;
            }
        };

        stringRequest.setTag(NotificationActivity.class.getSimpleName());
        VolleyHttp.getInstance(NotificationActivity.this).addToRequestQueue(stringRequest);
        Log.d("loopnotif", "loop");

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