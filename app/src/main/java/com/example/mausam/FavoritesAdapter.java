package com.example.mausam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    // One saved city, exactly what the row needs to show
    public static class FavoriteUiItem {
        public final String id;         // the document id (needed to delete it later)
        public final String city;
        public final String condition;
        public final String temp;

        public FavoriteUiItem(String id, String city, String condition, String temp) {
            this.id = id;
            this.city = city;
            this.condition = condition;
            this.temp = temp;
        }
    }

    // Tells the screen "the user tapped delete on this item"
    public interface OnDeleteClickListener {
        void onDeleteClick(FavoriteUiItem item);
    }

    private final List<FavoriteUiItem> items = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;

    public FavoritesAdapter(OnDeleteClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    // Call this whenever you have a new list of favorites
    public void setItems(List<FavoriteUiItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteUiItem item = items.get(position);
        holder.tvCity.setText(item.city);
        holder.tvCondition.setText(item.condition);
        holder.tvTemp.setText(item.temp);
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCity;
        final TextView tvCondition;
        final TextView tvTemp;
        final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCity = itemView.findViewById(R.id.tvFavCity);
            tvCondition = itemView.findViewById(R.id.tvFavCondition);
            tvTemp = itemView.findViewById(R.id.tvFavTemp);
            btnDelete = itemView.findViewById(R.id.btnDeleteFavorite);
        }
    }
}