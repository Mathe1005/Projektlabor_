package com.example.projektlabor;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewFriends;
    private FriendAdapter friendAdapter;
    private List<User> friendsList;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private EditText editTextFriendEmail;
    private MaterialButton btnAddFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        recyclerViewFriends = findViewById(R.id.recycler_view_friends);
        editTextFriendEmail = findViewById(R.id.edit_text_friend_email);
        btnAddFriend = findViewById(R.id.btn_add_friend);

        friendsList = new ArrayList<>();
        friendAdapter = new FriendAdapter(friendsList, new FriendAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(User friend) {
                // Handle friend click (e.g., open friend's profile)
                Toast.makeText(FriendsActivity.this, "Clicked on: " + friend.getUsername(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFriends.setAdapter(friendAdapter);

        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String friendEmail = editTextFriendEmail.getText().toString().trim();
                if (!friendEmail.isEmpty()) {
                    addFriend(friendEmail);
                } else {
                    Toast.makeText(FriendsActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadFriends();
    }

    private void loadFriends() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        usersRef.child(currentUserId).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsList.clear();
                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    String friendId = friendSnapshot.getKey();
                    usersRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User friend = dataSnapshot.getValue(User.class);
                            if (friend != null) {
                                friend.setUserId(dataSnapshot.getKey());
                                friendsList.add(friend);
                                friendAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(FriendsActivity.this, "Error loading friend: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FriendsActivity.this, "Error loading friends: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addFriend(String friendEmail) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        usersRef.orderByChild("email").equalTo(friendEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String friendId = userSnapshot.getKey();
                        if (friendId.equals(currentUserId)) {
                            Toast.makeText(FriendsActivity.this, "You can't add yourself as a friend", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DatabaseReference currentUserFriendsRef = usersRef.child(currentUserId).child("friends");
                        DatabaseReference friendUserFriendsRef = usersRef.child(friendId).child("friends");

                        currentUserFriendsRef.child(friendId).setValue(true)
                                .addOnSuccessListener(aVoid -> friendUserFriendsRef.child(currentUserId).setValue(true)
                                        .addOnSuccessListener(aVoid1 -> {
                                            Toast.makeText(FriendsActivity.this, "Friend added successfully", Toast.LENGTH_SHORT).show();
                                            editTextFriendEmail.setText("");
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(FriendsActivity.this, "Error adding friend: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                                .addOnFailureListener(e -> Toast.makeText(FriendsActivity.this, "Error adding friend: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                        return;
                    }
                }
                Toast.makeText(FriendsActivity.this, "User not found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FriendsActivity.this, "Error adding friend: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}