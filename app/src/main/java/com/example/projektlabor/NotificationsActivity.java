package com.example.projektlabor;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<EventNotification> notifications;
    private FirebaseAuth mAuth;
    private DatabaseReference notificationsRef;
    private DatabaseReference eventsRef;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        mAuth = FirebaseAuth.getInstance();
        notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view_notifications);
        emptyView = findViewById(R.id.empty_view);

        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(notifications, (notification, accepted) -> {
            if (accepted) {
                acceptInvitation(notification);
            } else {
                declineInvitation(notification);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        String userId = mAuth.getCurrentUser().getUid();
        notificationsRef.child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notifications.clear();
                        for (DataSnapshot notifSnapshot : snapshot.getChildren()) {
                            EventNotification notification = notifSnapshot.getValue(EventNotification.class);
                            if (notification != null) {
                                if (notification.getStatus().equals("pending") ||
                                        notification.getStatus().equals("unread")) {
                                    notifications.add(notification);
                                }
                            }
                        }

                        if (notifications.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Kezelj hibát
                    }
                });
    }

    private void acceptInvitation(EventNotification notification) {
        if (notification.getNotificationType() == null ||
                notification.getNotificationType().equals("default")) {
            // Meghívó elfogadása
            notificationsRef.child(mAuth.getCurrentUser().getUid())
                    .child(notification.getNotificationId())
                    .child("status")
                    .setValue("accepted");

            // Résztvevő hozzáadása az eseményhez
            eventsRef.child(notification.getEventId())
                    .child("participants")
                    .child(mAuth.getCurrentUser().getUid())
                    .setValue(true);
        } else {
            // Módosítási/törlési értesítés megjelölése olvasottként és törlése
            markAsRead(notification);
        }
    }

    private void declineInvitation(EventNotification notification) {
        notificationsRef.child(mAuth.getCurrentUser().getUid())
                .child(notification.getNotificationId())
                .child("status")
                .setValue("declined");
    }

    private void markAsRead(EventNotification notification) {
        notificationsRef.child(mAuth.getCurrentUser().getUid())
                .child(notification.getNotificationId())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    adapter.removeNotification(notification);
                });
    }
}