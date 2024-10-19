package com.example.projektlabor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventActivity.Event> eventList;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(EventActivity.Event event);
    }

    public EventAdapter(List<EventActivity.Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
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
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventLocation, eventTime;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.text_event_name);
            eventLocation = itemView.findViewById(R.id.text_event_location);
            eventTime = itemView.findViewById(R.id.text_event_time);
        }

        public void bind(final EventActivity.Event event, final OnEventClickListener listener) {
            eventName.setText(event.eventName);
            eventLocation.setText(event.eventLocation);
            eventTime.setText(event.eventTime);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onEventClick(event);
                }
            });
        }
    }
}