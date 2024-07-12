package com.example.mokoli;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class RekapAdapter extends RecyclerView.Adapter<RekapAdapter.RekapViewHolder> {

    private Context context;
    private List<RekapItem> rekapList;

    public RekapAdapter(Context context, List<RekapItem> rekapList) {
        this.context = context;
        this.rekapList = rekapList;
    }

    @NonNull
    @Override
    public RekapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rekap_item_layout, parent, false);
        return new RekapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RekapViewHolder holder, int position) {
        RekapItem currentItem = rekapList.get(position);

        holder.textViewBulan.setText(currentItem.getBulan());
        holder.textViewPembelianToken.setText("Rp " + formatCurrency((long) currentItem.getTotalPembelianToken()));
        holder.textViewJumlahToken.setText(formatTotalJumlahToken(currentItem.getTotalJumlahToken()));
    }

    private String formatCurrency(double value) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(value);
    }

    @Override
    public int getItemCount() {
        return rekapList.size();
    }

    private String formatTotalJumlahToken(double value) {
        return String.format("%.2f", value);
    }

    public static class RekapViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewBulan;
        public TextView textViewPembelianToken;
        public TextView textViewJumlahToken;

        public RekapViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBulan = itemView.findViewById(R.id.rekapbulan);
            textViewPembelianToken = itemView.findViewById(R.id.rekapbeli);
            textViewJumlahToken = itemView.findViewById(R.id.rekapjml);
        }
    }
}
