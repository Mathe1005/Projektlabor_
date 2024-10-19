package com.example.projektlabor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUserEvents;
    private RecyclerView recyclerViewOtherEvents;
    private EventAdapter userEventAdapter;
    private EventAdapter otherEventAdapter;
    private List<EventActivity.Event> userEventList;
    private List<EventActivity.Event> otherEventList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("events");

        MaterialButton btnAddEvent = findViewById(R.id.btn_add_event);
        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, EventActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout navHome = findViewById(R.id.nav_home);
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });

        LinearLayout navProfile = findViewById(R.id.nav_profile);
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        recyclerViewUserEvents = findViewById(R.id.recycler_view_user_events);
        recyclerViewOtherEvents = findViewById(R.id.recycler_view_other_events);
        recyclerViewUserEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOtherEvents.setLayoutManager(new LinearLayoutManager(this));

        userEventList = new ArrayList<>();
        otherEventList = new ArrayList<>();

        EventAdapter.OnEventClickListener listener = new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(EventActivity.Event event) {
                Toast.makeText(HomeActivity.this, "Event clicked: " + event.eventName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditClick(EventActivity.Event event) {
                Intent intent = new Intent(HomeActivity.this, EditEventActivity.class);
                intent.putExtra("EVENT_ID", event.eventId);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(EventActivity.Event event) {
                deleteEvent(event);
            }
        };

        userEventAdapter = new EventAdapter(userEventList, listener);
        otherEventAdapter = new EventAdapter(otherEventList, listener);

        recyclerViewUserEvents.setAdapter(userEventAdapter);
        recyclerViewOtherEvents.setAdapter(otherEventAdapter);

        loadEvents();
    }

    private void loadEvents() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userEventList.clear();
                otherEventList.clear();
                String currentUserId = mAuth.getCurrentUser().getUid();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventActivity.Event event = snapshot.getValue(EventActivity.Event.class);
                    if (event != null) {
                        if (event.creatorId.equals(currentUserId)) {
                            userEventList.add(event);
                        } else {
                            otherEventList.add(event);
                        }
                    }
                }
                userEventAdapter.notifyDataSetChanged();
                otherEventAdapter.notifyDataSetChanged();

                updateEventListsVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, "Failed to load events: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEventListsVisibility() {
        TextView userEventsTitle = findViewById(R.id.text_user_events_title);
        TextView otherEventsTitle = findViewById(R.id.text_other_events_title);

        if (userEventList.isEmpty()) {
            userEventsTitle.setVisibility(View.GONE);
            recyclerViewUserEvents.setVisibility(View.GONE);
        } else {
            userEventsTitle.setVisibility(View.VISIBLE);
            recyclerViewUserEvents.setVisibility(View.VISIBLE);
        }

        if (otherEventList.isEmpty()) {
            otherEventsTitle.setVisibility(View.GONE);
            recyclerViewOtherEvents.setVisibility(View.GONE);
        } else {
            otherEventsTitle.setVisibility(View.VISIBLE);
            recyclerViewOtherEvents.setVisibility(View.VISIBLE);
        }
    }

    private void deleteEvent(EventActivity.Event event) {
        mDatabase.child(event.eventId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to delete event: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}