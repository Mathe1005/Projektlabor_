package com.example.projektlabor;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import com.google.android.material.button.MaterialButton;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUserEvents;
    private RecyclerView recyclerViewOtherEvents;
    private EventAdapter userEventAdapter;
    private EventAdapter otherEventAdapter;
    private List<EventActivity.Event> userEventList;
    private List<EventActivity.Event> otherEventList;
    private DatabaseReference mDatabase;
    private DatabaseReference notificationsRef;
    private DatabaseReference friendRequestsRef;
    private FirebaseAuth mAuth;
    private TextView notificationsBadge;
    private TextView friendsBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("events");
        notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
        friendRequestsRef = FirebaseDatabase.getInstance().getReference("friendRequests");

        initializeViews();
        setupEventLists();
        setupBottomNavigation();
        loadEvents();
        setupBadgeListeners();
    }

    private void initializeViews() {
        MaterialButton btnAddEvent = findViewById(R.id.btn_add_event);
        btnAddEvent.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, EventActivity.class)));

        MaterialButton btnSearchEvent = findViewById(R.id.btn_search_event);
        btnSearchEvent.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchEventActivity.class)));

        recyclerViewUserEvents = findViewById(R.id.recycler_view_user_events);
        recyclerViewOtherEvents = findViewById(R.id.recycler_view_other_events);
        recyclerViewUserEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOtherEvents.setLayoutManager(new LinearLayoutManager(this));

        notificationsBadge = findViewById(R.id.notifications_badge);
        friendsBadge = findViewById(R.id.friends_badge);
    }

    private void setupBadgeListeners() {
        String userId = mAuth.getCurrentUser().getUid();

        // Értesítések figyelése
        notificationsRef.child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = 0;
                        for (DataSnapshot notifSnapshot : snapshot.getChildren()) {
                            EventNotification notification = notifSnapshot.getValue(EventNotification.class);
                            if (notification != null) {
                                // Számoljuk a pending meghívókat és az olvasatlan értesítéseket
                                if (notification.getStatus().equals("pending") ||
                                        notification.getStatus().equals("unread")) {
                                    count++;
                                }
                            }
                        }
                        updateNotificationBadge(count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Barát kérelmek figyelése (ez marad változatlan)
        friendRequestsRef.orderByChild("receiverId")
                .equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = 0;
                        for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                            FriendRequest request = requestSnapshot.getValue(FriendRequest.class);
                            if (request != null && request.getStatus().equals("pending")) {
                                count++;
                            }
                        }
                        updateFriendBadge(count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void updateNotificationBadge(long count) {
        if (count > 0) {
            notificationsBadge.setVisibility(View.VISIBLE);
            notificationsBadge.setText(String.valueOf(count));
        } else {
            notificationsBadge.setVisibility(View.GONE);
        }
    }

    private void updateFriendBadge(long count) {
        if (count > 0) {
            friendsBadge.setVisibility(View.VISIBLE);
            friendsBadge.setText(String.valueOf(count));
        } else {
            friendsBadge.setVisibility(View.GONE);
        }
    }

    private void setupBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        navHome.setOnClickListener(v -> recreate());

        LinearLayout navCalendar = findViewById(R.id.nav_calendar);
        navCalendar.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CalendarActivity.class)));

        LinearLayout navNotifications = findViewById(R.id.nav_notifications);
        navNotifications.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, NotificationsActivity.class)));

        LinearLayout navFriends = findViewById(R.id.nav_friends);
        navFriends.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, FriendsActivity.class)));

        LinearLayout navProfile = findViewById(R.id.nav_profile);
        navProfile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
    }

    private void setupEventLists() {
        userEventList = new ArrayList<>();
        otherEventList = new ArrayList<>();

        userEventAdapter = new EventAdapter(userEventList, this);
        otherEventAdapter = new EventAdapter(otherEventList, this);

        recyclerViewUserEvents.setAdapter(userEventAdapter);
        recyclerViewOtherEvents.setAdapter(otherEventAdapter);
    }

    private void showEventDetails(EventActivity.Event event) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_event_details);

        TextView titleText = dialog.findViewById(R.id.dialog_title);
        TextView descriptionText = dialog.findViewById(R.id.dialog_description);
        TextView participantsText = dialog.findViewById(R.id.dialog_participants);
        TextView sportCategoryText = dialog.findViewById(R.id.dialog_sport_category);
        TextView startTimeText = dialog.findViewById(R.id.dialog_start_time);
        TextView creatorText = dialog.findViewById(R.id.dialog_creator);

        titleText.setText(event.eventName);
        descriptionText.setText(event.description);
        sportCategoryText.setText("Sport: " + event.sportCategory);
        startTimeText.setText("Starts at: " + event.startTime);
        creatorText.setText("Created by: " + event.creatorUsername);

        int participantCount = event.participants != null ? event.participants.size() : 0;
        participantsText.setText(String.format("%d/%d participants",
                participantCount,
                event.maxParticipants));

        dialog.show();
    }

    private void loadEvents() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userEventList.clear();
                otherEventList.clear();
                String currentUserId = mAuth.getCurrentUser().getUid();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventActivity.Event event = snapshot.getValue(EventActivity.Event.class);
                    if (event != null) {
                        if (event.creatorId.equals(currentUserId)) {
                            userEventList.add(event);
                        } else {
                            otherEventList.add(event);
                        }
                    }
                }
                userEventAdapter.notifyDataSetChanged();
                otherEventAdapter.notifyDataSetChanged();

                updateEventListsVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, "Failed to load events: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEventListsVisibility() {
        TextView userEventsTitle = findViewById(R.id.text_user_events_title);
        TextView otherEventsTitle = findViewById(R.id.text_other_events_title);

        if (userEventList.isEmpty()) {
            userEventsTitle.setVisibility(View.GONE);
            recyclerViewUserEvents.setVisibility(View.GONE);
        } else {
            userEventsTitle.setVisibility(View.VISIBLE);
            recyclerViewUserEvents.setVisibility(View.VISIBLE);
        }

        if (otherEventList.isEmpty()) {
            otherEventsTitle.setVisibility(View.GONE);
            recyclerViewOtherEvents.setVisibility(View.GONE);
        } else {
            otherEventsTitle.setVisibility(View.VISIBLE);
            recyclerViewOtherEvents.setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteConfirmationDialog(final EventActivity.Event event) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_delete);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView titleText = dialog.findViewById(R.id.dialog_title);
        TextView messageText = dialog.findViewById(R.id.dialog_message);
        Button yesButton = dialog.findViewById(R.id.btn_yes);
        Button noButton = dialog.findViewById(R.id.btn_no);

        titleText.setText("Confirm Deletion");
        messageText.setText("Are you sure you want to delete this event?");

        yesButton.setOnClickListener(v -> {
            deleteEvent(event);
            dialog.dismiss();
        });

        noButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void deleteEvent(EventActivity.Event event) {
        mDatabase.child(event.eventId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(HomeActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomeActivity.this, "Failed to delete event: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}