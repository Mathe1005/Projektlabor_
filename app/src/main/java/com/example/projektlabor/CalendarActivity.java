package com.example.projektlabor;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private ImageView backButton;
    private DatabaseReference eventsRef;
    private FirebaseAuth mAuth;
    private Map<String, List<EventActivity.Event>> eventsByDate;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeViews();
        setupCalendar();
        loadEvents();
    }

    private void initializeViews() {
        mAuth = FirebaseAuth.getInstance();
        eventsRef = FirebaseDatabase.getInstance().getReference("events");
        eventsByDate = new HashMap<>();
        dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

        calendarView = findViewById(R.id.calendar_view);
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void setupCalendar() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String dateString = String.format(Locale.getDefault(), "%d.%02d.%02d", year, month + 1, dayOfMonth);
            showEventsForDate(dateString);
        });
    }

    private void loadEvents() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventsByDate.clear();

                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    EventActivity.Event event = eventSnapshot.getValue(EventActivity.Event.class);
                    if (event != null && (event.creatorId.equals(currentUserId) ||
                            (event.participants != null && event.participants.containsKey(currentUserId)))) {

                        List<EventActivity.Event> events = eventsByDate.computeIfAbsent(
                                event.eventTime, k -> new ArrayList<>());
                        events.add(event);
                    }
                }

                // Frissítjük a naptár megjelenését
                updateCalendarDecorations();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CalendarActivity.this,
                        "Failed to load events: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEventsForDate(String date) {
        List<EventActivity.Event> events = eventsByDate.get(date);
        if (events != null && !events.isEmpty()) {
            StringBuilder message = new StringBuilder("Events on this day:\n\n");
            for (EventActivity.Event event : events) {
                message.append("• ").append(event.eventName)
                        .append(" at ").append(event.startTime)
                        .append("\n");
            }
            Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No events on this day", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCalendarDecorations() {
        // Itt lehetne implementálni a napok vizuális dekorációját
        // például különböző színekkel jelölni azokat a napokat, ahol van esemény
    }
}