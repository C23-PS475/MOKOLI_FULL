package com.example.mokoli;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RiwayatAdapter extends RecyclerView.Adapter<RiwayatAdapter.RiwayatViewHolder> {
    private List<RiwayatItem> riwayatList;

    public RiwayatAdapter(List<RiwayatItem> riwayatList) {
        this.riwayatList = riwayatList;
    }

    @NonNull
    @Override
    public RiwayatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item_layout, parent, false);
        return new RiwayatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RiwayatViewHolder holder, int position) {
        RiwayatItem riwayatItem = riwayatList.get(position);
        holder.bind(riwayatItem);
    }

    @Override
    public int getItemCount() {
        return riwayatList.size();
    }

    public static class RiwayatViewHolder extends RecyclerView.ViewHolder {
        TextView currentDateTextView;
        TextView fullnameTextView;
        TextView jumlahTokenTextView;
        TextView metodePembayaranTextView;
        TextView pembelianTokenTextView;

        public RiwayatViewHolder(@NonNull View itemView) {
            super(itemView);
            currentDateTextView = itemView.findViewById(R.id.texttgl);
            fullnameTextView = itemView.findViewById(R.id.textnama);
            jumlahTokenTextView = itemView.findViewById(R.id.textjml);
            metodePembayaranTextView = itemView.findViewById(R.id.textmtd);
            pembelianTokenTextView = itemView.findViewById(R.id.textbeli);
        }
        public void bind(RiwayatItem riwayatItem) {
            currentDateTextView.setText(riwayatItem.getCurrentDate());
            fullnameTextView.setText(riwayatItem.getFullname());
            jumlahTokenTextView.setText(riwayatItem.getJumlahToken());
            metodePembayaranTextView.setText(riwayatItem.getMetodePembayaran());
            pembelianTokenTextView.setText(riwayatItem.getPembelianToken());
        }
    }
}

