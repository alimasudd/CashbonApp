package com.wtm.cashbon.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.wtm.cashbon.HomeFragment;
import com.wtm.cashbon.adapter.ListStatusKasbonAdapter;
import com.wtm.cashbon.camera.CameraActivity;
import com.wtm.cashbon.model.KasbonModel;
import com.wtm.cashbon.R;
import com.wtm.cashbon.adapter.ListRiwayatKasbonAdapter;
import com.wtm.cashbon.databinding.ActivityKasbonBinding;
import com.wtm.cashbon.model.StatusKasbonModel;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.DecimalsFormat;
import com.wtm.cashbon.utils.GeneralHelper;
import com.wtm.cashbon.utils.GlobalToast;
import com.wtm.cashbon.utils.MediaProcess;
import com.wtm.cashbon.utils.SessionHelper;
import com.wtm.cashbon.utils.SessionManager;
import com.wtm.cashbon.utils.VolleyHttp;
import com.wtm.cashbon.utils.VolleyMultipartHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class KasbonActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private int maxPinjam = 450;
    private int jumlahAwal = 300000;
    private int jumlahPinjam = 300000;
    private int kelipatan = 100000;
    private final int jumlahHari = 30;
    private double biayaAdmin = 3;

    private Intent extra;
    private ImageView imgCapture;
    private String fileFoto;
    private File fotoFile, tmpFile;
    public static final int SELFIE_REQUEST_CODE = 1102;
    private final int PERMISSION_ALL = 1105;
    private final String[] PERMISSIONS = {
            Manifest.permission.CAMERA
    };
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9101;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds
    private Location location;
    private LocationRequest locationRequest;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    ListRiwayatKasbonAdapter adapter;
    ListStatusKasbonAdapter adapterStatus;
    private SessionManager sessionManager;
    private AlertDialog progressDialog;
    private ArrayList<KasbonModel> dataSet;
    private ArrayList<StatusKasbonModel> dataStatus;

    private ActivityKasbonBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityKasbonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        init();
        hitung();

        binding.sbJumlahPinjaman.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                jumlahPinjam = jumlahAwal + (i * kelipatan);
                binding.txJumlahPinjaman.setText(DecimalsFormat.priceWithoutDecimal(String.valueOf(jumlahPinjam)));
                binding.sbJumlahPinjaman.setMax(maxPinjam);
//                    Log.d("maxpinjam3", String.valueOf(max));
                hitung();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//        binding.refreshLayout.setColorSchemeResources(
