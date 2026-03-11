package com.example.dormmanager.ui.keys;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.KeyAssignment;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KeyAssignmentAdapter extends RecyclerView.Adapter<KeyAssignmentAdapter.KeyAssignmentViewHolder> {

    private List<KeyAssignment> keyAssignments;
    private List<KeyAssignment> keyAssignmentsFiltered;
    private List<KeyAssignment> keyAssignmentsPage;
    private Context context;
    private OnKeyAssignmentClickListener listener;

    public interface OnKeyAssignmentClickListener {
        void onKeyAssignmentClick(KeyAssignment assignment);
    }

    public KeyAssignmentAdapter(Context context, OnKeyAssignmentClickListener listener) {
        this.context = context;
        this.keyAssignments = new ArrayList<>();
        this.keyAssignmentsFiltered = new ArrayList<>();
        this.keyAssignmentsPage = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public KeyAssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_key_assignment, parent, false);
        return new KeyAssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KeyAssignmentViewHolder holder, int position) {
        KeyAssignment assignment = keyAssignmentsPage.get(position);
        holder.bind(assignment);
    }

    @Override
    public int getItemCount() {
        return keyAssignmentsPage.size();
    }

    public int getTotalItemCount() {
        return keyAssignmentsFiltered.size();
    }

    public void setKeyAssignments(List<KeyAssignment> assignments) {
        this.keyAssignments = assignments;
        this.keyAssignmentsFiltered = new ArrayList<>(assignments);
        // Don't set keyAssignmentsPage here - let setPage() handle pagination
        this.keyAssignmentsPage.clear();
        notifyDataSetChanged();
    }

    public void setPage(int page, int itemsPerPage) {
        keyAssignmentsPage.clear();
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, keyAssignmentsFiltered.size());
        
        if (startIndex < keyAssignmentsFiltered.size()) {
            keyAssignmentsPage.addAll(keyAssignmentsFiltered.subList(startIndex, endIndex));
        }
        notifyDataSetChanged();
    }

    public void filter(String filter) {
        keyAssignmentsFiltered.clear();

        if (filter == null || filter.equals("ALL")) {
            keyAssignmentsFiltered.addAll(keyAssignments);
        } else {
            for (KeyAssignment assignment : keyAssignments) {
                boolean matches = false;

                switch (filter.toUpperCase()) {
                    case "ACTIVE":
                        matches = assignment.isActive();
                        break;
                    case "RETURNED":
                        matches = assignment.isReturned();
                        break;
                    case "LOST":
                        matches = assignment.isLost();
                        break;
                    case "OVERDUE":
                        matches = assignment.isOverdue();
                        break;
                }

                if (matches) {
                    keyAssignmentsFiltered.add(assignment);
                }
            }
        }

        // Don't set keyAssignmentsPage here - let setPage() handle pagination
        // Reset to first page by clearing keyAssignmentsPage
        keyAssignmentsPage.clear();
        notifyDataSetChanged();
    }

    class KeyAssignmentViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView tvIcon;
        private TextView tvKeyCode;
        private TextView tvKeyDescription;
        private TextView tvAssignmentType;
        private TextView tvIssuedDate;
        private TextView tvExpectedReturn;
        private TextView tvWarning;
        private Chip chipStatus;

        public KeyAssignmentViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvKeyCode = itemView.findViewById(R.id.tvKeyCode);
            tvKeyDescription = itemView.findViewById(R.id.tvKeyDescription);
            tvAssignmentType = itemView.findViewById(R.id.tvAssignmentType);
            tvIssuedDate = itemView.findViewById(R.id.tvIssuedDate);
            tvExpectedReturn = itemView.findViewById(R.id.tvExpectedReturn);
            tvWarning = itemView.findViewById(R.id.tvWarning);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }

        public void bind(KeyAssignment assignment) {
            // Icon
            tvIcon.setText(assignment.getKeyIcon());

            // Key code
            tvKeyCode.setText(assignment.getKeyCode() != null ?
                    assignment.getKeyCode() : "Key");

            // Key description
            if (assignment.getKeyDescription() != null &&
                    !assignment.getKeyDescription().isEmpty()) {
                tvKeyDescription.setText(assignment.getKeyDescription());
                tvKeyDescription.setVisibility(View.VISIBLE);
            } else {
                tvKeyDescription.setVisibility(View.GONE);
            }

            // Assignment type
            tvAssignmentType.setText(assignment.getAssignmentTypeDisplay());

            // Issued date
            String issuedDate = formatDate(assignment.getIssuedAt());
            tvIssuedDate.setText("Issued: " + issuedDate);

            // Expected return
            if (assignment.getExpectedReturn() != null && !assignment.isPermanent()) {
                String returnDate = formatDate(assignment.getExpectedReturn());
                tvExpectedReturn.setText("Return by: " + returnDate);
                tvExpectedReturn.setVisibility(View.VISIBLE);
            } else if (assignment.isPermanent()) {
                tvExpectedReturn.setText("Permanent assignment");
                tvExpectedReturn.setVisibility(View.VISIBLE);
            } else {
                tvExpectedReturn.setVisibility(View.GONE);
            }

            // Warning for overdue
            if (assignment.isOverdue() && assignment.isActive()) {
                tvWarning.setVisibility(View.VISIBLE);
                if (assignment.getDaysOverdue() != null) {
                    tvWarning.setText("⚠️ OVERDUE by " + assignment.getDaysOverdue() + " days");
                } else {
                    tvWarning.setText("⚠️ OVERDUE");
                }
                tvWarning.setTextColor(context.getResources().getColor(R.color.error, null));
            } else if (assignment.isLost()) {
                tvWarning.setVisibility(View.VISIBLE);
                tvWarning.setText("🔴 KEY LOST");
                tvWarning.setTextColor(context.getResources().getColor(R.color.error, null));
            } else {
                tvWarning.setVisibility(View.GONE);
            }

            // Status chip
            String statusDisplay = assignment.getStatusDisplay();
            chipStatus.setText(statusDisplay);
            chipStatus.setChipBackgroundColorResource(assignment.getStatusColor());

            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onKeyAssignmentClick(assignment);
                }
            });
        }

        private String formatDate(String dateStr) {
            if (dateStr == null) return "Unknown";

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat(
                        "MMM dd, yyyy", Locale.getDefault());

                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return dateStr.substring(0, Math.min(10, dateStr.length()));
            }
        }
    }
}