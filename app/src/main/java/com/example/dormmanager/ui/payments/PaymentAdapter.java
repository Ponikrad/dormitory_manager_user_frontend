package com.example.dormmanager.ui.payments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.Payment;
import androidx.cardview.widget.CardView;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {

    private List<Payment> payments;
    private List<Payment> paymentsFiltered;
    private List<Payment> paymentsPage;
    private Context context;
    private OnPaymentClickListener listener;

    public interface OnPaymentClickListener {
        void onPaymentClick(Payment payment);
    }

    public PaymentAdapter(Context context, OnPaymentClickListener listener) {
        this.context = context;
        this.payments = new ArrayList<>();
        this.paymentsFiltered = new ArrayList<>();
        this.paymentsPage = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentsPage.get(position);
        holder.bind(payment);
    }

    @Override
    public int getItemCount() {
        return paymentsPage.size();
    }

    public int getTotalItemCount() {
        return paymentsFiltered.size();
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
        this.paymentsFiltered = new ArrayList<>(payments);
        // Don't set paymentsPage here - let setPage() handle pagination
        this.paymentsPage.clear();
        notifyDataSetChanged();
    }

    public void setPage(int page, int itemsPerPage) {
        paymentsPage.clear();
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, paymentsFiltered.size());
        
        if (startIndex < paymentsFiltered.size()) {
            paymentsPage.addAll(paymentsFiltered.subList(startIndex, endIndex));
        }
        notifyDataSetChanged();
    }

    public void filter(String status) {
        paymentsFiltered.clear();

        if (status == null || status.equals("ALL")) {
            paymentsFiltered.addAll(payments);
        } else {
            for (Payment payment : payments) {
                if (payment.getStatus() != null && payment.getStatus().equals(status)) {
                    paymentsFiltered.add(payment);
                }
            }
        }

        // Don't set paymentsPage here - let setPage() handle pagination
        // Reset to first page by clearing paymentsPage
        paymentsPage.clear();
        notifyDataSetChanged();
    }

    class PaymentViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView tvIcon;
        private TextView tvDescription;
        private TextView tvPaymentType;
        private TextView tvDate;
        private TextView tvAmount;
        private Chip chipStatus;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPaymentType = itemView.findViewById(R.id.tvPaymentType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }

        public void bind(Payment payment) {
            // Description
            tvDescription.setText(payment.getDescription() != null ?
                    payment.getDescription() : "Payment");

            // Payment Type
            tvPaymentType.setText(payment.getPaymentType() != null ?
                    payment.getPaymentType() : "GENERAL");

            // Amount
            tvAmount.setText(payment.getFormattedAmount());

            // Date
            String date = formatDate(payment.getCreatedAt());
            tvDate.setText(date);

            // Icon based on payment type
            String icon = getPaymentIcon(payment.getPaymentType());
            tvIcon.setText(icon);

            // Status chip
            String status = payment.getStatusDisplay() != null ?
                    payment.getStatusDisplay() : payment.getStatus();
            chipStatus.setText(status);
            chipStatus.setChipBackgroundColorResource(getStatusColor(payment.getStatus()));

            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentClick(payment);
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

        private String getPaymentIcon(String paymentType) {
            if (paymentType == null) return "💳";

            switch (paymentType.toUpperCase()) {
                case "RENT":
                    return "🏠";
                case "UTILITIES":
                    return "💡";
                case "DEPOSIT":
                    return "💰";
                case "FINE":
                    return "⚠️";
                default:
                    return "💳";
            }
        }

        private int getStatusColor(String status) {
            if (status == null) return R.color.status_pending;

            switch (status.toUpperCase()) {
                case "COMPLETED":
                    return R.color.status_completed;
                case "PENDING":
                case "PROCESSING":
                    return R.color.status_pending;
                case "FAILED":
                case "CANCELLED":
                    return R.color.status_failed;
                default:
                    return R.color.status_pending;
            }
        }
    }
}