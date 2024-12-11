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

    public void removeNotification(EventNotification notification) {
        int position = notifications.indexOf(notification);
        if (position != -1) {
            notifications.remove(position);
            notifyItemRemoved(position);
        }
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private MaterialButton acceptButton;
        private MaterialButton declineButton;
        private MaterialButton okButton;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_notification_message);
            acceptButton = itemView.findViewById(R.id.button_accept);
            declineButton = itemView.findViewById(R.id.button_decline);
            okButton = itemView.findViewById(R.id.button_ok);
        }

        void bind(EventNotification notification) {
            String message;
            switch (notification.getNotificationType()) {
                case "modified":
                case "cancelled":
                case "date_changed":
                case "location_changed":
                case "time_changed":
                    message = getUpdateMessage(notification);
                    messageText.setText(message);
                    acceptButton.setVisibility(View.GONE);
                    declineButton.setVisibility(View.GONE);
                    okButton.setVisibility(View.VISIBLE);
                    okButton.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onNotificationAction(notification, true);
                        }
                    });
                    break;

                default:
                    message = String.format("%s has invited you to join '%s'",
                            notification.getSenderName(), notification.getEventName());
                    messageText.setText(message);
                    acceptButton.setVisibility(View.VISIBLE);
                    declineButton.setVisibility(View.VISIBLE);
                    okButton.setVisibility(View.GONE);

                    acceptButton.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onNotificationAction(notification, true);
                        }
                    });

                    declineButton.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onNotificationAction(notification, false);
                        }
                    });
                    break;
            }
        }

        private String getUpdateMessage(EventNotification notification) {
            switch (notification.getNotificationType()) {
                case "modified":
                    return String.format("%s has modified the event '%s'",
                            notification.getSenderName(), notification.getEventName());
                case "cancelled":
                    return String.format("Event '%s' has been cancelled by %s",
                            notification.getEventName(), notification.getSenderName());
                case "date_changed":
                    return String.format("The date of event '%s' has been changed by %s",
                            notification.getEventName(), notification.getSenderName());
                case "location_changed":
                    return String.format("The location of event '%s' has been changed by %s",
                            notification.getEventName(), notification.getSenderName());
                case "time_changed":
                    return String.format("The start time of event '%s' has been changed by %s",
                            notification.getEventName(), notification.getSenderName());
                default:
                    return "";
            }
        }
    }
}
