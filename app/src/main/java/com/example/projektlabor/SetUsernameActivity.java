package com.example.projektlabor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SetUsernameActivity extends AppCompatActivity {
    private TextInputEditText usernameEditText;
    private MaterialButton confirmButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_username);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        usernameEditText = findViewById(R.id.username_edit_text);
        confirmButton = findViewById(R.id.confirm_button);

        confirmButton.setOnClickListener(v -> setUsername());
    }

    private void setUsername() {
        String username = usernameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Please enter a username");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(profileTask -> {
                        if (profileTask.isSuccessful()) {
                            mDatabase.child("users").child(userId).child("username").setValue(username)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SetUsernameActivity.this,
                                                    "Username set successfully", Toast.LENGTH_SHORT).show();
                                            navigateToMain();
                                        } else {
                                            Toast.makeText(SetUsernameActivity.this,
                                                    "Failed to set username", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(SetUsernameActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
    }
}