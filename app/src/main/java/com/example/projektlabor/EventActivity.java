package com.example.projektlabor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EventActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private TextInputEditText eventNameEditText, eventLocationEditText, eventTimeEditText;
    private MaterialButton createEventButton;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event);

        // Toolbar beállítása
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Create Event");

        mDatabase = FirebaseDatabase.getInstance().getReference("events");
        mAuth = FirebaseAuth.getInstance();

        eventNameEditText = findViewById(R.id.event_name);
        eventLocationEditText = findViewById(R.id.event_location);
        eventTimeEditText = findViewById(R.id.event_time);
        createEventButton = findViewById(R.id.create_event_button);

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEvent();
            }
        });
    }

    private void createEvent() {
        String eventName = eventNameEditText.getText().toString().trim();
        String eventLocation = eventLocationEditText.getText().toString().trim();
        String eventTime = eventTimeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(eventName)) {
            eventNameEditText.setError("Please enter event name");
            return;
        }
        if (TextUtils.isEmpty(eventLocation)) {
            eventLocationEditText.setError("Please enter event location");
            return;
        }
        if (TextUtils.isEmpty(eventTime)) {
            eventTimeEditText.setError("Please enter event time");
            return;
        }

        String eventId = mDatabase.push().getKey();
        String creatorId = mAuth.getCurrentUser().getUid();

        Event event = new Event(eventId, eventName, eventLocation, eventTime, creatorId);

        if (eventId != null) {
            mDatabase.child(eventId).setValue(event).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EventActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(EventActivity.this, "Failed to create event", Toast.LENGTH_SHORT).show();
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

        public Event() {}

        public Event(String eventId, String eventName, String eventLocation, String eventTime, String creatorId) {
            this.eventId = eventId;
            this.eventName = eventName;
            this.eventLocation = eventLocation;
            this.eventTime = eventTime;
            this.creatorId = creatorId;
        }
    }
}