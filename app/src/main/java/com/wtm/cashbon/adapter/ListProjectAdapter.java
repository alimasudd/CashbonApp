package com.wtm.cashbon.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.wtm.cashbon.R;
import com.wtm.cashbon.home.project.KunjunganActivity;
import com.wtm.cashbon.home.project.ProjectActivity;
import com.wtm.cashbon.model.ProjectModel;
import com.wtm.cashbon.utils.SessionManager;

import java.util.ArrayList;

public class ListProjectAdapter extends RecyclerView.Adapter<ListProjectAdapter.MyViewHolder> {


    private ArrayList<ProjectModel> dataSet;
    private Context context;
    private Intent intent;
    private SessionManager sessionManager;

    public ListProjectAdapter(ArrayList<ProjectModel> data, Context context) {
        this.dataSet = data;
        this.context = context;

    }

    @Override
    public ListProjectAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        // Inflate the layout view you have created for the list rows here
        View view = layoutInflater.inflate(R.layout.list_kunjungan, parent, false);

        sessionManager = new SessionManager(context);

        return new ListProjectAdapter.MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return dataSet == null ? 0 : dataSet.size();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final ListProjectAdapter.MyViewHolder holder, final int listPosition) {

        final ProjectModel value = dataSet.get(listPosition);

        holder.txJudul.setText(value.getJudul());
        holder.txTanggalAwal.setText(value.getTanggalAwal());
        holder.txTanggalAkhir.setText(value.getTanggalAkhir());
        holder.txStatus.setText(value.getStatus());

        if (value.getStatus().toLowerCase().equals("selesai")) {
            holder.txStatus.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.border_button_gray));
            holder.cvKunjungan.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorGrayDark));
        } else {
            holder.txStatus.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.border_button_red));
            holder.cvKunjungan.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorRed));
        }


        holder.cvKunjungan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, ProjectActivity.class);
                i.putExtra("id", value.getId());
                i.putExtra("idStatus", value.getStatus_id());
                i.putExtra("judul", value.getJudul());
                i.putExtra("status", value.getStatus());
                i.putExtra("keterangan", value.getKeterangan());
                i.putExtra("tanggal_awal", value.getTanggalAwal());
                i.putExtra("tanggal_akhir", value.getTanggalAkhir());
//
                if (value.getStatus().toLowerCase().equals("selesai")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setTitle("Maaf");
                    builder.setMessage("Project sudah selesai");

                    builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });


                    AlertDialog alert = builder.create();
                    alert.setCancelable(false);
                    alert.show();
                } else {
                    context.startActivity(i);
                }
            }
        });

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txJudul;
        TextView txTanggalAwal;
        TextView txTanggalAkhir;
        TextView txStatus;
        CardView cvKunjungan;

        public MyViewHolder(View itemView) {
            super(itemView);

            txJudul = itemView.findViewById(R.id.txJudulTugas);
            txTanggalAwal = itemView.findViewById(R.id.txTanggalAwal);
            txTanggalAkhir = itemView.findViewById(R.id.txTanggalAkhir);
            txStatus = itemView.findViewById(R.id.txStatusTugas);
            cvKunjungan = itemView.findViewById(R.id.cvKunjungan);
        }
    }
}