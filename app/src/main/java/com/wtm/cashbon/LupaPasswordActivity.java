package com.wtm.cashbon;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.wtm.cashbon.databinding.ActivityLoginBinding;
import com.wtm.cashbon.databinding.ActivityLupaPasswordBinding;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class LupaPasswordActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private AlertDialog progressDialog;
    private ActivityLupaPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLupaPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        sessionManager = new SessionManager(this);
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);

        binding.btVerifikasi.setEnabled(false);
        binding.etNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (binding.etNumber.getText().toString().length() == 4) {
                    binding.btVerifikasi.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button));
                    binding.btVerifikasi.setEnabled(true);
                } else {
                    binding.btVerifikasi.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_gray));
                    binding.btVerifikasi.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void submitProses() {

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_FORGOT_PASSWORD_NEW, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("logpassword", response);

//                if (SessionHelper.sessionManager(LupaPasswordActivity.this).checkResponse(response)) {

                try {

                    JSONObject jo = new JSONObject(response);
                    boolean result = jo.getBoolean("response");
                    String msgg = jo.getString("message");

                    if (result) {
                        binding.lyVerifikasi.setVisibility(View.VISIBLE);
                        binding.lyTelp.setVisibility(View.GONE);
                    } else {
                        binding.txError1.setVisibility(View.VISIBLE);
                        binding.txError1.setText(msgg);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    GlobalToast.ShowToast(LupaPasswordActivity.this, getString(R.string.gagal_server));

                }

                progressDialog.dismiss();

//                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressDialog.dismiss();

                SessionHelper.sessionManager(LupaPasswordActivity.this).checkError(error);
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
                params.put("email", binding.etEmail.getText().toString().trim());
                return params;
            }
        };

        stringRequest.setTag(LupaPasswordActivity.class.getSimpleName());
        VolleyHttp.getInstance(LupaPasswordActivity.this).addToRequestQueue(stringRequest);

    }

    private void submitVerif() {

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_FORGOT_PASSWORD_NEW_ACTIV, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("logverifikasi", response);

//                if (SessionHelper.sessionManager(LupaPasswordActivity.this).checkResponse(response)) {

                try {

                    JSONObject jo = new JSONObject(response);
                    boolean result = jo.getBoolean("response");
                    String msgg = jo.getString("message");

                    if (result) {
                        binding.lyGantiPassword.setVisibility(View.VISIBLE);
                        binding.lyVerifikasi.setVisibility(View.GONE);
                    } else {
                        binding.txError2.setVisibility(View.VISIBLE);
                        binding.txError2.setText(msgg);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    GlobalToast.ShowToast(LupaPasswordActivity.this, getString(R.string.gagal_server));

                }

                progressDialog.dismiss();

//                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressDialog.dismiss();

                SessionHelper.sessionManager(LupaPasswordActivity.this).checkError(error);
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
                params.put("email", binding.etEmail.getText().toString().trim());
                params.put("kode", binding.etNumber.getText().toString().trim());
                return params;
            }
        };

        stringRequest.setTag(LupaPasswordActivity.class.getSimpleName());
        VolleyHttp.getInstance(LupaPasswordActivity.this).addToRequestQueue(stringRequest);

    }

    private void submitPassword() {

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_FORGOT_PASSWORD_NEW_PASSWORD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("loggantipassword", response);

//                if (SessionHelper.sessionManager(LupaPasswordActivity.this).checkResponse(response)) {

                try {

                    JSONObject jo = new JSONObject(response);
                    boolean result = jo.getBoolean("response");
                    String msgg = jo.getString("message");

                    if (result) {
                        finish();
                        GlobalToast.ShowToast(getApplicationContext(), "Password berhasil diganti");
                    } else {
                        binding.txError3.setVisibility(View.VISIBLE);
                        binding.txError3.setText(msgg);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    GlobalToast.ShowToast(LupaPasswordActivity.this, getString(R.string.gagal_server));

                }

                progressDialog.dismiss();

//                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressDialog.dismiss();

                SessionHelper.sessionManager(LupaPasswordActivity.this).checkError(error);
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
                params.put("email", binding.etEmail.getText().toString().trim());
                params.put("password", binding.etPassword.getText().toString().trim());
                return params;
            }
        };

        stringRequest.setTag(LupaPasswordActivity.class.getSimpleName());
        VolleyHttp.getInstance(LupaPasswordActivity.this).addToRequestQueue(stringRequest);

    }

    @OnClick({R.id.btBack, R.id.btTelp, R.id.btVerifikasi, R.id.btPassword})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btBack:
                finish();
                break;

            case R.id.btTelp:
                if (!GeneralHelper.isEmailValid(binding.etEmail.getText().toString().trim())) {
                    binding.etEmail.setError("Email tidak valid");
                    binding.etEmail.requestFocus();
                } else {
                    submitProses();
                }
                break;

            case R.id.btVerifikasi:
                submitVerif();
                break;

            case R.id.btPassword:
                if (!isValidPassword(binding.etPassword.getText().toString().trim())) {
                    binding.txError3.setVisibility(View.VISIBLE);
                    binding.txError3.setText(getString(R.string.kombinasi_password));
                }else if(!binding.etPassword.getText().toString().trim().equals(binding.etKonfirmPassword.getText().toString().trim())){
                    binding.txError3.setVisibility(View.VISIBLE);
                    binding.txError3.setText("Password dan konfirmasi password harus sama");
                }else{
                    submitPassword();
                }
                break;
        }
    }

    public boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;

//        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z]).{8,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

}