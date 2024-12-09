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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FilteredEventsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewEvents;
    private EventAdapter eventAdapter;
    private List<EventActivity.Event> eventList;
    private DatabaseReference eventsRef;
    private FirebaseAuth mAuth;
    private TextView noEventsText;

    private String searchQuery = "";
    private String selectedSport = "";
    private String selectedDate = "";
    private boolean showOnlyAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_events);

        // Get filter parameters from intent
        searchQuery = getIntent().getStringExtra("searchQuery").toLowerCase();
        selectedSport = getIntent().getStringExtra("selectedSport");
        selectedDate = getIntent().getStringExtra("selectedDate");
        showOnlyAvailable = getIntent().getBooleanExtra("showOnlyAvailable", false);

        initializeViews();
        setupEventsList();
        loadFilteredEvents();
    }

    private void initializeViews() {
        mAuth = FirebaseAuth.getInstance();
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        recyclerViewEvents = findViewById(R.id.recycler_view_filtered_events);
        noEventsText = findViewById(R.id.text_no_events);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupEventsList() {
        eventList = new ArrayList<>();
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(eventList, this);
        recyclerViewEvents.setAdapter(eventAdapter);

        mAuth = FirebaseAuth.getInstance();
        eventsRef = FirebaseDatabase.getInstance().getReference("events");
    }

    private void loadFilteredEvents() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventActivity.Event event = snapshot.getValue(EventActivity.Event.class);
                    if (event != null && matchesFilters(event)) {
                        eventList.add(event);
                    }
                }

                if (eventList.isEmpty()) {
                    noEventsText.setVisibility(View.VISIBLE);
                    recyclerViewEvents.setVisibility(View.GONE);
                } else {
                    noEventsText.setVisibility(View.GONE);
                    recyclerViewEvents.setVisibility(View.VISIBLE);
                }

                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FilteredEventsActivity.this,
                        "Failed to load events: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean matchesFilters(EventActivity.Event event) {
        // Keresési szöveg ellenőrzése
        boolean matchesSearch = searchQuery.isEmpty() ||
                event.eventName.toLowerCase().contains(searchQuery) ||
                event.eventLocation.toLowerCase().contains(searchQuery) ||
                event.sportCategory.toLowerCase().contains(searchQuery) ||
                event.description.toLowerCase().contains(searchQuery) ||
                event.creatorUsername.toLowerCase().contains(searchQuery);

        // Sport kategória ellenőrzése
        boolean matchesSport = selectedSport.isEmpty() ||
                selectedSport.equals("All") ||
                event.sportCategory.equals(selectedSport);

        // Dátum ellenőrzése
        boolean matchesDate = selectedDate.isEmpty() ||
                event.eventTime.equals(selectedDate);

        // Elérhetőség ellenőrzése
        boolean matchesAvailability = !showOnlyAvailable ||
                !event.isFull();

        return matchesSearch && matchesSport && matchesDate && matchesAvailability;
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
        eventsRef.child(event.eventId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(FilteredEventsActivity.this,
                        "Event deleted successfully",
                        Toast.LENGTH_SHORT).show();
                loadFilteredEvents();
            } else {
                Toast.makeText(FilteredEventsActivity.this,
                        "Failed to delete event: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}