//                android.R.color.holo_green_dark, android.R.color.holo_blue_dark,
//                android.R.color.holo_orange_dark, android.R.color.holo_red_dark);
//        binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        getProfile();
//                    }
//                }, 2000);
//            }
//        });
    }

    private void init() {

        sessionManager = new SessionManager(this);
        extra = getIntent();
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        dataSet = new ArrayList<>();
        adapter = new ListRiwayatKasbonAdapter(dataSet, this);

        dataStatus = new ArrayList<>();
        adapterStatus = new ListStatusKasbonAdapter(dataStatus, this);

        termCondition();
        getProfile();

        if (!sessionManager.getBiayaAdmin().equals("0")) {
            biayaAdmin = Double.parseDouble(sessionManager.getBiayaAdmin());
            binding.txBiayaAdmin.setText("( Biaya Penarikan Kasbon " + biayaAdmin +"%");
        }
        if (!sessionManager.getMaxPinjam().equals("0")) {
            maxPinjam = Integer.parseInt(sessionManager.getMaxPinjam());
        }
        if (!sessionManager.getKelipatan().equals("0")) {
            kelipatan = Integer.parseInt(sessionManager.getKelipatan());
        }
        if (!sessionManager.getJumlahAwal().equals("0")) {
            jumlahAwal = Integer.parseInt(sessionManager.getJumlahAwal());
            jumlahPinjam = jumlahAwal;
            binding.txJumlahPinjaman.setText(DecimalsFormat.priceWithoutDecimal(sessionManager.getJumlahAwal()));
        }

        Log.d("logHitung", sessionManager.getMaxPinjam() + ", " + sessionManager.getJumlahAwal());
    }

    private void termCondition(){
        String link = "Saya menyetujui seluruh Syarat dan Ketentuan serta Kebijakan Privasi dari PT Progo Puncak Group";
        SpannableString ss = new SpannableString(Html.fromHtml(link));
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
            }
            @Override
            public void onClick(View textView) {

                AlertDialog.Builder builder = new AlertDialog.Builder(KasbonActivity.this);
                builder.setTitle("Syarat & Ketentuan");
                builder.setMessage(HtmlCompat.fromHtml(getString(R.string.terms), HtmlCompat.FROM_HTML_MODE_LEGACY));

                builder.setPositiveButton("Saya Mengerti", (dialog, which) -> dialog.dismiss());


                AlertDialog alert = builder.create();
                alert.setCancelable(false);
                alert.show();
            }
        };
        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
            }
            @Override
            public void onClick(View textView) {

                AlertDialog.Builder builder = new AlertDialog.Builder(KasbonActivity.this);
                builder.setTitle("Kebijakan Privasi");
                builder.setMessage(HtmlCompat.fromHtml(getString(R.string.privacy), HtmlCompat.FROM_HTML_MODE_LEGACY));

                builder.setPositiveButton("Saya Mengerti", (dialog, which) -> dialog.dismiss());


                AlertDialog alert = builder.create();
                alert.setCancelable(false);
                alert.show();

            }
        };
        ss.setSpan(clickableSpan1, 24, 44, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(clickableSpan2, 51, 68, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.txTerm.setText(ss);
        binding.txTerm.setMovementMethod(LinkMovementMethod.getInstance());
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

    public void hitung() {

//        double total_byr = jumlahPinjam * Math.pow((1 + (bunga / 100)), (double) jumlahHari);
//        double xbunga = (double) jumlahPinjam * (double) jumlahHari * (bunga / 100);
        double xbiayaadmin = (double) jumlahPinjam * (biayaAdmin / 100);
        double total_byr = (double) jumlahPinjam - xbiayaadmin;

        binding.txTotalPinjaman.setText(DecimalsFormat.priceWithoutDecimal(total_byr + ""));
        binding.txBiayaAdmin.setText("( Biaya admin Rp. " + DecimalsFormat.priceWithoutDecimal(xbiayaadmin + "")+" )");

    }

    private void getProfile() {
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_GET_PROFILE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("responsProfile", response);

                binding.lyNoConnection.setVisibility(View.GONE);
//                binding.cvRiwayat.setVisibility(View.VISIBLE);
                binding.cvPengajuan.setVisibility(View.VISIBLE);
                binding.cvStatusPengajuan.setVisibility(View.VISIBLE);

                if (SessionHelper.sessionManager(KasbonActivity.this).checkResponse(response)) {
                    try {

                        JSONObject jo = new JSONObject(response);
                        sessionManager.setMaxPinjam(jo.getString("kasbon_max"));
                        sessionManager.setJumlahAwal(jo.getString("kasbon_min"));
                        sessionManager.setKelipatan(jo.getString("kelipatan_kasbon"));
                        sessionManager.setBiayaAdmin(jo.getString("biaya_admin"));
                        sessionManager.setStatusPinjaman(jo.getString("status_kasbon"));
                        binding.txLinkBiayaAdmin.setText("Biaya Penarikan Kasbon "+jo.getString("biaya_admin")+"%");
                        binding.txKasbonAktif.setText("Total Kasbon Aktif Rp. "+DecimalsFormat.priceWithoutDecimal(jo.getString("nominal_pinjaman_aktif")));
                        binding.txKasbonVerifikasi.setText("Total Kasbon Verifikasi Rp. "+DecimalsFormat.priceWithoutDecimal(jo.getString("nominal_pinjaman_verifikasi")));

                        if (jo.getString("status_kasbon").equals("false")) {
                            binding.cvPengajuan.setVisibility(View.GONE);
                            binding.txMessage.setVisibility(View.VISIBLE);
                            binding.txMessage.setText("Saat ini anda belum dapat mengajuan kasbon gaji. Biasanya disebabkan beberapa faktor sebagai berikuti :\n\n" +
                                    "1. Saldo anda saat saat ini dibawah minimal pengajuan;\n" +
                                    "2. Telah memasuki tanggal cut off gajian di perusahaan anda;\n" +
                                    "3. Invoice yang telah dikirim belum dilunasi oleh Perusahaan anda;\n\n" +
                                    "Harap Cek aplikasi secara rutin untuk mendapatkan Update Pinjaman kasbon selanjutnya!");
                        }else{
                            binding.txMessage.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }

//                    getKasbonList();
//                    getKasbonStatus();
                }

                progressDialog.dismiss();
//                binding.refreshLayout.setRefreshing(false);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressDialog.dismiss();
//                binding.refreshLayout.setRefreshing(false);
                checkError(error);

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
        VolleyHttp.getInstance(KasbonActivity.this).addToRequestQueue(stringRequest);
    }

    private void getKasbonStatus() {

        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_GET_CASHBON_STATUS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("responsKasbon", response);

                if (SessionHelper.sessionManager(KasbonActivity.this).checkResponse(response)) {

                    try {

                        if (response.equals("null")) {
                            binding.cvStatusPengajuan.setVisibility(View.GONE);
                        }

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
                        LinearLayoutManager llm = new LinearLayoutManager(KasbonActivity.this);
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
                return params;
            }
        };

        VolleyHttp.getInstance(KasbonActivity.this).addToRequestQueue(stringRequest);
    }

    private void postKasbon() {

        progressDialog.show();

        VolleyMultipartHttp volleyMultipartRequest = new VolleyMultipartHttp(Request.Method.POST, AppConf.URL_POST_CASHBON,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse nResponse) {
                        String response = new String(nResponse.data);
                        Log.d("POSTKASBON", response);

                        try {

                            JSONObject jo = new JSONObject(response);
                            boolean r = jo.getBoolean("response");
                            String message = jo.getString("message");
                            if (r) {
//                        Toast.makeText(getApplicationContext(), "Terima Kasih", Toast.LENGTH_LONG).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(KasbonActivity.this);

                                builder.setTitle("Terima Kasih");
                                builder.setMessage("Pengajuan anda telah terkirim, mohon menunggu segera kami proses.");

                                builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();
                                        finish();

                                        binding.ivFoto.setVisibility(View.GONE);
                                        binding.txfotoselfie.setVisibility(View.VISIBLE);

                                    }
                                });


                                AlertDialog alert = builder.create();
                                alert.setCancelable(false);
                                alert.show();
                            } else {

                                AlertDialog.Builder builder = new AlertDialog.Builder(KasbonActivity.this);

                                builder.setTitle("Perhatian");
                                builder.setMessage(message);

                                builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();
                                        finish();

                                    }
                                });


                                AlertDialog alert = builder.create();
                                alert.setCancelable(false);
                                alert.show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                        progressDialog.dismiss();
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                SessionHelper.sessionManager(KasbonActivity.this).checkError(error);

                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Log.d("koneksivolley", jsonError);
                }
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + "");

                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("token", sessionManager.getToken());
                params.put("loan_amount", String.valueOf(jumlahPinjam));
                params.put("loan_fee", String.valueOf(biayaAdmin));
                params.put("lat", sessionManager.getLatitude());
                params.put("lng", sessionManager.getLongitude());
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("userfile_selfie", new DataPart(System.currentTimeMillis() + "_" + UUID.randomUUID() + ".jpg", MediaProcess.getFileToArray(fileFoto)));
                return params;
            }
        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                20 * 1000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //adding the request to volley
