package com.example.mausam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    // One forecast day, exactly what the card needs to show
    public static class ForecastUiItem {
        public final String day;
        public final String icon;
        public final String temp;

        public ForecastUiItem(String day, String icon, String temp) {
            this.day = day;
            this.icon = icon;
            this.temp = temp;
        }
    }

    private final List<ForecastUiItem> items = new ArrayList<>();

    // Call this whenever you have new forecast data
    public void setItems(List<ForecastUiItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForecastUiItem item = items.get(position);
        holder.tvDay.setText(item.day);
        holder.tvIcon.setText(item.icon);
        holder.tvTemp.setText(item.temp);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDay;
        final TextView tvIcon;
        final TextView tvTemp;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvForecastDay);
            tvIcon = itemView.findViewById(R.id.tvForecastIcon);
            tvTemp = itemView.findViewById(R.id.tvForecastTemp);
        }
    }
}