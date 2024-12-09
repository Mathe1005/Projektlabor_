package com.example.projektlabor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<EventNotification> notifications;
    private OnNotificationActionListener listener;

    public interface OnNotificationActionListener {
        void onNotificationAction(EventNotification notification, boolean accepted);
    }

    public NotificationAdapter(List<EventNotification> notifications, OnNotificationActionListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private MaterialButton acceptButton;
        private MaterialButton declineButton;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_notification_message);
            acceptButton = itemView.findViewById(R.id.button_accept);
            declineButton = itemView.findViewById(R.id.button_decline);
        }

        void bind(EventNotification notification) {
            String message = String.format("%s has invited you to join '%s'",
                    notification.getSenderName(), notification.getEventName());
            messageText.setText(message);

            acceptButton.setOnClickListener(v -> {
                listener.onNotificationAction(notification, true);
            });

            declineButton.setOnClickListener(v -> {
                listener.onNotificationAction(notification, false);
            });
        }
    }
}