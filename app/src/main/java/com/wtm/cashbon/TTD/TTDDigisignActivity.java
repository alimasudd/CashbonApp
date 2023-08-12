package com.wtm.cashbon.TTD;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.DownloadListener;
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
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class TTDDigisignActivity extends AppCompatActivity {

    @BindView(R.id.webView)
    WebView webView;

    SessionManager sessionManager;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttddigisign);
        ButterKnife.bind(this);

        progressDialog = new SpotsDialog(TTDDigisignActivity.this, R.style.Custom);
        progressDialog.setCancelable(false);
        sessionManager = new SessionManager(TTDDigisignActivity.this);
        loadURL();
    }


    @Override
    public void onResume() {
        super.onResume();

        progressDialog = new SpotsDialog(TTDDigisignActivity.this, R.style.Custom);
        progressDialog.setCancelable(false);
        sessionManager = new SessionManager(TTDDigisignActivity.this);
        loadURL();
    }

    private void loadURL() {

        WebSettings webSetting = webView.getSettings();
//        webSetting.setBuiltInZoomControls(true);
        webSetting.setJavaScriptEnabled(true);
        webView.setWebViewClient(new CustomWebViewClient());
//        Log.d("LOADDIGISIGN", "load " + sessionManager.getLinkAktivasi());
        webView.loadUrl("sign_document_open?id_klien=" + sessionManager.getIDKlien());

        webView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

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

            // TODO Auto-generated method stub
            if (url.contains("https://pinjamwinwin.com/api_digisign/close")) {
                postDigisign();
            }
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub

            super.onPageFinished(view, url);
        }

    }

    private void postDigisign() {

//        progressDialog.setMessage(getString(R.string.silahkan_tunggu));
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_POST_DATADIGIDOK, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POSTDIGISIGN_SIGNED", response);
                try {

                    JSONObject jo = new JSONObject(response);
                    String result = jo.getString("message");
                    if (result.equals("success_digisign")) {
                        finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

//                    Log.d("POSTDIGISIGN_SIGNED", response);
                    GlobalToast.ShowToast(TTDDigisignActivity.this, getString(R.string.gagal_server));

                }
                progressDialog.dismiss();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                SessionHelper.sessionManager(TTDDigisignActivity.this).checkError(error);
            }

        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("id_klien", sessionManager.getIDKlien());
                return params;
            }
        };
        VolleyHttp.getInstance(TTDDigisignActivity.this).addToRequestQueue(stringRequest);
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
