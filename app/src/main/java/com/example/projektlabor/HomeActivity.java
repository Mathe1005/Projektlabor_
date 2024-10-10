package com.example.projektlabor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Hivatkozás az "Add New Event" gombra
        MaterialButton btnAddEvent = findViewById(R.id.btn_add_event);

        // OnClickListener hozzáadása a gombhoz
        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Átirányítás az EventActivity-re
                Intent intent = new Intent(HomeActivity.this, EventActivity.class);
                startActivity(intent);
            }
        });
    }
}