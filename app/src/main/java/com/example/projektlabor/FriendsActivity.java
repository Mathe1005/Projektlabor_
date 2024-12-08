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
    private RecyclerView recyclerViewRequests;
    private FriendAdapter friendAdapter;
    private FriendRequestAdapter requestAdapter;
    private List<User> friendsList;
    private List<FriendRequest> requestsList;
    private DatabaseReference usersRef;
    private DatabaseReference requestsRef;
    private FirebaseAuth mAuth;
    private TextInputEditText editTextFriendEmail;
    private MaterialButton btnAddFriend;
    private ProgressBar progressBar;
    private ImageView backButton;
    private TextView textEmptyState;
    private TextView textEmptyRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        initializeFirebase();
        initializeViews();
        setupRecyclerViews();
        setupClickListeners();
        loadFriendRequests();
        loadFriends();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        requestsRef = FirebaseDatabase.getInstance().getReference("friendRequests");
    }

    private void initializeViews() {
        recyclerViewFriends = findViewById(R.id.recycler_view_friends);
        recyclerViewRequests = findViewById(R.id.recycler_view_friend_requests);
        editTextFriendEmail = findViewById(R.id.edit_text_friend_email);
        btnAddFriend = findViewById(R.id.btn_add_friend);
        progressBar = findViewById(R.id.progress_bar);
        backButton = findViewById(R.id.back_button);
        textEmptyState = findViewById(R.id.text_empty_state);
        textEmptyRequests = findViewById(R.id.text_empty_requests);
    }

    private void setupRecyclerViews() {
        friendsList = new ArrayList<>();
        requestsList = new ArrayList<>();

        // Friend Adapter setup
        friendAdapter = new FriendAdapter(friendsList, new FriendAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(User friend) {
                showFriendOptionsDialog(friend);
            }
        });

        // Request Adapter setup
        requestAdapter = new FriendRequestAdapter(requestsList, new FriendRequestAdapter.OnRequestClickListener() {
            @Override
            public void onAcceptClick(FriendRequest request) {
                acceptFriendRequest(request);
            }

            @Override
            public void onRejectClick(FriendRequest request) {
                rejectFriendRequest(request);
            }
        });

        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFriends.setAdapter(friendAdapter);

        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRequests.setAdapter(requestAdapter);
    }

    private void setupClickListeners() {
        btnAddFriend.setOnClickListener(v -> {
            String friendEmail = editTextFriendEmail.getText().toString().trim();
            if (validateEmail(friendEmail)) {
                sendFriendRequest(friendEmail);
            }
        });

        backButton.setOnClickListener(v -> onBackPressed());

        editTextFriendEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                editTextFriendEmail.setError(null);
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
            editTextFriendEmail.setError("You cannot add yourself");
            editTextFriendEmail.requestFocus();
            return false;
        }

        return true;
    }

    private void sendFriendRequest(String friendEmail) {
        showProgress();
        String currentUserId = mAuth.getCurrentUser().getUid();
        String currentUserEmail = mAuth.getCurrentUser().getEmail();
        String currentUsername = mAuth.getCurrentUser().getDisplayName();

        usersRef.orderByChild("email").equalTo(friendEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        hideProgress();

                        if (!dataSnapshot.exists()) {
                            editTextFriendEmail.setError("User not found");
                            return;
                        }

                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String friendId = userSnapshot.getKey();
                            User friendUser = userSnapshot.getValue(User.class);

                            if (friendUser.isBlocked(currentUserId)) {
                                editTextFriendEmail.setError("Unable to send request");
                                return;
                            }

                            FriendRequest request = new FriendRequest(
                                    currentUserId,
                                    friendId,
                                    currentUserEmail,
                                    currentUsername != null ? currentUsername : currentUserEmail
                            );

                            requestsRef.child(request.getRequestId()).setValue(request)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(FriendsActivity.this,
                                                "Friend request sent",
                                                Toast.LENGTH_SHORT).show();
                                        editTextFriendEmail.setText("");
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(FriendsActivity.this,
                                                    "Failed to send request",
                                                    Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        hideProgress();
                        Toast.makeText(FriendsActivity.this,
                                "Error: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void acceptFriendRequest(FriendRequest request) {
        showProgress();
        String currentUserId = mAuth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();

        updates.put("/users/" + currentUserId + "/friends/" + request.getSenderId(), true);
        updates.put("/users/" + request.getSenderId() + "/friends/" + currentUserId, true);
        updates.put("/friendRequests/" + request.getRequestId() + "/status", "accepted");

        FirebaseDatabase.getInstance().getReference()
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    hideProgress();
                    Toast.makeText(FriendsActivity.this,
                            "Friend request accepted",
                            Toast.LENGTH_SHORT).show();
                    loadFriends();
                    loadFriendRequests();
                })
                .addOnFailureListener(e -> {
                    hideProgress();
                    Toast.makeText(FriendsActivity.this,
                            "Failed to accept request",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void rejectFriendRequest(FriendRequest request) {
        showProgress();
        requestsRef.child(request.getRequestId())
                .child("status")
                .setValue("rejected")
                .addOnSuccessListener(aVoid -> {
                    hideProgress();
                    Toast.makeText(FriendsActivity.this,
                            "Friend request rejected",
                            Toast.LENGTH_SHORT).show();
                    loadFriendRequests();
                })
                .addOnFailureListener(e -> {
                    hideProgress();
                    Toast.makeText(FriendsActivity.this,
                            "Failed to reject request",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFriends() {
        showProgress();
        String currentUserId = mAuth.getCurrentUser().getUid();

        usersRef.child(currentUserId).child("friends")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        friendsList.clear();

                        if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()) {
                            hideProgress();
                            showEmptyState(true);
                            return;
                        }

                        showEmptyState(false);
                        for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                            String friendId = friendSnapshot.getKey();
                            loadFriendDetails(friendId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        hideProgress();
                        Toast.makeText(FriendsActivity.this,
                                "Error loading friends: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadFriendDetails(String friendId) {
        usersRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User friend = dataSnapshot.getValue(User.class);
                if (friend != null) {
                    friendsList.add(friend);
                    friendAdapter.notifyDataSetChanged();
                }
                hideProgress();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideProgress();
                Toast.makeText(FriendsActivity.this,
                        "Error loading friend details: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFriendRequests() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        requestsRef.orderByChild("receiverId")
                .equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        requestsList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            FriendRequest request = snapshot.getValue(FriendRequest.class);
                            if (request != null && request.getStatus().equals("pending")) {
                                requestsList.add(request);
                            }
                        }
                        requestAdapter.notifyDataSetChanged();
                        updateRequestsVisibility();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(FriendsActivity.this,
                                "Error loading requests",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showFriendOptionsDialog(User friend) {
        // Itt implementáld a barát opciók dialógust (törlés, blokkolás, stb.)
        // Például használhatsz egy AlertDialog-ot vagy egy egyedi dialógust
    }

    private void blockUser(String userId) {
        showProgress();
        String currentUserId = mAuth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();

        updates.put("/users/" + currentUserId + "/blockedUsers/" + userId, true);
        updates.put("/users/" + currentUserId + "/friends/" + userId, null);
        updates.put("/users/" + userId + "/friends/" + currentUserId, null);

        FirebaseDatabase.getInstance().getReference()
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    hideProgress();
                    Toast.makeText(FriendsActivity.this,
                            "User blocked",
                            Toast.LENGTH_SHORT).show();
                    loadFriends();
                })
                .addOnFailureListener(e -> {
                    hideProgress();
                    Toast.makeText(FriendsActivity.this,
                            "Failed to block user",
                            Toast.LENGTH_SHORT).show();
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

    private void updateRequestsVisibility() {
        textEmptyRequests.setVisibility(requestsList.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerViewRequests.setVisibility(requestsList.isEmpty() ? View.GONE : View.VISIBLE);
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