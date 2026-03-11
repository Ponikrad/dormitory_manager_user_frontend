package com.example.dormmanager.ui.reservations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.Reservation;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private List<Reservation> reservations;
    private List<Reservation> reservationsFiltered;
    private List<Reservation> reservationsPage; // Current page items
    private Context context;
    private OnReservationClickListener listener;

    public interface OnReservationClickListener {
        void onReservationClick(Reservation reservation);
    }

    public ReservationAdapter(Context context, OnReservationClickListener listener) {
        this.context = context;
        this.reservations = new ArrayList<>();
        this.reservationsFiltered = new ArrayList<>();
        this.reservationsPage = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservationsPage.get(position);
        holder.bind(reservation);
    }

    @Override
    public int getItemCount() {
        return reservationsPage.size();
    }

    public int getTotalItemCount() {
        return reservationsFiltered.size();
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
        this.reservationsFiltered = new ArrayList<>(reservations);
        this.reservationsPage = new ArrayList<>(reservations);
        notifyDataSetChanged();
    }

    public void setPage(int page, int itemsPerPage) {
        reservationsPage.clear();
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, reservationsFiltered.size());
        
        if (startIndex < reservationsFiltered.size()) {
            reservationsPage.addAll(reservationsFiltered.subList(startIndex, endIndex));
        }
        notifyDataSetChanged();
    }

    public void filter(String filter) {
        reservationsFiltered.clear();

        if (filter == null || filter.equals("ALL")) {
            reservationsFiltered.addAll(reservations);
        } else {
            for (Reservation reservation : reservations) {
                boolean matches = false;

                switch (filter.toUpperCase()) {
                    case "UPCOMING":
                        matches = reservation.isUpcoming();
                        break;
                    case "ACTIVE":
                        matches = reservation.isActive();
                        break;
                    case "PAST":
                        matches = "COMPLETED".equalsIgnoreCase(reservation.getStatus()) ||
                                "CANCELLED".equalsIgnoreCase(reservation.getStatus());
                        break;
                }

                if (matches) {
                    reservationsFiltered.add(reservation);
                }
            }
        }

        reservationsPage = new ArrayList<>(reservationsFiltered);
        notifyDataSetChanged();
    }

    class ReservationViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private LinearLayout statusBar;
        private TextView tvDay;
        private TextView tvMonth;
        private TextView tvResourceName;
        private TextView tvResourceDetails;
        private TextView tvTimeSlot;
        private Chip statusChip;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            statusBar = itemView.findViewById(R.id.statusBar);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvResourceName = itemView.findViewById(R.id.tvResourceName);
            tvResourceDetails = itemView.findViewById(R.id.tvResourceDetails);
            tvTimeSlot = itemView.findViewById(R.id.tvTimeSlot);
            statusChip = itemView.findViewById(R.id.statusChip);
        }

        public void bind(Reservation reservation) {
            // Parse date for badge - try multiple formats
            Date date = parseDate(reservation.getStartTime());

            if (date != null) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());

                tvDay.setText(dayFormat.format(date));
                tvMonth.setText(monthFormat.format(date).toUpperCase());
            } else {
                // Fallback - try to extract date from string manually
                String startTime = reservation.getStartTime();
                if (startTime != null && startTime.length() >= 10) {
                    try {
                        String[] dateParts = startTime.substring(0, 10).split("-");
                        if (dateParts.length == 3) {
                            tvDay.setText(dateParts[2]); // Day

                            // Convert month number to name
                            int monthNum = Integer.parseInt(dateParts[1]);
                            String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                                    "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
                            if (monthNum >= 1 && monthNum <= 12) {
                                tvMonth.setText(months[monthNum - 1]);
                            } else {
                                tvMonth.setText("---");
                            }
                        } else {
                            setDefaultDate();
                        }
                    } catch (Exception e) {
                        setDefaultDate();
                    }
                } else {
                    setDefaultDate();
                }
            }

            // Resource name
            tvResourceName.setText(reservation.getResourceName() != null ?
                    reservation.getResourceName() : "Reservation");

            // Resource type & location
            String details = "";
            if (reservation.getResourceType() != null) {
                details = reservation.getResourceType().replace("_", " ");
            }
            if (reservation.getResourceLocation() != null) {
                if (!details.isEmpty()) details += " • ";
                details += reservation.getResourceLocation();
            }
            tvResourceDetails.setText(details);

            // Time slot
            String timeSlot = formatTime(reservation.getStartTime()) + " - " +
                    formatTime(reservation.getEndTime());
            tvTimeSlot.setText(timeSlot);

            // Status chip
            String statusDisplay = reservation.getStatusDisplay() != null ?
                    reservation.getStatusDisplay() : reservation.getStatus();
            statusChip.setText(statusDisplay);

            // Set status bar and chip colors
            int statusColor = getStatusColor(reservation.getStatus());
            statusChip.setChipBackgroundColorResource(statusColor);
            statusBar.setBackgroundResource(statusColor);

            // Click listener for entire card - opens reservation details
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReservationClick(reservation);
                }
            });
        }

        private void setDefaultDate() {
            tvDay.setText("--");
            tvMonth.setText("---");
        }

        private Date parseDate(String dateTimeStr) {
            if (dateTimeStr == null) return null;

            // Try different date formats
            String[] formats = {
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS",
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                    "yyyy-MM-dd"
            };

            for (String format : formats) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                    return sdf.parse(dateTimeStr);
                } catch (ParseException e) {
                    // Try next format
                }
            }

            return null;
        }

        private String formatTime(String dateTimeStr) {
            if (dateTimeStr == null) return "";

            Date date = parseDate(dateTimeStr);
            if (date != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return timeFormat.format(date);
            }

            // Fallback - try to extract time manually
            if (dateTimeStr.contains("T") && dateTimeStr.length() >= 16) {
                return dateTimeStr.substring(11, 16); // Extract HH:mm
            } else if (dateTimeStr.contains(" ") && dateTimeStr.length() >= 16) {
                int spaceIndex = dateTimeStr.indexOf(" ");
                if (spaceIndex + 5 < dateTimeStr.length()) {
                    return dateTimeStr.substring(spaceIndex + 1, spaceIndex + 6);
                }
            }

            return dateTimeStr;
        }

        private int getStatusColor(String status) {
            if (status == null) return R.color.status_pending;

            switch (status.toUpperCase()) {
                case "CONFIRMED":
                    return R.color.status_completed;
                case "CHECKED_IN":
                    return R.color.primary_blue;
                case "COMPLETED":
                    return R.color.status_completed;
                case "PENDING":
                    return R.color.status_pending;
                case "CANCELLED":
                case "REJECTED":
                case "NO_SHOW":
                    return R.color.status_failed;
                default:
                    return R.color.status_pending;
            }
        }
    }
}