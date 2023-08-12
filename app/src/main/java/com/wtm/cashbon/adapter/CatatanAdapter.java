package com.wtm.cashbon.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wtm.cashbon.R;
import com.wtm.cashbon.model.CatatanModel;

import java.util.ArrayList;

public class CatatanAdapter extends RecyclerView.Adapter<CatatanAdapter.MyViewHolder> {


    private ArrayList<CatatanModel> dataSet;

    private ArrayList<CatatanModel> klienList;
    private Context context;
    private Intent intent;

    public CatatanAdapter(ArrayList<CatatanModel> data, Context context) {
        this.dataSet = data;
        this.klienList = data;
        this.context = context;

    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        // Inflate the layout view you have created for the list rows here
        View view = layoutInflater.inflate(R.layout.list_catatan, parent, false);


        return new MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return dataSet == null? 0: dataSet.size();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        final CatatanModel value = dataSet.get(listPosition);

        holder.txCatatan.setText(value.getCatatan());

        holder.txTanggal.setText(value.getTanggal());

        holder.txNama.setText(value.getNama());

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txTanggal;
        TextView txCatatan;
        TextView txNama;

        public MyViewHolder(View itemView) {
            super(itemView);

            txCatatan = itemView.findViewById(R.id.txCatatan);
            txTanggal = itemView.findViewById(R.id.txTanggal);
            txNama = itemView.findViewById(R.id.txNama);

        }
    }
}
