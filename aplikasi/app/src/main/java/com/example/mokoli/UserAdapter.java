package com.example.mokoli;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private Context context;

    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monitoring, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        TextView noKamarTextView, namaTextView, kwhTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            noKamarTextView = itemView.findViewById(R.id.nokamar);
            namaTextView = itemView.findViewById(R.id.nama);
            kwhTextView = itemView.findViewById(R.id.KWH);
        }

        public void bind(User user) {
            noKamarTextView.setText(user.getNoKamar());
            namaTextView.setText(user.getFullName());
            kwhTextView.setText(String.valueOf(user.getKwh()));
        }
    }
}