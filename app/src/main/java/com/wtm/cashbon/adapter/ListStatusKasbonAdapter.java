package com.wtm.cashbon.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.wtm.cashbon.R;
import com.wtm.cashbon.TTD.TTDAktivActivity;
import com.wtm.cashbon.TTD.TTDDigisignActivity;
import com.wtm.cashbon.model.StatusKasbonModel;
import com.wtm.cashbon.utils.GlobalToast;
import com.wtm.cashbon.utils.SessionHelper;
import com.wtm.cashbon.utils.VolleyHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class ListStatusKasbonAdapter extends RecyclerView.Adapter<ListStatusKasbonAdapter.MyViewHolder> implements Filterable {


    private ArrayList<StatusKasbonModel> dataSet;

    private ArrayList<StatusKasbonModel> klienList;
    private Context context;
    private StatusKasbonAdapterListener listener;
    public ListStatusKasbonAdapter(ArrayList<StatusKasbonModel> data, Context context) {
        this.dataSet = data;
        this.klienList = data;
        this.context = context;
        this.listener = listener;

        progressDialog = new SpotsDialog(context, R.style.Custom);
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        // Inflate the layout view you have created for the list rows here
        View view = layoutInflater.inflate(R.layout.list_status_kasbon, parent, false);


        return new MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return dataSet == null? 0: dataSet.size();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        final StatusKasbonModel value = dataSet.get(listPosition);

        holder.txNoPengajuan.setText(value.getNoPinjaman());

        holder.txNominal.setText(value.getNominal());

        holder.txTanggal.setText(value.getTanggal());

        holder.txStatus.setText(value.getStatus());

        if(value.getStatus().equals("Kasbon Aktif")){
            holder.txInfoAktif.setVisibility(View.VISIBLE);
        }

        int nomer = value.getNomer() +1;

        if(Integer.parseInt(value.getSignStatus()) != 3){
            holder.btDIGI.setVisibility(View.VISIBLE);
        }

        if(value.getStatus().toLowerCase().equals("dibatalkan")){
            holder.btDIGI.setVisibility(View.GONE);
        }

        if(value.getIsSigned().toLowerCase().equals("f")){
            if(value.getSignRegistered().toLowerCase().equals("t")){
                holder.btDIGI.setText("Tanda Tangan Dokumen");
            }else{
                holder.btDIGI.setText("Daftarkan TTD anda");
            }
        }else if(value.getIsSigned().toLowerCase().equals("t")){
            holder.btDIGI.setText("Download Perjanjian");
        }

        holder.btDIGI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.btDIGI.getText().toString().toLowerCase().equals("download perjanjian")) {
                    context.startActivity(new Intent(context, TTDDigisignActivity.class));
                } else if (holder.btDIGI.getText().toString().toLowerCase().equals("tanda tangan dokumen")) {
                    postSENDDigisign();
                } else {
                    postDigisign();
                }

            }
        });

    }

    AlertDialog progressDialog;

    private void postDigisign() {

        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://pinjamwinwin.com/modules/api_digisign/registration.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("POSTDIGISIGN_REGIST", response);
                try {

                    JSONObject jo = new JSONObject(response);
                    String result = jo.getString("response");
                    if (result.contains("User ini sudah terdaftar") || result.contains("Berhasil Mendaftar")) {
                        postDigisignAktiv();
                    } else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setTitle("Maaf");
                        builder.setMessage(result);

                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        });


                        AlertDialog alert = builder.create();
                        alert.show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.d("POSTDIGISIGN_REGIST", e.toString());
                    GlobalToast.ShowToast(context, context.getString(R.string.gagal_server));

                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SessionHelper.sessionManager(context).checkError(error);
                progressDialog.dismiss();
            }

        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("id_klien", SessionHelper.sessionManager(context).getIDKlien());
                return params;
            }
        };
        VolleyHttp.getInstance(context).addToRequestQueue(stringRequest);
    }

    private void postDigisignAktiv() {

        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://pinjamwinwin.com/modules/api_digisign/activation.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("POSTDIGISIGNAktiv", response);
                try {

                    JSONObject jo = new JSONObject(response);
                    String result = jo.getString("response");
                    if (result.contains("User ini sudah aktif")) {
//                        btDIGI.setText("TANDA TANGAN DOKUMEN");

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setTitle("Selamat");
                        builder.setMessage("Pendaftaran anda berhasil, silahkan tanda tangan dokumen segera");

                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        });


                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        context.startActivity(new Intent(context, TTDAktivActivity.class));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    GlobalToast.ShowToast(context, context.getString(R.string.gagal_server));

                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SessionHelper.sessionManager(context).checkError(error);
                progressDialog.dismiss();
            }

        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("id_klien", SessionHelper.sessionManager(context).getIDKlien());
                return params;
            }
        };
        VolleyHttp.getInstance(context).addToRequestQueue(stringRequest);
    }

    private void postSENDDigisign() {

        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://pinjamwinwin.com/modules/api_digisign/send_document.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("POSTDIGISIGN_SEND", response);
                try {

                    JSONObject jo = new JSONObject(response);
                    String result = jo.getString("response");
                    if (result.contains("sukses")) {
                        postSIGNDigisign();
                    } else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setTitle("Perhatian");
                        builder.setMessage(result);

                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        });


                        AlertDialog alert = builder.create();
                        alert.show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    GlobalToast.ShowToast(context, context.getString(R.string.gagal_server));

                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                SessionHelper.sessionManager(context).checkError(error);
            }

        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("id_klien", SessionHelper.sessionManager(context).getIDKlien());
                return params;
            }
        };
        VolleyHttp.getInstance(context).addToRequestQueue(stringRequest);
    }

    private void postSIGNDigisign() {

        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://pinjamwinwin.com/modules/api_digisign/sign_document.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("POSTDIGISIGN_SIGN", response);
                try {

                    JSONObject jo = new JSONObject(response);
                    String result = jo.getString("response");
                    if (result.contains("sukses")) {
                        context.startActivity(new Intent(context, TTDDigisignActivity.class));
                    } else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setTitle("Perhatian");
                        builder.setMessage(result);

                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        });


                        AlertDialog alert = builder.create();
                        alert.show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    GlobalToast.ShowToast(context, context.getString(R.string.gagal_server));

                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                SessionHelper.sessionManager(context).checkError(error);
            }

        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("id_klien", SessionHelper.sessionManager(context).getIDKlien());
                return params;
            }
        };
        VolleyHttp.getInstance(context).addToRequestQueue(stringRequest);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txNominal;
        TextView txTanggal;
        TextView txNo;
        TextView txStatus;
        TextView txNoPengajuan;
        TextView txInfoAktif;
        Button btDIGI;

        public MyViewHolder(View itemView) {
            super(itemView);

            txStatus = itemView.findViewById(R.id.txStatusLunas);
            txTanggal = itemView.findViewById(R.id.txStatusTanggalPengajuan);
            txNominal = itemView.findViewById(R.id.txStatusTotalPinjaman);
            txNoPengajuan = itemView.findViewById(R.id.txStatusNoPengajuan);
            txInfoAktif = itemView.findViewById(R.id.txInfoAktif);
            btDIGI = itemView.findViewById(R.id.btTTD);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    dataSet = klienList;
                } else {
                    ArrayList<StatusKasbonModel> filteredList = new ArrayList<>();
                    for (StatusKasbonModel row : klienList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getTanggal().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    dataSet = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = dataSet;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                dataSet = (ArrayList<StatusKasbonModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface StatusKasbonAdapterListener {
        void onKlienSelected(StatusKasbonModel StatusKasbonModel);
    }
}
