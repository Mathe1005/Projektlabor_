package com.example.projektlabor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventActivity.Event> eventList;
    private Context context;
    private FirebaseAuth mAuth;
    private DatabaseReference eventsRef;

    public EventAdapter(List<EventActivity.Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
        this.eventsRef = FirebaseDatabase.getInstance().getReference("events");
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventActivity.Event event = eventList.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;
        TextView creatorEmail;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.text_event_name);
            creatorEmail = itemView.findViewById(R.id.text_creator_email);

            // Az egész kártya kattintható lesz
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    EventActivity.Event event = eventList.get(position);
                    openEventDetails(event);
                }
            });
        }

        void bind(EventActivity.Event event) {
            eventName.setText(event.eventName);
            creatorEmail.setText("Created by: " + event.creatorUsername);
        }

        private void openEventDetails(EventActivity.Event event) {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("EVENT_ID", event.eventId);
            context.startActivity(intent);
        }
    }

    public void updateEvents(List<EventActivity.Event> newEvents) {
        this.eventList.clear();
        this.eventList.addAll(newEvents);
        notifyDataSetChanged();
    }
}