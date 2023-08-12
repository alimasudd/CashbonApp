package com.wtm.cashbon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.firebase.client.Firebase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.wtm.cashbon.databinding.ActivityLoginBinding;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.GeneralHelper;
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

public class LoginActivity extends AppCompatActivity {


    String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private SessionManager sessionManager;
    private AlertDialog progressDialog;
    private ActivityLoginBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);
        Firebase.setAndroidContext(this);

        init();
    }

    private void init() {
        sessionManager = new SessionManager(this);
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);

        if (sessionManager.checkLogin()) {
            goTo();
        }
    }

    private void goTo() {

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void postFirebaseToken(final String tokenfirebase, final String token) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_POST_FIREBASE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                SessionHelper.sessionManager(LoginActivity.this).checkResponse(response);
                Log.d("POSTFIREBASE", response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("token", token);
                params.put("firebase_token", tokenfirebase);
                return params;
            }
        };

//        updateAndroidSecurityProvider();

        stringRequest.setTag(LoginActivity.class.getSimpleName());
        VolleyHttp.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

    }

    private void loginProses() {

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                Log.d("responseLogin", response);

                if (SessionHelper.sessionManager(LoginActivity.this).checkResponse(response)) {

                    try {

                        JSONObject jo = new JSONObject(response);
                        boolean result = jo.getBoolean("response");

                        if (result) {

                            final String token = jo.getString("token");
                            Log.d("TOKENN", token);

                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(task -> {
                                        if (!task.isSuccessful()) {
                                            Log.d("tokenfirebase", "getInstanceId failed", task.getException());
                                            return;
                                        }

                                        // Get new FCM registration token
                                        String tokenfirebase = task.getResult();
                                        Log.d("tokenfirebase", tokenfirebase);
                                        postFirebaseToken(tokenfirebase, token);
                                    });

                            SessionHelper.sessionManager(LoginActivity.this).createLoginSession(token);


                            if (!GeneralHelper.hasPermissions(LoginActivity.this, PERMISSIONS_LOCATION)) {
                                ActivityCompat.requestPermissions(LoginActivity.this, PERMISSIONS_LOCATION, AppConf.PERMISSION_ALL);
                            }else{
                                goTo();
                            }

                        } else {
                            GlobalToast.ShowToast(LoginActivity.this, getString(R.string.login_gagal));
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("koneksivolley", e.toString());
                        GlobalToast.ShowToast(LoginActivity.this, getString(R.string.gagal_server));

                    }

                    progressDialog.dismiss();

                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressDialog.dismiss();

                SessionHelper.sessionManager(LoginActivity.this).checkError(error);
                Log.d("koneksivolley", error.toString());
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Log.d("koneksivolley", jsonError);
                }
            }

        })
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> header = new HashMap<String, String>();
                header.put("email", binding.etEmail.getText().toString().trim());
                header.put("password", binding.etPassword.getText().toString().trim());
                return header;
            }
        };

        stringRequest.setTag(LoginActivity.class.getSimpleName());
        VolleyHttp.getInstance(LoginActivity.this).addToRequestQueue(stringRequest);

    }


    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == AppConf.PERMISSION_ALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goTo();
            }
        }
    }

    @Override
    protected void onDestroy() {
        VolleyHttp.getInstance(LoginActivity.this).cancelPendingRequests(LoginActivity.class.getSimpleName());
        super.onDestroy();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btLogin, R.id.ivShow, R.id.btLupaPassword})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btLogin:
                if (!binding.etEmail.getText().toString().isEmpty() && !binding.etPassword.getText().toString().isEmpty()) {
                    loginProses();
                } else {
                    GlobalToast.ShowToast(LoginActivity.this, getString(R.string.login_kosong));
                }
                break;
            case R.id.ivShow:
                if (binding.txVisible.getText().equals("visible")) {
                    binding.txVisible.setText("gone");
                    binding.ivShow.setImageResource(R.drawable.eyeno);
                    binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.etPassword.setSelection(binding.etPassword.getText().length());
                } else {
                    binding.txVisible.setText("visible");
                    binding.ivShow.setImageResource(R.drawable.eye);
                    binding.etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                break;
            case R.id.btLupaPassword:
                startActivity(new Intent(LoginActivity.this, LupaPasswordActivity.class));
                break;
        }
    }
}