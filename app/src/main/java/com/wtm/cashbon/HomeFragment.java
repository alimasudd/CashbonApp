package com.wtm.cashbon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.wtm.cashbon.databinding.FragmentHomeBinding;
import com.wtm.cashbon.home.AktifActivity;
import com.wtm.cashbon.home.VerifikasiActivity;
import com.wtm.cashbon.home.FormCutiActivity;
import com.wtm.cashbon.home.KasbonActivity;
import com.wtm.cashbon.home.LunasActivity;
import com.wtm.cashbon.home.NotificationActivity;
import com.wtm.cashbon.home.ProjectHomeActivity;
import com.wtm.cashbon.home.AbsensiActivity;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.DecimalsFormat;
import com.wtm.cashbon.utils.GeneralHelper;
import com.wtm.cashbon.utils.GlobalToast;
import com.wtm.cashbon.utils.SessionHelper;
import com.wtm.cashbon.utils.SessionManager;
import com.wtm.cashbon.utils.VolleyHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class HomeFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    Unbinder unbinder;

    private SessionManager sessionManager;
    private AlertDialog progressDialog;
    private FragmentActivity mActivity;

    private FragmentHomeBinding binding;

    private int slide_ke = 0;

    String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9101;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds
    private Location location;
    private LocationRequest locationRequest;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private int kasbon_aktif = 1;
    private boolean isApplyCashbon = false;

    public HomeFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        unbinder = ButterKnife.bind(this, view);
        mActivity = getActivity();

        init();

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void init(){
        sessionManager = new SessionManager(mActivity);
        progressDialog = new SpotsDialog(mActivity, R.style.Custom);
        progressDialog.setCancelable(false);


        googleApiClient = new GoogleApiClient.Builder(mActivity).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM yyyy");
        LocalDateTime now = LocalDateTime.now();
//        binding.txTanggal.setText(dtf.format(now));

        getProfile();
    }


    private void getProfile() {
        progressDialog.show();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_GET_PROFILE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                binding.lyNoConnection.setVisibility(View.GONE);
                binding.linearMenu.setVisibility(View.VISIBLE);
                Log.d("responsProfile", response);
                Log.d("tokenProfile", sessionManager.getToken());

                if (SessionHelper.sessionManager(mActivity).checkResponse(response)) {
                    try {

                        JSONObject jo = new JSONObject(response);
                        binding.txName.setText(""+jo.getString("nama_lengkap"));
                        binding.txNamaPerusahaan.setText(""+jo.getString("nama_perusahaan"));
                        sessionManager.setNama(jo.getString("nama_lengkap"));
                        sessionManager.setEmail(jo.getString("email"));
                        sessionManager.setRekening(jo.getString("no_rekening"));
                        sessionManager.setBankID(jo.getInt("bank_id"));
                        sessionManager.setImageProfile(jo.getString("image"));
                        sessionManager.setMaxPinjam(jo.getString("kasbon_max"));
                        sessionManager.setJumlahAwal(jo.getString("kasbon_min"));
                        sessionManager.setKelipatan(jo.getString("kelipatan_kasbon"));
                        sessionManager.setBiayaAdmin(jo.getString("biaya_admin"));
                        sessionManager.setStatusPinjaman(jo.getString("status_kasbon"));
                        sessionManager.setBank(jo.getString("nama_bank"));
                        sessionManager.setNamaRekening(jo.getString("bank_atas_nama"));
                        sessionManager.setEditBank(jo.getBoolean("isEditBank"));
                        kasbon_aktif = Integer.parseInt(jo.getString("status_aktif"));
                        isApplyCashbon = jo.getBoolean("isApplyCashbon");
//                        binding.txNotif.setText(jo.getString("unread_notif"));
                        binding.txKasbonDiambil.setText("Limit Gaji Tersedia \nRp. "+ DecimalsFormat.priceWithoutDecimal(jo.getString("kasbon_max_nominal")));
                        binding.txGajian.setText("Gajian \nsetiap tanggal "+jo.getString("tgl_gajian"));
                        binding.txJumlahAktif.setText(jo.getString("jml_kasbon_aktif"));
                        binding.txJumlahAlpa.setText(jo.getString("jml_alpa"));
                        binding.txJumlahSakit.setText(jo.getString("jml_sakit"));
                        binding.txJumlahIzin.setText(jo.getString("jml_izin"));
                        binding.txJumlahLunas.setText(jo.getString("jml_kasbon_lunas"));
                        binding.txJumlahBatal.setText(jo.getString("jml_pinjaman_verifikasi"));
                        binding.txJumlahTerlambat.setText(jo.getString("jml_terlambat"));
                        binding.txJumlahKasbon.setText(jo.getString("jml_kasbon"));
                        binding.txNotif.setText(jo.getString("notif_unread"));
//                        Glide.with(mActivity).load(jo.getString("logo_pangkat")).into(binding.ivPangkat);
//                        if (jo.has("file_selfie")) {
//                            Glide.with(mActivity).load(imageLink).placeholder(R.drawable.user_icon).into(binding.ivPhoto);
//                        }
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

    public void checkError(VolleyError error) {

        if (error instanceof TimeoutError) {
            binding.lyNoConnection.setVisibility(View.VISIBLE);
            binding.txNoConnection.setText(getString(R.string.timeout));

            binding.linearMenu.setVisibility(View.GONE);
        } else if (error instanceof NoConnectionError) {
            binding.lyNoConnection.setVisibility(View.VISIBLE);
            binding.txNoConnection.setText(getString(R.string.no_connection));

            binding.linearMenu.setVisibility(View.GONE);
        } else {
            binding.lyNoConnection.setVisibility(View.VISIBLE);
            binding.txNoConnection.setText(getString(R.string.gagal_server));

            binding.linearMenu.setVisibility(View.GONE);
        }
    }


    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btNextSlide, R.id.cvAbsensi, R.id.cvCuti, R.id.cvProject, R.id.cvKasbon, R.id.btRefresh, R.id.ivNotif, R.id.btLunas, R.id.btAktif, R.id.btBatal, R.id.ivWhatsapp})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btNextSlide:
                if(slide_ke == 0){
                    binding.linearSlide1.setVisibility(View.VISIBLE);
                    binding.linearSlide2.setVisibility(View.GONE);
                    binding.linearSlide3.setVisibility(View.GONE);
                    binding.linearSlide4.setVisibility(View.GONE);
                    slide_ke = 1;
                } else if(slide_ke == 1){
                    binding.linearSlide1.setVisibility(View.GONE);
                    binding.linearSlide2.setVisibility(View.VISIBLE);
                    binding.linearSlide3.setVisibility(View.GONE);
                    binding.linearSlide4.setVisibility(View.GONE);
                    slide_ke = 2;
                } else if(slide_ke == 2){
                    binding.linearSlide1.setVisibility(View.GONE);
                    binding.linearSlide2.setVisibility(View.GONE);
                    binding.linearSlide3.setVisibility(View.VISIBLE);
                    binding.linearSlide4.setVisibility(View.GONE);
                    slide_ke = 3;
                } else if(slide_ke == 3){
                    binding.linearSlide1.setVisibility(View.GONE);
                    binding.linearSlide2.setVisibility(View.GONE);
                    binding.linearSlide3.setVisibility(View.GONE);
                    binding.linearSlide4.setVisibility(View.VISIBLE);
                    slide_ke = 0;
                }
                break;

            case R.id.cvAbsensi:
                startActivity(new Intent(mActivity, AbsensiActivity.class));
                break;

            case R.id.cvProject:
                startActivity(new Intent(mActivity, ProjectHomeActivity.class));
                break;

            case R.id.cvCuti:
                startActivity(new Intent(mActivity, FormCutiActivity.class));
                break;

            case R.id.cvKasbon:
                if (!GeneralHelper.hasPermissions(mActivity, PERMISSIONS_LOCATION)) {
                    ActivityCompat.requestPermissions(mActivity, PERMISSIONS_LOCATION, AppConf.PERMISSION_ALL);
                }else if(kasbon_aktif == 2){

                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

                    builder.setTitle("Perhatian");
                    builder.setMessage("Maaf Akun anda telah di nonaktifkan oleh HRD anda, yang mendapatkan layanan Kasbon adalah hanya karyawan yang sedang aktif bekerja." +
                            "\n\nHubungi HRD anda jika anda masih aktif di perusahaan tersebut!");

                    builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }
                    });


                    AlertDialog alert = builder.create();
                    alert.setCancelable(false);
                    alert.show();
                }
