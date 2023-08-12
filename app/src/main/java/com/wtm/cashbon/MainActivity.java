package com.wtm.cashbon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.wtm.cashbon.profile.ProfileFragment;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.SessionManager;
import com.wtm.cashbon.utils.VolleyHttp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.bottom_bar)
    BottomNavigationView bottomBar;

    private SessionManager sessionManager;
    String GAID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        sessionManager = new SessionManager(MainActivity.this);

        bottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        repaceFragment(new HomeFragment(), HomeFragment.class.getSimpleName());
                        break;
                    case R.id.navigation_profile:
                        repaceFragment(new ProfileFragment(), ProfileFragment.class.getSimpleName());
                        break;
                    case R.id.navigation_setting:
                        repaceFragment(new SettingFragment(), SettingFragment.class.getSimpleName());
                        break;
                }

                return true;
            }
        });

        repaceFragment(new HomeFragment(), HomeFragment.class.getSimpleName());
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(statusReceiver, new IntentFilter(getString(R.string.cashbon)));

        getVersionCode();
        new GetGAIDTask().execute();

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("boolean", String.valueOf(sessionManager.getDone()));
        if (sessionManager.getDone() == 1) {
            repaceFragment(new HomeFragment(), HomeFragment.class.getSimpleName());
            sessionManager.setDone(0);
        }
    }

    private void repaceFragment(Fragment fragment, String TAG) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment, TAG);
        transaction.commit();
    }

    private BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String fragment = intent.getStringExtra(getString(R.string.fragment));
            Fragment a = null;

            if (fragment.equals(HomeFragment.class.getSimpleName())) {
                Log.d("broadcast", fragment);
                bottomBar.getMenu().findItem(R.id.navigation_home).setChecked(true);
                a = new HomeFragment();
            } else if (fragment.equals(ProfileFragment.class.getSimpleName())) {
                a = new ProfileFragment();
            } else if (fragment.equals(SettingFragment.class.getSimpleName())) {
                a = new SettingFragment();
            }

            if (a != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, a, fragment);
                transaction.commitAllowingStateLoss();
            }

        }
    };

    private class GetGAIDTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            AdvertisingIdClient.Info adInfo;
            adInfo = null;
            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(MainActivity.this.getApplicationContext());
                if (adInfo.isLimitAdTrackingEnabled()) // check if user has opted out of tracking
                    return "did not found GAID... sorry";
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            }
            return adInfo.getId();
        }

        @Override
        protected void onPostExecute(String s) {
            GAID = s;
            Log.d("LogADID", GAID);
            postDeviceID();
        }
    }

    private void postDeviceID() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_POST_ADID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POSTADID", response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("token", sessionManager.getToken());
                params.put("id_handphone", GAID);
                return params;
            }
        };

//        updateAndroidSecurityProvider();

        stringRequest.setTag(LoginActivity.class.getSimpleName());
        VolleyHttp.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

    }

    private void getVersionCode() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_GET_VERSIONCODE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("responVersion", response);

                try {
                    JSONObject jo = new JSONObject(response);
                    int versionNow = Integer.valueOf(jo.getString("version"));
                    String linkupdate = jo.getString("link");
                    String message = jo.getString("message");

                    try {
                        PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
                        int version = pInfo.versionCode;
                        if (version < versionNow) {
                            dialogPopup(linkupdate, message);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }

        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("token", sessionManager.getToken());
                return params;
            }
        };

        stringRequest.setTag(MainActivity.class.getSimpleName());
        VolleyHttp.getInstance(MainActivity.this).addToRequestQueue(stringRequest);
    }

    private void dialogPopup(String linkupdate, String message) {
        // TODO Auto-generated method stub
        final Dialog dialog = new Dialog(MainActivity.this);

        LayoutInflater layout = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layout.inflate(R.layout.dialog_update_aplikasi, null);

        final TextView txKeterangan = view.findViewById(R.id.txKeterangan);
        final Button btUpdate = view.findViewById(R.id.btUpdate);

        txKeterangan.setText(message);
        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(linkupdate); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
//                dialog.dismiss();
            }
        });
//        btClose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//
//                sessionManager.setDone(1);
//                Intent intent = new Intent(getString(R.string.pengajuan));
//                intent.putExtra(getString(R.string.step), 4);
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//
//            }
//        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(view);
        dialog.setCancelable(false);

        dialog.show();

    }

    private void outApp() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(getString(R.string.konfirmasi));
        builder.setMessage(getString(R.string.apakah_exit));

        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                finish();

            }
        });

        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        HomeFragment home = (HomeFragment) getSupportFragmentManager().findFragmentByTag(HomeFragment.class.getSimpleName());
        if (home != null && home.isVisible()) {
            //DO STUFF
            outApp();
        } else {
            repaceFragment(new HomeFragment(), HomeFragment.class.getSimpleName());
//            toolbarTop.setVisibility(View.VISIBLE);
//            toolbarHide.setVisibility(View.GONE);
            bottomBar.getMenu().findItem(R.id.navigation_home).setChecked(true);
        }
    }

}