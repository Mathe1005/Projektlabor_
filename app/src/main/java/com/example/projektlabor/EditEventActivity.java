package com.example.projektlabor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditEventActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private EditText eventNameEditText, eventLocationEditText, eventTimeEditText;
    private Button updateEventButton;
    private String eventId;
    private EventActivity.Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        mDatabase = FirebaseDatabase.getInstance().getReference("events");
        mAuth = FirebaseAuth.getInstance();

        eventNameEditText = findViewById(R.id.edit_event_name);
        eventLocationEditText = findViewById(R.id.edit_event_location);
        eventTimeEditText = findViewById(R.id.edit_event_time);
        updateEventButton = findViewById(R.id.update_event_button);

        eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null) {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEventData();

        updateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEvent();
            }
        });
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditEventActivity.this, "Failed to load event data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEvent() {
        if (currentEvent == null) {
            Toast.makeText(this, "Error: Event data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = eventNameEditText.getText().toString().trim();
        String newLocation = eventLocationEditText.getText().toString().trim();
        String newTime = eventTimeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newLocation) || TextUtils.isEmpty(newTime)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();
        if (!currentEvent.creatorId.equals(currentUserId)) {
            Toast.makeText(this, "You don't have permission to edit this event", Toast.LENGTH_SHORT).show();
            return;
        }

        EventActivity.Event updatedEvent = new EventActivity.Event(eventId, newName, newLocation, newTime, currentEvent.creatorId);

        mDatabase.child(eventId).setValue(updatedEvent).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(EditEventActivity.this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditEventActivity.this, "Failed to update event: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}