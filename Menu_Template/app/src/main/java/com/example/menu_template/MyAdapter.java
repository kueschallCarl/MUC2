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
        public TextView rowTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            rowTextView = itemView.findViewById(R.id.row_text_view);
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
        holder.rowTextView.setText(rowData.getRowData());
    }

    @Override
    public int getItemCount() {
        return rowDataList.size();
    }
}
