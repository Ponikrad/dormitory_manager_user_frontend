package com.example.dormmanager.ui.issues;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.Issue;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.IssueViewHolder> {

    private List<Issue> issues;
    private List<Issue> issuesFiltered;
    private List<Issue> issuesPage;
    private Context context;
    private OnIssueClickListener listener;

    public interface OnIssueClickListener {
        void onIssueClick(Issue issue);
    }

    public IssueAdapter(Context context, OnIssueClickListener listener) {
        this.context = context;
        this.issues = new ArrayList<>();
        this.issuesFiltered = new ArrayList<>();
        this.issuesPage = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_issue, parent, false);
        return new IssueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolder holder, int position) {
        Issue issue = issuesPage.get(position);
        holder.bind(issue);
    }

    @Override
    public int getItemCount() {
        return issuesPage.size();
    }

    public int getTotalItemCount() {
        return issuesFiltered.size();
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
        this.issuesFiltered = new ArrayList<>(issues);
        // Don't set issuesPage here - let setPage() handle pagination
        this.issuesPage.clear();
        notifyDataSetChanged();
    }

    public void setPage(int page, int itemsPerPage) {
        issuesPage.clear();
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, issuesFiltered.size());
        
        if (startIndex < issuesFiltered.size()) {
            issuesPage.addAll(issuesFiltered.subList(startIndex, endIndex));
        }
        notifyDataSetChanged();
    }

    public void filter(String status) {
        issuesFiltered.clear();

        if (status == null || status.equals("ALL")) {
            issuesFiltered.addAll(issues);
        } else {
            for (Issue issue : issues) {
                if (issue.getStatus() != null && issue.getStatus().equals(status)) {
                    issuesFiltered.add(issue);
                }
            }
        }

        // Don't set issuesPage here - let setPage() handle pagination
        // Reset to first page by clearing issuesPage
        issuesPage.clear();
        notifyDataSetChanged();
    }

    public void filterByPriority(String priority) {
        issuesFiltered.clear();

        if (priority == null || priority.equals("ALL")) {
            issuesFiltered.addAll(issues);
        } else {
            for (Issue issue : issues) {
                if (issue.getPriority() != null && issue.getPriority().equals(priority)) {
                    issuesFiltered.add(issue);
                }
            }
        }

        issuesPage = new ArrayList<>(issuesFiltered);
        notifyDataSetChanged();
    }

    class IssueViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView tvIcon;
        private TextView tvTitle;
        private TextView tvCategory;
        private TextView tvDescription;
        private TextView tvReportedTime;
        private Chip chipStatus;
        private Chip chipPriority;

        public IssueViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvReportedTime = itemView.findViewById(R.id.tvReportedTime);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            chipPriority = itemView.findViewById(R.id.chipPriority);
        }

        public void bind(Issue issue) {
            // Icon
            tvIcon.setText(issue.getIssueIcon());

            // Title
            tvTitle.setText(issue.getTitle() != null ? issue.getTitle() : "Issue");

            // Category
            tvCategory.setText(issue.getCategoryDisplay() != null ?
                    issue.getCategoryDisplay() : issue.getCategory());

            // Description (short)
            String desc = issue.getDescription();
            if (desc != null && desc.length() > 60) {
                tvDescription.setText(desc.substring(0, 60) + "...");
            } else {
                tvDescription.setText(desc != null ? desc : "No description");
            }

            // Time
            String time = formatDate(issue.getReportedAt());
            tvReportedTime.setText(time);

            // Status chip
            String statusDisplay = issue.getStatusDisplay() != null ?
                    issue.getStatusDisplay() : issue.getStatus();
            chipStatus.setText(statusDisplay);
            chipStatus.setChipBackgroundColorResource(getStatusColorResource(issue.getStatus()));

            // Priority chip
            String priorityDisplay = issue.getPriorityDisplay() != null ?
                    issue.getPriorityDisplay() : issue.getPriority();
            chipPriority.setText(priorityDisplay);
            chipPriority.setChipBackgroundColorResource(getPriorityColorResource(issue.getPriority()));

            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIssueClick(issue);
                }
            });
        }

        private String formatDate(String dateString) {
            if (dateString == null) return "Unknown date";

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat(
                        "MMM dd, yyyy", Locale.getDefault());

                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return dateString.substring(0, Math.min(10, dateString.length()));
            }
        }

        private int getStatusColorResource(String status) {
            if (status == null) return R.color.status_pending;

            switch (status.toUpperCase()) {
                case "RESOLVED":
                    return R.color.status_completed;
                case "IN_PROGRESS":
                case "ACKNOWLEDGED":
                    return R.color.status_pending;
                case "REPORTED":
                    return R.color.status_pending;
                case "CANCELLED":
                case "CLOSED":
                    return R.color.status_failed;
                default:
                    return R.color.status_pending;
            }
        }

        private int getPriorityColorResource(String priority) {
            if (priority == null) return R.color.status_pending;

            switch (priority.toUpperCase()) {
                case "CRITICAL":
                case "URGENT":
                    return R.color.status_failed;
                case "HIGH":
                    return R.color.status_pending;
                case "MEDIUM":
                    return R.color.primary_blue_light;
                case "LOW":
                    return R.color.status_completed;
                default:
                    return R.color.status_pending;
            }
        }
    }
}