package com.example.projektlabor;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

public class EventActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private TextInputEditText eventNameEditText, eventLocationEditText, eventTimeEditText;
    private TextInputEditText maxParticipantsEditText, descriptionEditText, startTimeEditText;
    private AutoCompleteTextView sportCategorySpinner;
    private MaterialButton createEventButton;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event);

        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Create Event");

        mDatabase = FirebaseDatabase.getInstance().getReference("events");
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupSportCategorySpinner();
    }

    private void initializeViews() {
        eventNameEditText = findViewById(R.id.event_name);
        eventLocationEditText = findViewById(R.id.event_location);
        eventTimeEditText = findViewById(R.id.event_time);
        sportCategorySpinner = findViewById(R.id.sport_category_spinner);
        maxParticipantsEditText = findViewById(R.id.max_participants);
        descriptionEditText = findViewById(R.id.event_description);
        startTimeEditText = findViewById(R.id.event_start_time);
        createEventButton = findViewById(R.id.create_event_button);

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
                    true
            );
            timePickerDialog.show();
        });

        createEventButton.setOnClickListener(v -> createEvent());
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

    private void createEvent() {
        String eventName = eventNameEditText.getText().toString().trim();
        String eventLocation = eventLocationEditText.getText().toString().trim();
        String eventTime = eventTimeEditText.getText().toString().trim();
        String sportCategory = sportCategorySpinner.getText().toString();
        String maxParticipantsStr = maxParticipantsEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String startTime = startTimeEditText.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(eventName)) {
            eventNameEditText.setError("Please enter event name");
            return;
        }
        if (TextUtils.isEmpty(eventLocation)) {
            eventLocationEditText.setError("Please enter event location");
            return;
        }
        if (TextUtils.isEmpty(eventTime)) {
            eventTimeEditText.setError("Please enter event date");
            return;
        }
        if (TextUtils.isEmpty(sportCategory)) {
            sportCategorySpinner.setError("Please select a sport category");
            return;
        }
        if (TextUtils.isEmpty(maxParticipantsStr)) {
            maxParticipantsEditText.setError("Please enter maximum participants");
            return;
        }
        if (TextUtils.isEmpty(description)) {
            descriptionEditText.setError("Please enter event description");
            return;
        }
        if (TextUtils.isEmpty(startTime)) {
            startTimeEditText.setError("Please enter start time");
            return;
        }

        int maxParticipants;
        try {
            maxParticipants = Integer.parseInt(maxParticipantsStr);
            if (maxParticipants <= 0) {
                maxParticipantsEditText.setError("Number must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            maxParticipantsEditText.setError("Please enter a valid number");
            return;
        }

        String eventId = mDatabase.push().getKey();
        String creatorId = mAuth.getCurrentUser().getUid();
        String creatorUsername = mAuth.getCurrentUser().getDisplayName();

        Event event = new Event(eventId, eventName, eventLocation, eventTime,
                creatorId, creatorUsername, sportCategory,
                maxParticipants, description, startTime);

        if (eventId != null) {
            mDatabase.child(eventId).setValue(event)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EventActivity.this,
                                    "Event created successfully",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EventActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(EventActivity.this,
                                    "Failed to create event",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static class Event {
        public String eventId;
        public String eventName;
        public String eventLocation;
        public String eventTime;
        public String creatorId;
        public String creatorUsername;
        public String sportCategory;
        public int maxParticipants;
        public String description;
        public String startTime;
        public Map<String, Boolean> participants;

        public Event() {
            this.participants = new HashMap<>();
        }

        public Event(String eventId, String eventName, String eventLocation, String eventTime,
                     String creatorId, String creatorUsername, String sportCategory,
                     int maxParticipants, String description, String startTime) {
            this.eventId = eventId;
            this.eventName = eventName;
            this.eventLocation = eventLocation;
            this.eventTime = eventTime;
            this.creatorId = creatorId;
            this.creatorUsername = creatorUsername;
            this.sportCategory = sportCategory;
            this.maxParticipants = maxParticipants;
            this.description = description;
            this.startTime = startTime;
            this.participants = new HashMap<>();
        }

        public boolean isFull() {
            return participants != null && participants.size() >= maxParticipants;
        }

        public boolean isParticipant(String userId) {
            return participants != null && participants.containsKey(userId);
        }
    }
}