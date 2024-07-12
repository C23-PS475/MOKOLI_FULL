package com.example.mokoli;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.TransaksiViewHolder> {
    private ArrayList<Transaksi> transaksiList;

    public TransaksiAdapter(ArrayList<Transaksi> transaksiList) {
        this.transaksiList = transaksiList;
    }

    @NonNull
    @Override
    public TransaksiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_penyewa, parent, false);
        return new TransaksiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransaksiViewHolder holder, int position) {
        Transaksi transaksi = transaksiList.get(position);
        holder.bind(transaksi);
    }

    @Override
    public int getItemCount() {
        return transaksiList.size();
    }

    public static class TransaksiViewHolder extends RecyclerView.ViewHolder {
        private TextView txtTanggalRequest, txtPembelianToken, txtJumlahToken, txtMetodePembayaran;

        public TransaksiViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTanggalRequest = itemView.findViewById(R.id.texttgl);
            txtPembelianToken = itemView.findViewById(R.id.textbeli);
            txtJumlahToken = itemView.findViewById(R.id.textjml);
            txtMetodePembayaran = itemView.findViewById(R.id.textmtd);
        }
        public void bind(Transaksi transaksi) {
            txtTanggalRequest.setText(transaksi.getCurrentDate());
            txtPembelianToken.setText(transaksi.getPembelianToken());
            txtJumlahToken.setText(transaksi.getJumlahToken());
            txtMetodePembayaran.setText(transaksi.getMetodePembayaran());
        }
    }
}
