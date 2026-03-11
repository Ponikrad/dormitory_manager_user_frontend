package com.example.dormmanager.ui.messages;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.ChatMessage;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {

    private List<ChatMessage> messages;
    private Context context;

    public ChatMessageAdapter(Context context) {
        this.context = context;
        this.messages = new ArrayList<>();
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage message) {
        // Check for duplicates by ID or content + timestamp
        if (message.getId() != null) {
            for (ChatMessage existing : this.messages) {
                if (existing.getId() != null && existing.getId().equals(message.getId())) {
                    // Message already exists, don't add duplicate
                    return;
                }
            }
        } else {
            // If no ID, check by content and timestamp
            for (ChatMessage existing : this.messages) {
                if (existing.getContent() != null && existing.getContent().equals(message.getContent()) &&
                    existing.getSentAt() != null && existing.getSentAt().equals(message.getSentAt()) &&
                    Boolean.TRUE.equals(existing.getIsFromCurrentUser()) == Boolean.TRUE.equals(message.getIsFromCurrentUser())) {
                    // Duplicate message, don't add
                    return;
                }
            }
        }
        
        this.messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    class ChatMessageViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout messageContainer;
        private TextView tvSenderName;
        private MaterialCardView cardMessage;
        private TextView tvMessage;
        private TextView tvTimestamp;

        public ChatMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageContainer = itemView.findViewById(R.id.messageContainer);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            cardMessage = itemView.findViewById(R.id.cardMessage);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        public void bind(ChatMessage message) {
            // Set message content
            tvMessage.setText(message.getContent());

            // Set timestamp
            tvTimestamp.setText(formatTime(message.getSentAt()));

            // Adjust layout based on whether it's from current user
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) messageContainer.getLayoutParams();

            if (message.isFromCurrentUser()) {
                // Current user's message - align right, different color
                params.gravity = Gravity.END;
                tvSenderName.setVisibility(View.GONE);
                cardMessage.setCardBackgroundColor(context.getResources().getColor(R.color.primary_blue, null));
                tvMessage.setTextColor(context.getResources().getColor(android.R.color.white, null));
                tvTimestamp.setTextColor(context.getResources().getColor(android.R.color.white, null));
                tvTimestamp.setAlpha(0.7f);
            } else {
                // Other user's message - align left
                params.gravity = Gravity.START;
                tvSenderName.setVisibility(View.VISIBLE);
                tvSenderName.setText(message.getDisplayName());
                cardMessage.setCardBackgroundColor(context.getResources().getColor(R.color.surface_variant, null));
                tvMessage.setTextColor(context.getResources().getColor(R.color.on_surface, null));
                tvTimestamp.setTextColor(context.getResources().getColor(R.color.on_surface_variant, null));
                tvTimestamp.setAlpha(1.0f);
            }

            messageContainer.setLayoutParams(params);
        }

        private String formatTime(String dateString) {
            if (dateString == null) return "";

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat(
                        "HH:mm", Locale.getDefault());

                Date date = inputFormat.parse(dateString);
                
                // Check if it's today
                SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String today = dayFormat.format(new Date());
                String messageDay = dayFormat.format(date);
                
                if (today.equals(messageDay)) {
                    return outputFormat.format(date);
                } else {
                    SimpleDateFormat dateOutputFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                    return dateOutputFormat.format(date);
                }
            } catch (ParseException e) {
                return dateString;
            }
        }
    }
}

