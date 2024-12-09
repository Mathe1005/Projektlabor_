package com.example.projektlabor;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class EventDetailsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String eventId;
    private EventActivity.Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null) {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("events");
        mAuth = FirebaseAuth.getInstance();

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        loadEventDetails();
    }

    private void loadEventDetails() {
        mDatabase.child(eventId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentEvent = snapshot.getValue(EventActivity.Event.class);
                if (currentEvent != null) {
                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EventDetailsActivity.this,
                        "Error loading event details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        TextView eventName = findViewById(R.id.text_event_name);
        TextView eventLocation = findViewById(R.id.text_event_location);
        TextView eventTime = findViewById(R.id.text_event_time);
        TextView creatorEmail = findViewById(R.id.text_creator_email);
        TextView sportCategory = findViewById(R.id.text_sport_category);
        TextView participantsCount = findViewById(R.id.text_participants_count);
        TextView description = findViewById(R.id.text_description);
        TextView startTime = findViewById(R.id.text_start_time);

        MaterialButton editButton = findViewById(R.id.button_edit_event);
        MaterialButton deleteButton = findViewById(R.id.button_delete_event);
        MaterialButton joinButton = findViewById(R.id.button_join_event);
        MaterialButton inviteButton = findViewById(R.id.button_invite_friends);

        // Alap információk beállítása
        eventName.setText(currentEvent.eventName);
        eventLocation.setText(currentEvent.eventLocation);
        eventTime.setText(currentEvent.eventTime);
        creatorEmail.setText("Created by: " + currentEvent.creatorUsername);
        sportCategory.setText("Sport: " + currentEvent.sportCategory);
        description.setText(currentEvent.description);
        startTime.setText("Starts at: " + currentEvent.startTime);

        int participantCount = currentEvent.participants != null ?
                currentEvent.participants.size() : 0;
        participantsCount.setText(String.format("%d/%d participants",
                participantCount, currentEvent.maxParticipants));

        String currentUserId = mAuth.getCurrentUser().getUid();
        boolean isCreator = currentEvent.creatorId.equals(currentUserId);

        // Gombok láthatóságának és működésének beállítása
        editButton.setVisibility(isCreator ? View.VISIBLE : View.GONE);
        deleteButton.setVisibility(isCreator ? View.VISIBLE : View.GONE);
        joinButton.setVisibility(!isCreator ? View.VISIBLE : View.GONE);
        inviteButton.setVisibility(isCreator ? View.VISIBLE : View.GONE);

        if (!isCreator) {
            if (currentEvent.isParticipant(currentUserId)) {
                joinButton.setText("Leave Event");
            } else if (currentEvent.isFull()) {
                joinButton.setText("Event Full");
                joinButton.setEnabled(false);
            } else {
                joinButton.setText("Join Event");
                joinButton.setEnabled(true);
            }
        }

        // Gombok eseménykezelőinek beállítása
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, EditEventActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });

        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        joinButton.setOnClickListener(v -> {
            if (currentEvent.isParticipant(currentUserId)) {
                leaveEvent();
            } else {
                joinEvent();
            }
        });

        inviteButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, InviteFriendsActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });
    }

    private void joinEvent() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child(eventId)
                .child("participants")
                .child(userId)
                .setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EventDetailsActivity.this,
                                "Successfully joined the event",
                                Toast.LENGTH_SHORT).show();
                        // Ütemezzük be az értesítést az esemény előttre
                        NotificationService.scheduleEventNotification(this, currentEvent);
                    } else {
                        Toast.makeText(EventDetailsActivity.this,
                                "Failed to join event",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void leaveEvent() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child(eventId)
                .child("participants")
                .child(userId)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EventDetailsActivity.this,
                                "Successfully left the event",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EventDetailsActivity.this,
                                "Failed to leave event",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeleteConfirmationDialog() {
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
            deleteEvent();
            dialog.dismiss();
        });

        noButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void deleteEvent() {
        // Először értesítjük a résztvevőket
        if (currentEvent.participants != null) {
            for (String participantId : currentEvent.participants.keySet()) {
                NotificationService.sendEventUpdateNotification(
                        EventDetailsActivity.this,
                        currentEvent.eventName,
                        "cancelled"
                );
            }
        }

        // Majd töröljük az eseményt
        mDatabase.child(eventId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EventDetailsActivity.this,
                        "Event deleted successfully",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EventDetailsActivity.this,
                        "Failed to delete event: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}