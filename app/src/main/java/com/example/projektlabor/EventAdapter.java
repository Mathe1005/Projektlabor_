package com.example.projektlabor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventActivity.Event> eventList;
    private OnEventClickListener listener;
    private FirebaseAuth mAuth;

    public interface OnEventClickListener {
        void onEventClick(EventActivity.Event event);
        void onEditClick(EventActivity.Event event);
        void onDeleteClick(EventActivity.Event event);
        void onJoinClick(EventActivity.Event event);
        void onLeaveClick(EventActivity.Event event);
    }

    public EventAdapter(List<EventActivity.Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
        this.mAuth = FirebaseAuth.getInstance();
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
        holder.bind(event, listener, mAuth.getCurrentUser().getUid());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventLocation, eventTime, creatorEmail, sportCategory;
        TextView participantsCount, description, startTime;
        Button editButton, deleteButton, joinButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.text_event_name);
            eventLocation = itemView.findViewById(R.id.text_event_location);
            eventTime = itemView.findViewById(R.id.text_event_time);
            creatorEmail = itemView.findViewById(R.id.text_creator_email);
            sportCategory = itemView.findViewById(R.id.text_sport_category);
            participantsCount = itemView.findViewById(R.id.text_participants_count);
            description = itemView.findViewById(R.id.text_description);
            startTime = itemView.findViewById(R.id.text_start_time);
            editButton = itemView.findViewById(R.id.button_edit_event);
            deleteButton = itemView.findViewById(R.id.button_delete_event);
            joinButton = itemView.findViewById(R.id.button_join_event);
        }

        public void bind(final EventActivity.Event event, final OnEventClickListener listener, String currentUserId) {
            eventName.setText(event.eventName);
            eventLocation.setText(event.eventLocation);
            eventTime.setText(event.eventTime);
            creatorEmail.setText("Created by: " + event.creatorEmail);
            sportCategory.setText("Sport: " + event.sportCategory);
            description.setText(event.description);
            startTime.setText("Starts at: " + event.startTime);

            int participantCount = event.participants != null ? event.participants.size() : 0;
            participantsCount.setText(String.format("%d/%d participants",
                    participantCount,
                    event.maxParticipants));

            if (event.creatorId.equals(currentUserId)) {
                editButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);
                joinButton.setVisibility(View.GONE);
            } else {
                editButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);

                if (event.isParticipant(currentUserId)) {
                    joinButton.setText("Leave Event");
                    joinButton.setEnabled(true);
                } else if (event.isFull()) {
                    joinButton.setText("Event Full");
                    joinButton.setEnabled(false);
                } else {
                    joinButton.setText("Join Event");
                    joinButton.setEnabled(true);
                }
                joinButton.setVisibility(View.VISIBLE);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(event);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(event);
                }
            });

            joinButton.setOnClickListener(v -> {
                if (listener != null) {
                    if (event.isParticipant(currentUserId)) {
                        listener.onLeaveClick(event);
                    } else {
                        listener.onJoinClick(event);
                    }
                }
            });
        }
    }
}