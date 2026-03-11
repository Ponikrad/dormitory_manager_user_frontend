package com.example.dormmanager.ui.reservations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.ReservableResource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder> {

    private List<ReservableResource> resources;
    private List<ReservableResource> resourcesFiltered;
    private Context context;
    private OnResourceClickListener listener;

    public interface OnResourceClickListener {
        void onResourceClick(ReservableResource resource);
    }

    public ResourceAdapter(Context context, OnResourceClickListener listener) {
        this.context = context;
        this.resources = new ArrayList<>();
        this.resourcesFiltered = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resource, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        ReservableResource resource = resourcesFiltered.get(position);
        holder.bind(resource);
    }

    @Override
    public int getItemCount() {
        return resourcesFiltered.size();
    }

    public void setResources(List<ReservableResource> resources) {
        this.resources = resources;
        this.resourcesFiltered = new ArrayList<>(resources);
        notifyDataSetChanged();
    }

    public void filter(String type) {
        resourcesFiltered.clear();

        if (type == null || type.equals("ALL")) {
            resourcesFiltered.addAll(resources);
        } else {
            for (ReservableResource resource : resources) {
                if (resource.getResourceType() != null &&
                        resource.getResourceType().equalsIgnoreCase(type)) {
                    resourcesFiltered.add(resource);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void search(String query) {
        resourcesFiltered.clear();

        if (query == null || query.trim().isEmpty()) {
            resourcesFiltered.addAll(resources);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ReservableResource resource : resources) {
                if ((resource.getName() != null &&
                        resource.getName().toLowerCase().contains(lowerQuery)) ||
                        (resource.getDescription() != null &&
                                resource.getDescription().toLowerCase().contains(lowerQuery)) ||
                        (resource.getLocation() != null &&
                                resource.getLocation().toLowerCase().contains(lowerQuery))) {
                    resourcesFiltered.add(resource);
                }
            }
        }

        notifyDataSetChanged();
    }

    class ResourceViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView tvIcon;
        private TextView tvResourceName;
        private TextView tvResourceType;
        private TextView tvLocation;
        private TextView tvCapacity;
        private Chip chipAvailability;
        private MaterialButton actionButton;

        public ResourceViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvResourceName = itemView.findViewById(R.id.tvResourceName);
            tvResourceType = itemView.findViewById(R.id.tvResourceType);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            chipAvailability = itemView.findViewById(R.id.chipAvailability);
            actionButton = itemView.findViewById(R.id.actionButton);
        }

        public void bind(ReservableResource resource) {
            // Icon
            tvIcon.setText(resource.getResourceIcon());

            // Resource name
            tvResourceName.setText(resource.getName() != null ? resource.getName() : "Resource");

            // Resource type
            if (resource.getResourceType() != null) {
                tvResourceType.setText(resource.getResourceTypeDisplay());
            } else {
                tvResourceType.setText("");
            }

            // Location
            String location = resource.getDisplayLocation();
            if (location != null && !location.isEmpty()) {
                tvLocation.setText(location);
                tvLocation.setVisibility(View.VISIBLE);
            } else {
                tvLocation.setVisibility(View.GONE);
            }

            // Capacity
            if (resource.getCapacity() != null) {
                tvCapacity.setText("👥 " + resource.getCapacity());
                tvCapacity.setVisibility(View.VISIBLE);
            } else {
                tvCapacity.setVisibility(View.GONE);
            }

            // Availability chip
            if (resource.isActive()) {
                chipAvailability.setText("Available");
                chipAvailability.setChipBackgroundColorResource(R.color.status_completed);
            } else {
                chipAvailability.setText("Unavailable");
                chipAvailability.setChipBackgroundColorResource(R.color.status_failed);
            }

            // Action button click
            actionButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onResourceClick(resource);
                }
            });

            // Card click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onResourceClick(resource);
                }
            });

            // Dim if not active
            if (!resource.isActive()) {
                cardView.setAlpha(0.5f);
                actionButton.setEnabled(false);
            } else {
                cardView.setAlpha(1.0f);
                actionButton.setEnabled(true);
            }
        }
    }
}