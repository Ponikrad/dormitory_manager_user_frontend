package com.example.dormmanager.ui.announcements;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.Announcement;
import com.example.dormmanager.utils.DateTimeUtils;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {

    private List<Announcement> announcements;
    private Context context;
    private OnAnnouncementClickListener listener;

    public interface OnAnnouncementClickListener {
        void onAnnouncementClick(Announcement announcement);
    }

    public AnnouncementAdapter(Context context, OnAnnouncementClickListener listener) {
        this.context = context;
        this.announcements = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        Announcement announcement = announcements.get(position);
        holder.bind(announcement);
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    public void setAnnouncements(List<Announcement> announcements) {
        this.announcements = announcements;
        notifyDataSetChanged();
    }

    class AnnouncementViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView tvIcon;
        private TextView tvTitle;
        private TextView tvType;
        private TextView tvPreview;
        private TextView tvDate;
        private Chip chipPriority;
        private View urgentIndicator;
        private View pinnedIndicator;

        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvType = itemView.findViewById(R.id.tvType);
            tvPreview = itemView.findViewById(R.id.tvPreview);
            tvDate = itemView.findViewById(R.id.tvDate);
            chipPriority = itemView.findViewById(R.id.chipPriority);
            urgentIndicator = itemView.findViewById(R.id.urgentIndicator);
            pinnedIndicator = itemView.findViewById(R.id.pinnedIndicator);
        }

        public void bind(Announcement announcement) {
            tvIcon.setText(announcement.getAnnouncementIcon());

            tvTitle.setText(announcement.getTitle() != null ?
                    announcement.getTitle() : "Announcement");

            tvType.setText(announcement.getTypeDisplay());

            String content = announcement.getContent();
            if (content != null && !content.isEmpty()) {
                String preview = content.length() > 150 ?
                        content.substring(0, 150) + "..." : content;
                tvPreview.setText(preview);
                tvPreview.setVisibility(View.VISIBLE);
            } else {
                tvPreview.setVisibility(View.GONE);
            }

            tvDate.setText(DateTimeUtils.getRelativeTime(announcement.getPublishedAt()));

            if (announcement.getPriority() != null &&
                    !"LOW".equals(announcement.getPriority())) {
                chipPriority.setVisibility(View.VISIBLE);
                chipPriority.setText(announcement.getPriorityIcon() + " " +
                        announcement.getPriority());
                chipPriority.setChipBackgroundColorResource(announcement.getPriorityColor());
            } else {
                chipPriority.setVisibility(View.GONE);
            }

            urgentIndicator.setVisibility(announcement.isUrgent() ?
                    View.VISIBLE : View.GONE);

            pinnedIndicator.setVisibility(announcement.isPinned() ?
                    View.VISIBLE : View.GONE);

            // Reset alpha to default (card color is handled by XML theme)
            cardView.setAlpha(1.0f);

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAnnouncementClick(announcement);
                }
            });
        }
    }
}