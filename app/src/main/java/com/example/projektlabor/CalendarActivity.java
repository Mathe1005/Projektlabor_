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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private ImageView backButton;
    private RecyclerView recyclerViewEvents;
    private EventAdapter eventAdapter;
    private DatabaseReference eventsRef;
    private FirebaseAuth mAuth;
    private Map<String, List<EventActivity.Event>> eventsByDate;
    private SimpleDateFormat dateFormat;
    private List<EventActivity.Event> selectedDateEvents;

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
        selectedDateEvents = new ArrayList<>();

        calendarView = findViewById(R.id.calendar_view);
        backButton = findViewById(R.id.back_button);
        recyclerViewEvents = findViewById(R.id.recycler_view_calendar_events);

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(selectedDateEvents, this);
        recyclerViewEvents.setAdapter(eventAdapter);

        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void setupCalendar() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String dateString = String.format(Locale.getDefault(), "%d.%02d.%02d", year, month + 1, dayOfMonth);
            updateEventsForDate(dateString);
        });
    }

    private void updateEventsForDate(String date) {
        selectedDateEvents.clear();
        List<EventActivity.Event> events = eventsByDate.get(date);
        if (events != null && !events.isEmpty()) {
            selectedDateEvents.addAll(events);
        }
        eventAdapter.notifyDataSetChanged();

        // Update the visibility of the RecyclerView and empty state
        if (selectedDateEvents.isEmpty()) {
            recyclerViewEvents.setVisibility(View.GONE);
            findViewById(R.id.text_no_events).setVisibility(View.VISIBLE);
        } else {
            recyclerViewEvents.setVisibility(View.VISIBLE);
            findViewById(R.id.text_no_events).setVisibility(View.GONE);
        }
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

                // Update calendar decorations for dates with events
                updateCalendarDecorations();

                // Update events for current selected date
                Calendar cal = Calendar.getInstance();
                String currentDate = dateFormat.format(cal.getTime());
                updateEventsForDate(currentDate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CalendarActivity.this,
                        "Failed to load events: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCalendarDecorations() {
        // Sajnos az Android alapértelmezett CalendarView nem támogatja a napok egyedi dekorálását
        // Alternatív megoldásként használhatnánk külső könyvtárat (pl. MaterialCalendarView)
        // vagy készíthetnénk saját naptár nézetet

        // Egyelőre csak a kiválasztott napra mutatjuk az eseményeket a RecyclerView-ban
    }
}