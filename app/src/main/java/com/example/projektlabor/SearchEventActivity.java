package com.example.projektlabor;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class SearchEventActivity extends AppCompatActivity {

    private TextInputEditText editTextSearch;
    private RecyclerView recyclerViewSearchResults;
    private EventAdapter eventAdapter;
    private List<EventActivity.Event> eventList;
    private DatabaseReference eventsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_event_activity);

        mAuth = FirebaseAuth.getInstance();
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        editTextSearch = findViewById(R.id.edit_text_search);
        recyclerViewSearchResults = findViewById(R.id.recycler_view_search_results);

        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(EventActivity.Event event) {
                showEventDetails(event);
            }

            @Override
            public void onEditClick(EventActivity.Event event) {
                if (event.creatorId.equals(mAuth.getCurrentUser().getUid())) {
                    Intent intent = new Intent(SearchEventActivity.this, EditEventActivity.class);
                    intent.putExtra("EVENT_ID", event.eventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(SearchEventActivity.this, "You can only edit your own events", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDeleteClick(EventActivity.Event event) {
                if (event.creatorId.equals(mAuth.getCurrentUser().getUid())) {
                    showDeleteConfirmationDialog(event);
                } else {
                    Toast.makeText(SearchEventActivity.this, "You can only delete your own events", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onJoinClick(EventActivity.Event event) {
                if (!event.isFull()) {
                    String userId = mAuth.getCurrentUser().getUid();
                    eventsRef.child(event.eventId)
                            .child("participants")
                            .child(userId)
                            .setValue(true)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SearchEventActivity.this,
                                            "Successfully joined the event",
                                            Toast.LENGTH_SHORT).show();
                                    searchEvents(editTextSearch.getText().toString());
                                } else {
                                    Toast.makeText(SearchEventActivity.this,
                                            "Failed to join event",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onLeaveClick(EventActivity.Event event) {
                String userId = mAuth.getCurrentUser().getUid();
                eventsRef.child(event.eventId)
                        .child("participants")
                        .child(userId)
                        .removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(SearchEventActivity.this,
                                        "Successfully left the event",
                                        Toast.LENGTH_SHORT).show();
                                searchEvents(editTextSearch.getText().toString());
                            } else {
                                Toast.makeText(SearchEventActivity.this,
                                        "Failed to leave event",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSearchResults.setAdapter(eventAdapter);

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchEvents(s.toString());
            }
        });
    }

    private void searchEvents(String query) {
        Query searchQuery = eventsRef.orderByChild("eventName")
                .startAt(query)
                .endAt(query + "\uf8ff");

        searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventActivity.Event event = snapshot.getValue(EventActivity.Event.class);
                    if (event != null) {
                        eventList.add(event);
                    }
                }
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SearchEventActivity.this, "Search failed: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        creatorText.setText("Created by: " + event.creatorEmail);

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
                Toast.makeText(SearchEventActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                searchEvents(editTextSearch.getText().toString());  // Refresh the search results
            } else {
                Toast.makeText(SearchEventActivity.this, "Failed to delete event: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}