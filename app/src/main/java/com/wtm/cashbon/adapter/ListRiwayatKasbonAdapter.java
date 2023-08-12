package com.wtm.cashbon.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.wtm.cashbon.model.KasbonModel;
import com.wtm.cashbon.R;

import java.util.ArrayList;

public class ListRiwayatKasbonAdapter extends RecyclerView.Adapter<ListRiwayatKasbonAdapter.MyViewHolder> implements Filterable {


    private ArrayList<KasbonModel> dataSet;

    private ArrayList<KasbonModel> klienList;
    private Context context;
    private KasbonAdapterListener listener;

    public ListRiwayatKasbonAdapter(ArrayList<KasbonModel> data, Context context) {
        this.dataSet = data;
        this.klienList = data;
        this.context = context;
        this.listener = listener;

    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        // Inflate the layout view you have created for the list rows here
        View view = layoutInflater.inflate(R.layout.list_riwayat_kasbon, parent, false);


        return new MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return dataSet == null? 0: dataSet.size();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        final KasbonModel value = dataSet.get(listPosition);

        holder.txNominal.setText(value.getNominal());

        holder.txTanggal.setText(value.getTanggal());

        holder.txStatus.setText(value.getStatus());
        if(value.getStatus().trim().equals("dibatalkan")){
            holder.txStatus.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.border_button_gray));
        }else if(value.getStatus().trim().equals("ditolak")){
            holder.txStatus.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.border_button_red));
        }else if(value.getStatus().trim().equals("aktif")){
            holder.txStatus.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.border_button));
        }else{
            holder.txStatus.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.border_button_green));
        }

        int nomer = value.getNomer() +1;
//        holder.txNo.setText(String.valueOf(nomer));

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txNominal;
        TextView txTanggal;
        TextView txNo;
        TextView txStatus;

        public MyViewHolder(View itemView) {
            super(itemView);

            txStatus = itemView.findViewById(R.id.txStatusLunas);
            txTanggal = itemView.findViewById(R.id.txTanggalPengajuan);
            txNominal = itemView.findViewById(R.id.txTotalPinjaman);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    listener.onKlienSelected(dataSet.get(getAdapterPosition()));
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
                    ArrayList<KasbonModel> filteredList = new ArrayList<>();
                    for (KasbonModel row : klienList) {

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
                dataSet = (ArrayList<KasbonModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface KasbonAdapterListener {
        void onKlienSelected(KasbonModel kasbonModel);
    }
}
