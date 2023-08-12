package com.wtm.cashbon;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.wtm.cashbon.HomeFragment;
import com.wtm.cashbon.R;
import com.wtm.cashbon.databinding.FragmentHomeBinding;
import com.wtm.cashbon.databinding.FragmentSettingBinding;
import com.wtm.cashbon.home.FormCutiActivity;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.DateTools;
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
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class SettingFragment extends Fragment {

    Unbinder unbinder;

    private SessionManager sessionManager;
    private AlertDialog progressDialog;
    private FragmentActivity mActivity;

    private FragmentSettingBinding binding;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        unbinder = ButterKnife.bind(this, view);
        mActivity = getActivity();

        sessionManager = new SessionManager(mActivity);
        progressDialog = new SpotsDialog(mActivity, R.style.Custom);
        progressDialog.setCancelable(false);

        return view;
    }

    private void postPassword() {

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_POST_GANTI_PASSWORD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POSTCUTI", response);

                try {

                    JSONObject jo = new JSONObject(response);
                    boolean r = jo.getBoolean("response");
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    if (r) {
                        builder.setTitle("Terima Kasih");
                        builder.setMessage(jo.getString("message"));

                        builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                                sessionManager.setDone(1);
                                Intent intent = new Intent(getString(R.string.cashbon));
                                intent.putExtra(getString(R.string.fragment), HomeFragment.class.getSimpleName());
                                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);

                            }
                        });
                    }else{
                        builder.setTitle("Maaf");
                        builder.setMessage(jo.getString("message"));

                        builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    }


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
                SessionHelper.sessionManager(mActivity).checkError(error);

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
                params.put("password_old", binding.etPasswordLama.getText().toString().trim());
                params.put("password_new", binding.etPasswordBaru.getText().toString().trim());
                return params;
            }
        };

        VolleyHttp.getInstance(mActivity).addToRequestQueue(stringRequest);

    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btSimpan, R.id.btLupaPassword})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btSimpan:
                String passwordBaru = binding.etPasswordBaru.getText().toString().trim();
                String passwordKonfirm = binding.etKonfirmPasswordBaru.getText().toString().trim();
                if (!isValidPassword(passwordBaru)) {
                    Toast.makeText(mActivity, getString(R.string.kombinasi_password), Toast.LENGTH_LONG).show();
                }else if(!passwordBaru.equals(passwordKonfirm)){
                    Toast.makeText(mActivity, getString(R.string.konfirm_password), Toast.LENGTH_LONG).show();
                } else {
                    postPassword();
                }
                break;

            case R.id.btLupaPassword:
                startActivity(new Intent(mActivity, LupaPasswordActivity.class));
                break;
        }
    }

    public boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z]).{8,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }
}