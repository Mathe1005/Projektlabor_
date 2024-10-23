package com.example.projektlabor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewFriends;
    private FriendAdapter friendAdapter;
    private List<User> friendsList;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private TextInputEditText editTextFriendEmail;
    private MaterialButton btnAddFriend;
    private ProgressBar progressBar;
    private ImageView backButton;
    private TextView textEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        initializeFirebase();
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadFriends();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void initializeViews() {
        recyclerViewFriends = findViewById(R.id.recycler_view_friends);
        editTextFriendEmail = findViewById(R.id.edit_text_friend_email);
        btnAddFriend = findViewById(R.id.btn_add_friend);
        progressBar = findViewById(R.id.progress_bar);
        backButton = findViewById(R.id.back_button);
        textEmptyState = findViewById(R.id.text_empty_state);
    }

    private void setupRecyclerView() {
        friendsList = new ArrayList<>();
        friendAdapter = new FriendAdapter(friendsList, new FriendAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(User friend) {
                showToast("Selected friend: " + friend.getEmail());
            }
        });

        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFriends.setAdapter(friendAdapter);
    }

    private void setupClickListeners() {
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String friendEmail = editTextFriendEmail.getText().toString().trim();
                if (validateEmail(friendEmail)) {
                    addFriend(friendEmail);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        editTextFriendEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editTextFriendEmail.setError(null);
                }
            }
        });
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            editTextFriendEmail.setError("Please enter an email address");
            editTextFriendEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextFriendEmail.setError("Please enter a valid email address");
            editTextFriendEmail.requestFocus();
            return false;
        }

        String currentUserEmail = mAuth.getCurrentUser().getEmail();
        if (email.equals(currentUserEmail)) {
            editTextFriendEmail.setError("You cannot add yourself as a friend");
            editTextFriendEmail.requestFocus();
            return false;
        }

        return true;
    }

    private void loadFriends() {
        showProgress();
        String currentUserId = mAuth.getCurrentUser().getUid();

        usersRef.child(currentUserId).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsList.clear();

                if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()) {
                    hideProgress();
                    showEmptyState(true);
                    return;
                }

                showEmptyState(false);
                int totalFriends = (int) dataSnapshot.getChildrenCount();
                final int[] loadedFriends = {0};

                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    String friendId = friendSnapshot.getKey();
                    usersRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String email = dataSnapshot.child("email").getValue(String.class);
                                String username = dataSnapshot.child("username").getValue(String.class);

                                User friend = new User();
                                friend.setUserId(dataSnapshot.getKey());
                                friend.setEmail(email);
                                friend.setUsername(username != null ? username : email);

                                friendsList.add(friend);
                                friendAdapter.notifyDataSetChanged();
                            }

                            loadedFriends[0]++;
                            if (loadedFriends[0] >= totalFriends) {
                                hideProgress();
                                showEmptyState(friendsList.isEmpty());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            hideProgress();
                            showToast("Error loading friend: " + databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideProgress();
                showToast("Error loading friends: " + databaseError.getMessage());
            }
        });
    }

    private void addFriend(String friendEmail) {
        showProgress();
        String currentUserId = mAuth.getCurrentUser().getUid();

        usersRef.orderByChild("email").equalTo(friendEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            hideProgress();
                            editTextFriendEmail.setError("User not found with this email");
                            return;
                        }

                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String friendId = userSnapshot.getKey();

                            usersRef.child(currentUserId).child("friends").child(friendId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                hideProgress();
                                                showToast("You are already friends with this user");
                                                return;
                                            }

                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("/users/" + currentUserId + "/friends/" + friendId, true);
                                            updates.put("/users/" + friendId + "/friends/" + currentUserId, true);

                                            FirebaseDatabase.getInstance().getReference()
                                                    .updateChildren(updates)
                                                    .addOnSuccessListener(aVoid -> {
                                                        hideProgress();
                                                        showToast("Friend added successfully");
                                                        editTextFriendEmail.setText("");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        hideProgress();
                                                        showToast("Error adding friend: " + e.getMessage());
                                                    });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            hideProgress();
                                            showToast("Database error: " + databaseError.getMessage());
                                        }
                                    });
                            return;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        hideProgress();
                        showToast("Database error: " + databaseError.getMessage());
                    }
                });
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        btnAddFriend.setEnabled(false);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
        btnAddFriend.setEnabled(true);
    }

    private void showEmptyState(boolean show) {
        textEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewFriends.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(FriendsActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}