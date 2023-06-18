package com.example.Mais_Maze_Android_App;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter class for populating a RecyclerView with a list of DatabaseRow objects.
 * Provides the necessary view holders and binds the data to the views.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<DatabaseRow> rowDataList;

    /**
     * Constructs a new MyAdapter with the specified list of DatabaseRow objects.
     * @param rowDataList the list of DatabaseRow objects to be displayed
     */
    public MyAdapter(List<DatabaseRow> rowDataList) {
        this.rowDataList = rowDataList;
    }

    /**
     * ViewHolder class representing an item view in the RecyclerView.
     * Holds references to the views contained within each item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView rowTextView;

        /**
         * Constructs a new ViewHolder with the specified item view.
         *
         * @param itemView the item view for the ViewHolder
         */
        public ViewHolder(View itemView) {
            super(itemView);
            rowTextView = itemView.findViewById(R.id.row_text_view);
        }
    }

    /**
     * Called when the RecyclerView needs a new ViewHolder object.
     *
     * @param parent   the ViewGroup into which the new View will be added after it is bound to an adapter position
     * @param viewType the view type of the new View
     * @return a new ViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_layout, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called to bind the data from the DatabaseRow object at the specified position to the ViewHolder.
     *
     * @param holder   the ViewHolder to bind the data to
     * @param position the position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DatabaseRow rowData = rowDataList.get(position);
        holder.rowTextView.setText(rowData.getRowData());
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return the total number of items
     */
    @Override
    public int getItemCount() {
        return rowDataList.size();
    }
}
