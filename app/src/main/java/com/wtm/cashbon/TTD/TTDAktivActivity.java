package com.wtm.cashbon.TTD;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.wtm.cashbon.R;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.GlobalToast;
import com.wtm.cashbon.utils.SessionHelper;
import com.wtm.cashbon.utils.SessionManager;
import com.wtm.cashbon.utils.VolleyHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class TTDAktivActivity extends AppCompatActivity {


    @BindView(R.id.webView)
    WebView webView;

    SessionManager sessionManager;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttdaktiv);
        ButterKnife.bind(this);

        progressDialog = new SpotsDialog(TTDAktivActivity.this, R.style.Custom);
        progressDialog.setCancelable(false);
        sessionManager = new SessionManager(this);
        loadURL();
    }

    private void loadURL() {

        WebSettings webSetting = webView.getSettings();
//        webSetting.setBuiltInZoomControls(true);
        webSetting.setJavaScriptEnabled(true);
        webView.setWebViewClient(new CustomWebViewClient());
//        Log.d("LOADDIGISIGN", "load " + sessionManager.getLinkAktivasi());
        webView.loadUrl("activation_open?id_klien=" + sessionManager.getIDKlien());

    }


    public class CustomWebViewClient extends android.webkit.WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
//            pbChecking.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url.contains("https://pinjamwinwin.com/api_digisign/close")) {
//                webView.destroy();
                webView.loadUrl("https://pinjamwinwin.com/api_digisign/activation_open?id_klien=" + sessionManager.getIDKlien());
                postDigisign();
            }

            // TODO Auto-generated method stub
//            view.loadUrl(url);
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub

            super.onPageFinished(view, url);
//            pbChecking.setVisibility(View.GONE);
        }

    }

    private void postDigisign() {

//        progressDialog.setMessage(getString(R.string.silahkan_tunggu));
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_POST_DATADIGI, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POSTDIGISIGN_DATA", response);
                try {

                    JSONObject jo = new JSONObject(response);
                    String result = jo.getString("message");
                    if (result.equals("success_digisign")) {

//                        sessionManager.setDone(1);
//                        Intent intent = new Intent(getString(R.string.pengajuan));
//                        intent.putExtra(getString(R.string.step), 4);
//                        LocalBroadcastManager.getInstance(TTDAktivActivity.this).sendBroadcast(intent);

                        finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.d("POSTDIGISIGN_DATA", response);
                    GlobalToast.ShowToast(TTDAktivActivity.this, getString(R.string.gagal_server));

                }
                progressDialog.dismiss();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                SessionHelper.sessionManager(TTDAktivActivity.this).checkError(error);
            }

        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("token", sessionManager.getToken());
                return params;
            }
        };
        VolleyHttp.getInstance(TTDAktivActivity.this).addToRequestQueue(stringRequest);
    }

    @OnClick(R.id.btBack)
    public void onViewClicked() {
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();

    }
}
