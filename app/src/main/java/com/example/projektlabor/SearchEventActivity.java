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
    }

    private void setupListeners() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Nincs szükség azonnali szűrésre, csak amikor a Show Results gombra kattintanak
            }

        });

        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Nincs szükség azonnali szűrésre
        });

        btnDateFilter.setOnClickListener(v -> showDatePicker());

        switchAvailableOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Nincs szükség azonnali szűrésre
        });

        btnSort.setOnClickListener(v -> {
            String[] sortOptions = {"Date", "Name", "Sport Category"};
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Sort By")
                    .setItems(sortOptions, (dialog, which) -> {
                        // Mentsük el a kiválasztott rendezési opciót
                        switch (which) {
                            case 0:
                                btnSort.setText("Sort By: Date");
                                break;
                            case 1:
                                btnSort.setText("Sort By: Name");
                                break;
                            case 2:
                                btnSort.setText("Sort By: Sport Category");
                                break;
                        }
                    })
                    .show();
        });
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
        datePickerDialog.show();
    }

    private void showFilteredResults() {
        Intent intent = new Intent(this, FilteredEventsActivity.class);
        intent.putExtra("searchQuery", editTextSearch.getText().toString());

        Chip selectedChip = findViewById(filterChipGroup.getCheckedChipId());
        String sportCategory = selectedChip != null ? selectedChip.getText().toString() : "All";
        intent.putExtra("selectedSport", sportCategory);

        intent.putExtra("selectedDate", selectedDate);
        intent.putExtra("showOnlyAvailable", switchAvailableOnly.isChecked());

        startActivity(intent);
    }
}