//        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(120000, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        volleyMultipartRequest.setTag(KasbonActivity.class.getSimpleName());
        Volley.newRequestQueue(KasbonActivity.this).add(volleyMultipartRequest);

    }

    private void dialogKonfirmasi() {
        // TODO Auto-generated method stub
        final Dialog dialog = new Dialog(KasbonActivity.this);

        LayoutInflater layout = (LayoutInflater) KasbonActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layout.inflate(R.layout.dialog_pengajuan, null);

        final TextView txKeterangan = view.findViewById(R.id.txKeterangan);
        final EditText etPass = view.findViewById(R.id.etPassword);
        final Button btLanjut = view.findViewById(R.id.btnLanjut);
        final Button btCancel = view.findViewById(R.id.btnCancel);

        txKeterangan.setText("Rp. " + DecimalsFormat.priceWithoutDecimal(jumlahPinjam + ""));

        btLanjut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etPass.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "Silahkan isi password", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.dismiss();
                    confirmPassword(etPass.getText().toString());
                }

            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();


            }
        });


        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(view);
        dialog.setCancelable(false);

        dialog.show();

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSION_ALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent moveForResultIntent = new Intent(KasbonActivity.this, CameraActivity.class);
                startActivityForResult(moveForResultIntent, SELFIE_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == SELFIE_REQUEST_CODE) {

            fileFoto = data.getStringExtra(CameraActivity.EXTRA_SELECTED_VALUE);
            binding.ivFoto.setImageBitmap(CameraActivity.bitmap);
            binding.ivFoto.setVisibility(View.VISIBLE);
            binding.txfotoselfie.setVisibility(View.GONE);


            Log.d("imagePathSurvacerepeat", fileFoto);
        }

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i("GPSAuto", "User agreed to make required location settings changes.");
//                    startLocationUpdates();
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i("GPSAuto", "User chose not to make required location settings changes.");
                    break;
            }
        }
    }

    void checkSP() {

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            displayLocationSettingsRequest(KasbonActivity.this);

        } else {

            if (googleApiClient != null && locationRequest != null) {
                if (!googleApiClient.isConnected()) {
                    googleApiClient.connect();
                }
            }

        }
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("GPSAuto", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("GPSAuto", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                        AlertDialog.Builder builder = new AlertDialog.Builder(KasbonActivity.this);

                        builder.setTitle("Informasi");
                        builder.setMessage("Kasbon meminta izin akses lokasi untuk mendapatkan data lokasi yang akurat guna proses verifikasi pengajuan Anda secara online ");

                        builder.setPositiveButton("Saya Mengerti", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                try {
                                    status.startResolutionForResult(KasbonActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException e) {
                                    Log.i("GPSAuto", "PendingIntent unable to execute request.");
                                }

                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.setCancelable(false);
                        alert.show();
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("GPSAuto", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        checkSP();
        checkPlayServices();

        getProfile();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(KasbonActivity.this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(KasbonActivity.this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
            }

            return false;
        }

        return true;
    }

    @Override
    public void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(KasbonActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(KasbonActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);


        if (location != null) {

            sessionManager.setLatitude(String.valueOf(location.getLatitude()));
            sessionManager.setLongitude(String.valueOf(location.getLongitude()));

        }

        startLocationUpdates();


    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(KasbonActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(KasbonActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            GlobalToast.ShowToast(KasbonActivity.this, "GPS belum diijinkan !");
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location loc) {

        if (loc != null) {
            location = loc;

            sessionManager.setLatitude(String.valueOf(loc.getLatitude()));
            sessionManager.setLongitude(String.valueOf(loc.getLongitude()));

            Log.d("locationlat", " location" + loc.getLatitude());
            Log.d("locationlong", " location" + loc.getLongitude());

//            Toast.makeText(getApplicationContext(), sessionManager.getLatitude().toString(), Toast.LENGTH_SHORT).show();


            Log.d("LOCNYA", loc.getLatitude() + " ;; " + loc.getLongitude());

            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();

        }
    }

    public void checkError(VolleyError error) {

        if (error instanceof TimeoutError) {
            binding.lyNoConnection.setVisibility(View.VISIBLE);
            binding.txNoConnection.setText(getString(R.string.timeout));

            binding.cvRiwayat.setVisibility(View.GONE);
            binding.cvPengajuan.setVisibility(View.GONE);
            binding.cvStatusPengajuan.setVisibility(View.GONE);
        } else if (error instanceof NoConnectionError) {
            binding.lyNoConnection.setVisibility(View.VISIBLE);
            binding.txNoConnection.setText(getString(R.string.no_connection));

            binding.cvRiwayat.setVisibility(View.GONE);
            binding.cvPengajuan.setVisibility(View.GONE);
            binding.cvStatusPengajuan.setVisibility(View.GONE);
        } else {
            binding.lyNoConnection.setVisibility(View.VISIBLE);
            binding.txNoConnection.setText(getString(R.string.gagal_server));

            binding.cvRiwayat.setVisibility(View.GONE);
            binding.cvPengajuan.setVisibility(View.GONE);
            binding.cvStatusPengajuan.setVisibility(View.GONE);
        }
    }

    private void confirmPassword(String password) {

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_CONFIRM_PASSWORD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("CONFIRM", response);

                try {

                    JSONObject jo = new JSONObject(response);
                    boolean r = jo.getBoolean("response");
                    if (r) {
                        postKasbon();
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(KasbonActivity.this);
                        builder.setTitle("Maaf");
                        builder.setMessage("Password yang anda masukkan salah");

                        builder.setPositiveButton("Oke", (dialog, which) -> dialog.dismiss());
                        AlertDialog alert = builder.create();
                        alert.setCancelable(false);
                        alert.show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                }
                progressDialog.dismiss();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                SessionHelper.sessionManager(getApplicationContext()).checkError(error);

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
                params.put("password", password.trim());
                return params;
            }
        };

        VolleyHttp.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btBack, R.id.btAjukan, R.id.btRefresh, R.id.btFoto, R.id.txBiayaAdmin})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btBack:
                finish();
                break;

            case R.id.btRefresh:
                getProfile();
                break;

            case R.id.btAjukan:
                if (fileFoto == null) {
                    GlobalToast.ShowToast(KasbonActivity.this, getString(R.string.silahkan_foto));
                } else if (!binding.cbKetentuan.isChecked()) {
                    GlobalToast.ShowToast(KasbonActivity.this, getString(R.string.persetujuan));
                } else {
                    dialogKonfirmasi();
                    Log.d("logHitung", String.valueOf(jumlahPinjam));
                }
                break;
            case R.id.btFoto:
                imgCapture = binding.ivFoto;
                tmpFile = fotoFile;
                if (!GeneralHelper.hasPermissions(KasbonActivity.this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(KasbonActivity.this, PERMISSIONS, PERMISSION_ALL);
                } else {
                    Intent moveForResultIntent = new Intent(KasbonActivity.this, CameraActivity.class);
                    startActivityForResult(moveForResultIntent, SELFIE_REQUEST_CODE);
//                    captureImage();
                }
                break;
            case R.id.txBiayaAdmin:

                AlertDialog.Builder builder = new AlertDialog.Builder(KasbonActivity.this);

                String text = "<ol type='A'>" +
                        "<li>Biaya Penarikan Kasbon anda akan dipotong dari depan diambil dari jumlah pengambilan kasbon yang anda ajukan saat ini;</li>" +
                        "<li>Pengajuan Kasbon anda akan di cairkan melalui rekening anda yang terdaftar dalam aplikasi Kasbon ini;</li>" +
                        "<li>Jika anda ingin merubah data rekening anda, harap hubungi HRD tempat anda bekerja sebagaimana tertera dalam aplikasi ini;</li>" +
                        "<li>Pastikan data rekening anda sudah benar dan valid. Kami tidak bertanggungjawab atas segala kesalahan pencairan Kasbon yang anda ajukan.</li>" +
                        "</ol>";

                builder.setTitle("Perhatian!");
                builder.setMessage(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY));

                builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                });


                AlertDialog alert = builder.create();
                alert.setCancelable(false);
                alert.show();
                break;
        }
    }
}