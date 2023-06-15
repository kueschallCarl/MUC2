package com.example.menu_template;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<DatabaseRow> rowDataList;

    public MyAdapter(List<DatabaseRow> rowDataList) {
        this.rowDataList = rowDataList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView playerNameTextView;
        public TextView timeTextView;
        public TextView scoreTextView;
        public TextView maisCountTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            playerNameTextView = itemView.findViewById(R.id.player_name_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            scoreTextView = itemView.findViewById(R.id.score_text_view);
            maisCountTextView = itemView.findViewById(R.id.mais_count_text_view);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DatabaseRow rowData = rowDataList.get(position);
        holder.playerNameTextView.setText(rowData.getPlayerName());
        holder.timeTextView.setText(rowData.getTime());
        holder.scoreTextView.setText(rowData.getScore());
        holder.maisCountTextView.setText(rowData.getMaisCount());
    }

    @Override
    public int getItemCount() {
        return rowDataList.size();
    }
}
