package com.example.projektlabor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EventActivity extends AppCompatActivity {

    // Firebase Database referencia
    private DatabaseReference mDatabase;

    // UI elemek
    private EditText eventNameEditText, eventLocationEditText, eventTimeEditText;
    private Button createEventButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event);  // Az event.xml layout hozzákapcsolása

        // Firebase adatbázis referencia
        mDatabase = FirebaseDatabase.getInstance().getReference("events");

        // Hivatkozás a layout elemekre
        eventNameEditText = findViewById(R.id.event_name);
        eventLocationEditText = findViewById(R.id.event_location);
        eventTimeEditText = findViewById(R.id.event_time);
        createEventButton = findViewById(R.id.create_event_button);

        // "Create Event" gomb eseménykezelő
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEvent();
            }
        });
    }

    private void createEvent() {
        // Olvassuk be a felhasználó által megadott adatokat
        String eventName = eventNameEditText.getText().toString().trim();
        String eventLocation = eventLocationEditText.getText().toString().trim();
        String eventTime = eventTimeEditText.getText().toString().trim();

        // Ellenőrizzük, hogy minden mező ki van-e töltve
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

        // Egyedi azonosító létrehozása az eseményhez
        String eventId = mDatabase.push().getKey();

        // Esemény objektum létrehozása
        Event event = new Event(eventId, eventName, eventLocation, eventTime);

        // Esemény feltöltése a Firebase adatbázisba
        if (eventId != null) {
            mDatabase.child(eventId).setValue(event).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                    // Visszalépés a HomeActivity-hez
                    Intent intent = new Intent(EventActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish(); // Bezárjuk az EventActivity-t
                } else {
                    Toast.makeText(EventActivity.this, "Failed to create event", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Event osztály az adatok tárolására
    public static class Event {
        public String eventId;
        public String eventName;
        public String eventLocation;
        public String eventTime;

        // Üres konstruktor szükséges a Firebase számára
        public Event() {}

        public Event(String eventId, String eventName, String eventLocation, String eventTime) {
            this.eventId = eventId;
            this.eventName = eventName;
            this.eventLocation = eventLocation;
            this.eventTime = eventTime;
        }
    }
}