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
    }

    public EventAdapter(List<EventActivity.Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
        this.mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
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
        TextView eventName, eventLocation, eventTime;
        Button editButton, deleteButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.text_event_name);
            eventLocation = itemView.findViewById(R.id.text_event_location);
            eventTime = itemView.findViewById(R.id.text_event_time);
            editButton = itemView.findViewById(R.id.button_edit_event);
            deleteButton = itemView.findViewById(R.id.button_delete_event);
        }

        public void bind(final EventActivity.Event event, final OnEventClickListener listener, String currentUserId) {
            eventName.setText(event.eventName);
            eventLocation.setText(event.eventLocation);
            eventTime.setText(event.eventTime);

            if (event.creatorId.equals(currentUserId)) {
                editButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);

                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onEditClick(event);
                    }
                });

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onDeleteClick(event);
                    }
                });
            } else {
                editButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onEventClick(event);
                }
            });
        }
    }
}