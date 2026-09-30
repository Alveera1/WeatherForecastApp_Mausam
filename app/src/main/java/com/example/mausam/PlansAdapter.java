package com.example.mausam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mausam.models.Plan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.ViewHolder> {

    // Tells the screen "the user tapped delete on this plan"
    public interface OnDeleteClickListener {
        void onDeleteClick(Plan plan);
    }

    private final List<Plan> items = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());

    public PlansAdapter(OnDeleteClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    // Call this whenever you have a new list of plans
    public void setItems(List<Plan> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plan plan = items.get(position);
        holder.tvActivity.setText(plan.getActivity());
        holder.tvLocation.setText(plan.getLocation());
        holder.tvDateTime.setText(dateFormat.format(new Date(plan.getDateTime())));
        holder.tvStatus.setText(statusLabel(plan.getStatus()));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(plan));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Turns "pending"/"safe"/"risky" into the emoji label shown on screen
    private String statusLabel(String status) {
        switch (status) {
            case "safe": return "✅ Safe";
            case "risky": return "⚠️ Risky";
            default: return "⏳ Pending";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvActivity;
        final TextView tvLocation;
        final TextView tvDateTime;
        final TextView tvStatus;
        final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActivity = itemView.findViewById(R.id.tvPlanActivity);
            tvLocation = itemView.findViewById(R.id.tvPlanLocation);
            tvDateTime = itemView.findViewById(R.id.tvPlanDateTime);
            tvStatus = itemView.findViewById(R.id.tvPlanStatus);
            btnDelete = itemView.findViewById(R.id.btnDeletePlan);
        }
    }
}