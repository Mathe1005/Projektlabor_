package com.example.projektlabor;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class EditEventActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private TextInputEditText eventNameEditText, eventLocationEditText, eventTimeEditText;
    private TextInputEditText maxParticipantsEditText, descriptionEditText, startTimeEditText;
    private AutoCompleteTextView sportCategorySpinner;
    private Button updateEventButton;
    private String eventId;
    private EventActivity.Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        mDatabase = FirebaseDatabase.getInstance().getReference("events");
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupSportCategorySpinner();

        eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null) {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEventData();
    }

    private void initializeViews() {
        eventNameEditText = findViewById(R.id.edit_event_name);
        eventLocationEditText = findViewById(R.id.edit_event_location);
        eventTimeEditText = findViewById(R.id.edit_event_time);
        sportCategorySpinner = findViewById(R.id.edit_sport_category_spinner);
        maxParticipantsEditText = findViewById(R.id.edit_max_participants);
        descriptionEditText = findViewById(R.id.edit_event_description);
        startTimeEditText = findViewById(R.id.edit_event_start_time);
        updateEventButton = findViewById(R.id.update_event_button);

        // Dátumválasztó beállítása
        eventTimeEditText.setFocusable(false);
        eventTimeEditText.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        String formattedDate = String.format("%d.%02d.%02d", year, month + 1, dayOfMonth);
                        eventTimeEditText.setText(formattedDate);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        // Időválasztó beállítása
        startTimeEditText.setFocusable(false);
        startTimeEditText.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        String formattedTime = String.format("%02d:%02d", hourOfDay, minute);
                        startTimeEditText.setText(formattedTime);
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true // 24 órás formátum
            );
            timePickerDialog.show();
        });

        updateEventButton.setOnClickListener(v -> updateEvent());
    }

    private void setupSportCategorySpinner() {
        String[] sports = new String[]{
                "Running", "Cycling", "Swimming", "Football", "Basketball",
                "Tennis", "Volleyball", "Hiking", "Yoga", "Other"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                sports
        );
        sportCategorySpinner.setAdapter(adapter);
    }

    private void loadEventData() {
        mDatabase.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentEvent = dataSnapshot.getValue(EventActivity.Event.class);
                if (currentEvent != null) {
                    eventNameEditText.setText(currentEvent.eventName);
                    eventLocationEditText.setText(currentEvent.eventLocation);
                    eventTimeEditText.setText(currentEvent.eventTime);
                    sportCategorySpinner.setText(currentEvent.sportCategory);
                    maxParticipantsEditText.setText(String.valueOf(currentEvent.maxParticipants));
                    descriptionEditText.setText(currentEvent.description);
                    startTimeEditText.setText(currentEvent.startTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditEventActivity.this, "Failed to load event data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Az updateEvent() metódusban módosítani kell az Event objektum létrehozását:

    private void updateEvent() {
        if (currentEvent == null) {
            Toast.makeText(this, "Error: Event data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = eventNameEditText.getText().toString().trim();
        String newLocation = eventLocationEditText.getText().toString().trim();
        String newTime = eventTimeEditText.getText().toString().trim();
        String newSportCategory = sportCategorySpinner.getText().toString();
        String newMaxParticipantsStr = maxParticipantsEditText.getText().toString().trim();
        String newDescription = descriptionEditText.getText().toString().trim();
        String newStartTime = startTimeEditText.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(newName)) {
            eventNameEditText.setError("Please enter event name");
            return;
        }
        if (TextUtils.isEmpty(newLocation)) {
            eventLocationEditText.setError("Please enter event location");
            return;
        }
        if (TextUtils.isEmpty(newTime)) {
            eventTimeEditText.setError("Please enter event date");
            return;
        }
        if (TextUtils.isEmpty(newSportCategory)) {
            sportCategorySpinner.setError("Please select a sport category");
            return;
        }
        if (TextUtils.isEmpty(newMaxParticipantsStr)) {
            maxParticipantsEditText.setError("Please enter maximum participants");
            return;
        }
        if (TextUtils.isEmpty(newDescription)) {
            descriptionEditText.setError("Please enter event description");
            return;
        }
        if (TextUtils.isEmpty(newStartTime)) {
            startTimeEditText.setError("Please enter start time");
            return;
        }

        int newMaxParticipants;
        try {
            newMaxParticipants = Integer.parseInt(newMaxParticipantsStr);
            if (newMaxParticipants <= 0) {
                maxParticipantsEditText.setError("Number must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            maxParticipantsEditText.setError("Please enter a valid number");
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();
        if (!currentEvent.creatorId.equals(currentUserId)) {
            Toast.makeText(this, "You don't have permission to edit this event", Toast.LENGTH_SHORT).show();
            return;
        }

        // Az új konstruktor használata, megtartva az eredeti isPrivate értéket
        EventActivity.Event updatedEvent = new EventActivity.Event(
                eventId,
                newName,
                newLocation,
                newTime,
                currentEvent.creatorId,
                currentEvent.creatorUsername,
                newSportCategory,
                newMaxParticipants,
                newDescription,
                newStartTime,
                currentEvent.isPrivate  // megtartjuk az eredeti isPrivate értéket
        );

        // Megtartjuk a jelenlegi résztvevőket és meghívottakat
        updatedEvent.participants = currentEvent.participants;
        updatedEvent.invitedUsers = currentEvent.invitedUsers;

        mDatabase.child(eventId).setValue(updatedEvent).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Értesítjük a résztvevőket a módosításról
                if (currentEvent.participants != null) {
                    for (String participantId : currentEvent.participants.keySet()) {
                        NotificationService.sendEventUpdateNotification(
                                EditEventActivity.this,
                                updatedEvent.eventName,
                                "modified"
                        );
                    }
                }
                Toast.makeText(EditEventActivity.this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditEventActivity.this, "Failed to update event: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}