package com.example.projektlabor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import com.google.android.material.button.MaterialButton;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewEvents;
    private EventAdapter eventAdapter;
    private List<EventActivity.Event> eventList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("events");

        // Hivatkozás az "Add New Event" gombra
        MaterialButton btnAddEvent = findViewById(R.id.btn_add_event);

        // OnClickListener hozzáadása a gombhoz
        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Átirányítás az EventActivity-re
                Intent intent = new Intent(HomeActivity.this, EventActivity.class);
                startActivity(intent);
            }
        });

        // Hivatkozás a home ikonra
        LinearLayout navHome = findViewById(R.id.nav_home);

        // OnClickListener hozzáadása a home ikonhoz
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mivel már a HomeActivity-n vagyunk, csak frissítjük az Activity-t
                recreate();
            }
        });

        // RecyclerView inicializálása
        recyclerViewEvents = findViewById(R.id.recycler_view_events);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));

        // Esemény lista inicializálása
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(EventActivity.Event event) {
                if (event.creatorId.equals(mAuth.getCurrentUser().getUid())) {
                    Intent intent = new Intent(HomeActivity.this, EditEventActivity.class);
                    intent.putExtra("EVENT_ID", event.eventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(HomeActivity.this, "You can only edit events you created", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerViewEvents.setAdapter(eventAdapter);

        loadEvents();
    }

    private void loadEvents() {
        mDatabase.addValueEventListener(new ValueEventListener() {
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
                Toast.makeText(HomeActivity.this, "Failed to load events: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}