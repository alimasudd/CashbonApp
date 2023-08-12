package com.wtm.cashbon.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.wtm.cashbon.HomeFragment;
import com.wtm.cashbon.R;
import com.wtm.cashbon.databinding.FragmentProfileBinding;
import com.wtm.cashbon.databinding.FragmentSettingBinding;
import com.wtm.cashbon.model.SearchModel;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.DecimalsFormat;
import com.wtm.cashbon.utils.GlobalToast;
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
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class ProfileFragment extends Fragment {

    Unbinder unbinder;

    private SessionManager sessionManager;
    private AlertDialog progressDialog;
    private FragmentActivity mActivity;

    private ArrayList<SearchModel> dataBank;
    private String idBank;

    private FragmentProfileBinding binding;
    private String urlNIK;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        unbinder = ButterKnife.bind(this, view);
        mActivity = getActivity();

        init();

        return view;
    }

    private void init() {
        sessionManager = new SessionManager(mActivity);
        progressDialog = new SpotsDialog(mActivity, R.style.Custom);
        progressDialog.setCancelable(false);

        dataBank = new ArrayList<>();
        binding.spPilihBank.setTitle(getString(R.string.pilih_bank));
        binding.spPilihBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SearchModel selected = (SearchModel) adapterView.getAdapter().getItem(i);
                idBank = selected.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Log.d("responsprofile", sessionManager.getImageProfile());

//        if(sessionManager.getEditBank()){
//            binding.etBank.setText(sessionManager.getBank());
//        }else{
//            binding.etBank.setVisibility(View.GONE);
//            binding.linearBank.setVisibility(View.VISIBLE);
//        }

        getProfile();

    }

    private void getBank() {
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_BANK, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("response4", response);
                try {

                    dataBank.clear();

                    JSONArray js = new JSONArray(response);

                    for (int i = 0; i < js.length(); i++) {
                        JSONObject jos = js.getJSONObject(i);

                        String id = jos.getString("bank_id");
                        String bank_label = jos.getString("bank_name");
//                        dataBank.add(bank_label);

                        SearchModel model = new SearchModel();
                        model.setId(id);
                        model.setNama(bank_label);
                        dataBank.add(model);

                        if (i == 0) {
                            idBank = id;
                        }

                    }

                    ArrayAdapter<SearchModel> adapter = new ArrayAdapter<SearchModel>(getActivity(), R.layout.list_item, R.id.text1, dataBank);
                    binding.spPilihBank.setAdapter(adapter);

                    binding.spPilihBank.setSelection(sessionManager.getBankID());


                } catch (JSONException e) {
                    e.printStackTrace();

//                        GlobalToast.ShowToast(mActivity, getString(R.string.gagal_server));

                }

                progressDialog.dismiss();

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressDialog.dismiss();
            }

        });

        stringRequest.setTag(ProfileFragment.class.getSimpleName());
        VolleyHttp.getInstance(mActivity).addToRequestQueue(stringRequest);

    }

    private void postProfile() {

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_POST_PROFILE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POSTPROFILE", response);

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
                params.put("nama_lengkap", binding.etName.getText().toString().trim());
                params.put("email", binding.etEmail.getText().toString().trim());
                params.put("no_rekening", binding.etRekening.getText().toString().trim());
                params.put("bank_atas_nama", binding.etNamaRekening.getText().toString().trim());
                params.put("nama_bank", binding.spPilihBank.getSelectedItem().toString().trim());
                params.put("ref_bank_id", String.valueOf(binding.spPilihBank.getSelectedItemPosition()));
                return params;
            }
        };

        VolleyHttp.getInstance(mActivity).addToRequestQueue(stringRequest);

    }

    private void getProfile() {
        progressDialog.show();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_GET_PROFILE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("responsProfile", response);

                if (SessionHelper.sessionManager(mActivity).checkResponse(response)) {
                    try {

                        JSONObject jo = new JSONObject(response);
                        binding.txName.setText(""+jo.getString("nama_lengkap"));
                        Glide.with(mActivity).load(jo.getString("image")).into(binding.ivPhoto);
                        binding.etName.setText(jo.getString("nama_lengkap"));
                        binding.etNIK.setText(jo.getString("nik"));
                        binding.etTanggalLahir.setText(jo.getString("tgl_lahir"));
                        binding.etEmail.setText(jo.getString("email"));
                        binding.etNomorTelp.setText(jo.getString("no_handphone"));
                        binding.etBank.setText(jo.getString("nama_bank"));
                        binding.etRekening.setText(jo.getString("no_rekening"));
                        binding.etNamaRekening.setText(jo.getString("bank_atas_nama"));
                        binding.etPerusahaan.setText(jo.getString("nama_perusahaan"));
                        binding.etTelpKantor.setText(jo.getString("no_telp_perusahaan"));
                        binding.etNIP.setText(jo.getString("nip"));
                        binding.etHRD.setText(jo.getString("nama_hrd"));
                        binding.etGajiBulanan.setText(DecimalsFormat.priceWithoutDecimal(jo.getString("total_gaji")));
                        binding.etTanggalGaji.setText(jo.getString("tgl_gajian"));
                        urlNIK = jo.getString("image_ktp");
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
                Log.d("koneksivolleyProfile", error.toString());
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Log.d("koneksivolleyProfile", jsonError);
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

//        stringRequest.setTag(ChatActivity.class.getSimpleName());
        VolleyHttp.getInstance(mActivity).addToRequestQueue(stringRequest);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btSimpan,R.id.etViewNIK})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btSimpan:
//                String nama = binding.etName.getText().toString().trim();
//                String email = binding.etEmail.getText().toString().trim();
//                String rekening = binding.etRekening.getText().toString().trim();
//                String rekeningNama = binding.etNamaRekening.getText().toString().trim();
//                if (nama.equals("")) {
////                    Snackbar snackbar = Snackbar
////                            .make(binding.btSimpan, getString(R.string.warning_belum_lengkap), Snackbar.LENGTH_LONG);
////                    snackbar.show();
//                    GlobalToast.ShowToast(mActivity, getString(R.string.warning_belum_lengkap));
//                } else if (email.equals("")) {
//                    GlobalToast.ShowToast(mActivity, getString(R.string.warning_belum_lengkap));
//                } else if (rekening.equals("")) {
//                    GlobalToast.ShowToast(mActivity, getString(R.string.warning_belum_lengkap));
//                } else if (rekeningNama.equals("")) {
//                    GlobalToast.ShowToast(mActivity, getString(R.string.warning_belum_lengkap));
//                } else if (binding.spPilihBank.getSelectedItemPosition() == 0) {
//                    GlobalToast.ShowToast(mActivity, getString(R.string.warning_belum_lengkap));
//                } else {
//                    postProfile();
//                }


                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle("Perhatian");
                builder.setMessage("Silahkan Hubungi HRD perusahaan anda untuk melakukan Update!");

                builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.setCancelable(false);
                alert.show();


                Log.d("bankPosition", String.valueOf(binding.spPilihBank.getSelectedItemPosition()));
                break;

            case R.id.etViewNIK:
                Uri uri = Uri.parse(urlNIK); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
        }
    }
}