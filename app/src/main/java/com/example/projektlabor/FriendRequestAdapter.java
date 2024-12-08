package com.example.projektlabor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {
    private List<FriendRequest> requestsList;
    private OnRequestClickListener listener;

    public interface OnRequestClickListener {
        void onAcceptClick(FriendRequest request);
        void onRejectClick(FriendRequest request);
    }

    public FriendRequestAdapter(List<FriendRequest> requestsList, OnRequestClickListener listener) {
        this.requestsList = requestsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_request_item, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        FriendRequest request = requestsList.get(position);
        holder.bind(request, listener);
    }

    @Override
    public int getItemCount() {
        return requestsList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUsername;
        TextView textViewEmail;
        Button acceptButton;
        Button rejectButton;

        RequestViewHolder(View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.text_view_username);
            textViewEmail = itemView.findViewById(R.id.text_view_email);
            acceptButton = itemView.findViewById(R.id.button_accept);
            rejectButton = itemView.findViewById(R.id.button_reject);
        }

        void bind(FriendRequest request, OnRequestClickListener listener) {
            textViewUsername.setText(request.getSenderUsername());
            textViewEmail.setText(request.getSenderEmail());

            acceptButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptClick(request);
                }
            });

            rejectButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRejectClick(request);
                }
            });
        }
    }
}