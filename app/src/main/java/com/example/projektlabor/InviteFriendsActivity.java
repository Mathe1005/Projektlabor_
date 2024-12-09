package com.example.projektlabor;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InviteFriendsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewFriends;
    private InviteFriendsAdapter adapter;
    private List<User> friendsList;
    private DatabaseReference usersRef;
    private DatabaseReference notificationsRef;
    private FirebaseAuth mAuth;
    private String eventId;
    private EventActivity.Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        // Inicializáljuk a Firebase referenciákat
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");

        // Az eventId-t megkapjuk az Intent-ből
        eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null) {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Betöltjük az aktuális esemény adatait
        FirebaseDatabase.getInstance().getReference("events").child(eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentEvent = snapshot.getValue(EventActivity.Event.class);
                        if (currentEvent == null) {
                            Toast.makeText(InviteFriendsActivity.this,
                                    "Error loading event details", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(InviteFriendsActivity.this,
                                "Error loading event details", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

        initializeViews();
        setupRecyclerView();
        loadFriends();
    }

    private void initializeViews() {
        recyclerViewFriends = findViewById(R.id.recycler_view_invite_friends);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        friendsList = new ArrayList<>();
        adapter = new InviteFriendsAdapter(friendsList, new InviteFriendsAdapter.OnInviteClickListener() {
            @Override
            public void onInviteClick(User friend) {
                inviteFriend(friend, currentEvent);
            }
        });
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFriends.setAdapter(adapter);
    }

    private void loadFriends() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        usersRef.child(currentUserId).child("friends")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        friendsList.clear();
                        for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                            String friendId = friendSnapshot.getKey();
                            if (friendId != null) {
                                loadFriendDetails(friendId);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(InviteFriendsActivity.this,
                                "Error loading friends list", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadFriendDetails(String friendId) {
        usersRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User friend = dataSnapshot.getValue(User.class);
                if (friend != null) {
                    friendsList.add(friend);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InviteFriendsActivity.this,
                        "Error loading friend details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void inviteFriend(User friend, EventActivity.Event event) {
        String notificationId = notificationsRef.child(friend.getUserId()).push().getKey();
        if (notificationId != null) {
            EventNotification notification = new EventNotification(
                    notificationId,
                    event.eventId,
                    mAuth.getCurrentUser().getUid(),
                    mAuth.getCurrentUser().getDisplayName(),
                    friend.getUserId(),
                    event.eventName
            );

            notificationsRef.child(friend.getUserId())
                    .child(notificationId)
                    .setValue(notification)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(InviteFriendsActivity.this,
                                "Invitation sent successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(InviteFriendsActivity.this,
                                "Failed to send invitation", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}