package com.example.projektlabor;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchEventActivity extends AppCompatActivity {

    private TextInputEditText editTextSearch;
    private List<EventActivity.Event> allEvents;
    private DatabaseReference eventsRef;
    private FirebaseAuth mAuth;
    private ChipGroup filterChipGroup;
    private MaterialButton btnDateFilter;
    private MaterialButton btnSort;
    private SwitchMaterial switchAvailableOnly;
    private String selectedDate = "";
    private MaterialButton btnShowResults;
    private String currentSortOption = "Date"; // Alapértelmezett rendezés

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_event_activity);

        initializeViews();
        setupListeners();
        loadAllEvents();
    }

    private void initializeViews() {
        mAuth = FirebaseAuth.getInstance();
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        editTextSearch = findViewById(R.id.edit_text_search);
        filterChipGroup = findViewById(R.id.filter_chip_group);
        btnDateFilter = findViewById(R.id.btn_date_filter);
        btnSort = findViewById(R.id.btn_sort);
        switchAvailableOnly = findViewById(R.id.switch_available_only);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        btnShowResults = findViewById(R.id.btn_show_results);
        btnShowResults.setOnClickListener(v -> showFilteredResults());

        btnSort.setText("Sort By: " + currentSortOption);
    }

    private void setupListeners() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Valós idejű keresés nem szükséges, a Show Results gomb kezeli
            }
        });

        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Chip kiválasztás kezelése a Show Results gombra lett áthelyezve
        });

        btnDateFilter.setOnClickListener(v -> showDatePicker());

        switchAvailableOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Switch állapot változás kezelése a Show Results gombra lett áthelyezve
        });

        btnSort.setOnClickListener(v -> showSortDialog());
    }

    private void showSortDialog() {
        String[] sortOptions = {"Date", "Name", "Sport Category", "Available Spots"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sort By")
                .setSingleChoiceItems(sortOptions, getSortOptionIndex(currentSortOption), (dialog, which) -> {
                    currentSortOption = sortOptions[which];
                    btnSort.setText("Sort By: " + currentSortOption);
                    dialog.dismiss();
                })
                .show();
    }

    private int getSortOptionIndex(String option) {
        switch (option) {
            case "Date": return 0;
            case "Name": return 1;
            case "Sport Category": return 2;
            case "Available Spots": return 3;
            default: return 0;
        }
    }

    private void loadAllEvents() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allEvents = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventActivity.Event event = snapshot.getValue(EventActivity.Event.class);
                    if (event != null) {
                        allEvents.add(event);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchEventActivity.this,
                        "Failed to load events: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format("%d.%02d.%02d", year, month + 1, dayOfMonth);
                    btnDateFilter.setText(selectedDate);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showFilteredResults() {
        Intent intent = new Intent(this, FilteredEventsActivity.class);

        // Keresési szöveg
        intent.putExtra("searchQuery", editTextSearch.getText().toString().toLowerCase());

        // Sport kategória
        Chip selectedChip = findViewById(filterChipGroup.getCheckedChipId());
        String sportCategory = selectedChip != null ? selectedChip.getText().toString() : "All";
        intent.putExtra("selectedSport", sportCategory);

        // Dátum és elérhetőség
        intent.putExtra("selectedDate", selectedDate);
        intent.putExtra("showOnlyAvailable", switchAvailableOnly.isChecked());

        // Rendezési beállítás
        intent.putExtra("sortOption", currentSortOption);

        startActivity(intent);
    }

    private void sortEvents(List<EventActivity.Event> events) {
        switch (currentSortOption) {
            case "Date":
                Collections.sort(events, (e1, e2) -> e1.eventTime.compareTo(e2.eventTime));
                break;
            case "Name":
                Collections.sort(events, (e1, e2) -> e1.eventName.compareTo(e2.eventName));
                break;
            case "Sport Category":
                Collections.sort(events, (e1, e2) -> e1.sportCategory.compareTo(e2.sportCategory));
                break;
            case "Available Sports":
                Collections.sort(events, (e1, e2) -> {
                    int spots1 = e1.maxParticipants - (e1.participants != null ? e1.participants.size() : 0);
                    int spots2 = e2.maxParticipants - (e2.participants != null ? e2.participants.size() : 0);
                    return spots2 - spots1; // Csökkenő sorrend (több szabad hely előre)
                });
                break;
        }
    }
}