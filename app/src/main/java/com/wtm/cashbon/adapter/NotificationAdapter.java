package com.wtm.cashbon.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.wtm.cashbon.R;
import com.wtm.cashbon.model.NotifModel;
import com.wtm.cashbon.utils.AppConf;
import com.wtm.cashbon.utils.SessionHelper;
import com.wtm.cashbon.utils.SessionManager;
import com.wtm.cashbon.utils.VolleyHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {


    private ArrayList<NotifModel> dataSet;
    private Context context;
    private Intent intent;
    private SessionManager sessionManager;
//    String notif_id;

    public NotificationAdapter(ArrayList<NotifModel> data, Context context) {
        this.dataSet = data;
        this.context = context;

    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        // Inflate the layout view you have created for the list rows here
        View view = layoutInflater.inflate(R.layout.list_notif, parent, false);

        sessionManager = new SessionManager(context);

        return new MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return dataSet == null ? 0 : dataSet.size();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        final NotifModel value = dataSet.get(listPosition);

        holder.etJudulNotif.setText(value.getJudul());
        holder.etTanggalNotif.setText(value.getTanggal());
        holder.etIsiNotif.setText(value.getIsi());

//        notif_id = value.getId();

        if(value.getIs_read().toLowerCase().equals("false")){
            holder.linear.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.border_primary));
        }else{
            holder.linear.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.border_gray_notif));
        }

//        if(value.getTipe().toLowerCase().equals("verifikasi komisi")){
//            holder.btShare.setVisibility(View.VISIBLE);
//        }

        holder.btShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "KONEK");
//                    String shareMessage = "\nsaya telah mendapatkan komisi Rp. "+ DecimalsFormat.priceWithoutDecimal(value.getKomisi())+". ayo bergabung dengan kami dan dapatkan banyak keuntungan!!!\n\nMasukkan Kode Referral saat mendaftar : "+sessionManager.getKodeReferral()+"\n\n";
//                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=com.pelunas";
//                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    context.startActivity(Intent.createChooser(shareIntent, "Pilih salah satu"));
                } catch (Exception e) {
                    //e.toString();
                }
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent i = new Intent(context, DetailDaftarKlienActivity.class);
//                i.putExtra("id_klien", value.getId_klien());
//
//                if (value.getTipe().toLowerCase().equals("tugas baru")) {
//                    context.startActivity(i);
//                } else if (value.getTipe().toLowerCase().equals("komisi cair")) {
//                    context.startActivity(i);
//                }

                holder.linear.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.border_gray_notif));
                postReadNotif(value.getId());
            }
        });

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView etJudulNotif;
        TextView etTanggalNotif;
        TextView etIsiNotif;
        CardView cardView;
        RelativeLayout linear;
        Button btShare;

        public MyViewHolder(View itemView) {
            super(itemView);

            etJudulNotif = itemView.findViewById(R.id.txJudulNotif);
            etTanggalNotif = itemView.findViewById(R.id.txTanggalNotif);
            etIsiNotif = itemView.findViewById(R.id.txIsiNotif);
            cardView = itemView.findViewById(R.id.cvNotif);
            linear = itemView.findViewById(R.id.lyBG);
            btShare = itemView.findViewById(R.id.btShare);
        }
    }

    private void postReadNotif(String notif_id) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConf.URL_POST_NOTIF, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("POSTNOTIF", response);

                    JSONObject jo = new JSONObject(response);
                    if (jo.has("response")) {
                        boolean r = jo.getBoolean("response");

                        Log.d("POSTNOTIF", String.valueOf(r));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SessionHelper.sessionManager(context).checkError(error);

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
                params.put("token",  SessionHelper.sessionManager(context).getToken());
                params.put("notif_id",  notif_id);
                return params;
            }
        };

        VolleyHttp.getInstance(context).addToRequestQueue(stringRequest);

    }

}