//                else if(!isApplyCashbon){
//                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
//
//                    builder.setTitle("Maaf");
//                    builder.setMessage("Saldo anda saat ini dibawah minimal pengajuan kasbon");
//
//                    builder.setPositiveButton("Saya mengerti", new DialogInterface.OnClickListener() {
//
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            dialog.dismiss();
//
//                        }
//                    });
//
//
//                    AlertDialog alert = builder.create();
//                    alert.setCancelable(false);
//                    alert.show();
//                }
                else{
                    startActivity(new Intent(mActivity, KasbonActivity.class));
                }
                break;

            case R.id.btRefresh:
                getProfile();
                break;

            case R.id.ivNotif:
                startActivity(new Intent(mActivity, NotificationActivity.class));
                break;

            case R.id.btLunas:
                startActivity(new Intent(mActivity, LunasActivity.class));
                break;

            case R.id.btAktif:
                startActivity(new Intent(mActivity, AktifActivity.class));
                break;

            case R.id.btBatal:
                startActivity(new Intent(mActivity, VerifikasiActivity.class));
                break;
            case R.id.ivWhatsapp:
                String noHP = "081331479537";
                if (noHP.substring(0, 1).equals("0")) {
                    noHP = "+62" + noHP.substring(1);
                    Log.d("nomorwa", "test1");
                }
                Uri uri = Uri.parse("https://wa.me/" + noHP); // missing 'http://' will cause crashed
                Intent intent0 = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent0);
                Log.d("nomorwa", noHP);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        checkGPS();
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
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(mActivity);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(mActivity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
            }

            return false;
        }

        return true;
    }

    public void checkGPS() {


        final LocationManager manager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            displayLocationSettingsRequest(mActivity);
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
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

                        builder.setTitle("Informasi");
                        builder.setMessage("Kasbon meminta izin akses lokasi untuk mendapatkan data lokasi yang akurat guna proses verifikasi pengajuan Anda secara online ");
                        builder.setPositiveButton("Saya Mengerti", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                try {
                                    status.startResolutionForResult(mActivity, REQUEST_CHECK_SETTINGS);
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

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == AppConf.PERMISSION_ALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(mActivity, KasbonActivity.class));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("GPSAuto", "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("GPSAuto", "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
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

        if (ActivityCompat.checkSelfPermission(mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mActivity,
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

        if (ActivityCompat.checkSelfPermission(mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            GlobalToast.ShowToast(mActivity, "GPS belum diijinkan !");
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
//            Toast.makeText(getActivity(), sessionManager.getLatitude().toString(), Toast.LENGTH_SHORT).show();

            Log.d("LOCNYA", loc.getLatitude() + " ;; " + loc.getLongitude());

            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();

//            submitLocation();

        }
    }
}