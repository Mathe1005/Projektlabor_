package com.example.projektlabor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class InviteFriendsAdapter extends RecyclerView.Adapter<InviteFriendsAdapter.ViewHolder> {
    private List<User> friendsList;
    private OnInviteClickListener listener;

    public interface OnInviteClickListener {
        void onInviteClick(User friend);
    }

    public InviteFriendsAdapter(List<User> friendsList, OnInviteClickListener listener) {
        this.friendsList = friendsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.invite_friend_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User friend = friendsList.get(position);
        holder.bind(friend, listener);
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUsername;
        TextView textViewEmail;
        MaterialButton inviteButton;

        ViewHolder(View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.text_view_username);
            textViewEmail = itemView.findViewById(R.id.text_view_email);
            inviteButton = itemView.findViewById(R.id.button_invite);
        }

        void bind(final User friend, final OnInviteClickListener listener) {
            textViewUsername.setText(friend.getUsername());
            textViewEmail.setText(friend.getEmail());

            inviteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInviteClick(friend);
                }
            });
        }
    }
}