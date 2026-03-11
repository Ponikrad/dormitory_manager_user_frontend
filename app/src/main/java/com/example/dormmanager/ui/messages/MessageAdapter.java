package com.example.dormmanager.ui.messages;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.Message;
import com.example.dormmanager.utils.DateTimeUtils;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> allMessages;
    private List<Message> messages; // Current page messages
    private Context context;
    private OnMessageClickListener listener;

    public interface OnMessageClickListener {
        void onMessageClick(Message message);
    }

    public MessageAdapter(Context context, OnMessageClickListener listener) {
        this.context = context;
        this.allMessages = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public int getTotalItemCount() {
        return allMessages.size();
    }

    public void setMessages(List<Message> messages) {
        this.allMessages = new ArrayList<>(messages);
        // Don't set messages here - let setPage() handle pagination
        // Clear messages so setPage() can populate first page
        this.messages.clear();
        notifyDataSetChanged();
    }

    public void setPage(int page, int itemsPerPage) {
        messages.clear();
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allMessages.size());
        
        if (startIndex < allMessages.size()) {
            messages.addAll(allMessages.subList(startIndex, endIndex));
        }
        notifyDataSetChanged();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView tvAvatar;  // ZMIENIONE z tvIcon
        private TextView tvSubject;
        private TextView tvSender;  // Dodane - to jest w contentContainer
        private TextView tvPreview;
        private TextView tvTime;    // ZMIENIONE z tvDate
        private Chip chipImportant; // Dodane
        private View unreadIndicator;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvPreview = itemView.findViewById(R.id.tvPreview);
            tvTime = itemView.findViewById(R.id.tvTime);
            chipImportant = itemView.findViewById(R.id.chipImportant);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }

        public void bind(Message message) {
            // Avatar - pierwsza litera nadawcy
            String senderName = message.getSenderName() != null ?
                    message.getSenderName() : "Administration";
            tvAvatar.setText(senderName.substring(0, 1).toUpperCase());

            // Subject
            String subject = message.getSubject() != null ? message.getSubject() : "No Subject";
            tvSubject.setText(subject);

            // Sender
            tvSender.setText(senderName);

            // Preview
            String content = message.getContent();
            if (content != null && !content.isEmpty()) {
                String preview = content.length() > 100 ?
                        content.substring(0, 100) + "..." : content;
                tvPreview.setText(preview);
                tvPreview.setVisibility(View.VISIBLE);
            } else {
                tvPreview.setVisibility(View.GONE);
            }

            // Time
            tvTime.setText(DateTimeUtils.getRelativeTime(message.getSentAt()));

            // Important badge
            if (message.isUrgent() || (message.getPriority() != null && message.getPriority() > 3)) {
                chipImportant.setVisibility(View.VISIBLE);
                chipImportant.setText("Important");
            } else {
                chipImportant.setVisibility(View.GONE);
            }

            // Unread indicator and styling
            if (message.isUnread()) {
                unreadIndicator.setVisibility(View.VISIBLE);
                tvSubject.setTypeface(null, Typeface.BOLD);
                tvSender.setTypeface(null, Typeface.BOLD);
                cardView.setAlpha(1.0f);
            } else {
                unreadIndicator.setVisibility(View.GONE);
                tvSubject.setTypeface(null, Typeface.NORMAL);
                tvSender.setTypeface(null, Typeface.NORMAL);
                cardView.setAlpha(0.8f);
            }

            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMessageClick(message);
                }
            });
        }
    }
}