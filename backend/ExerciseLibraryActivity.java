package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.adapters.ExerciseLibraryAdapter;
import com.example.fit_lifegym.models.ExerciseLibrary;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExerciseLibraryActivity extends AppCompatActivity implements ExerciseLibraryAdapter.OnExerciseClickListener {

    private RecyclerView rvExercises;
    private View layoutEmpty;
    private EditText etSearch;
    private ExerciseLibraryAdapter adapter;
    private DatabaseReference exercisesRef;
    private SessionManager sessionManager;

    private List<ExerciseLibrary> allExercises = new ArrayList<>();
    private List<ExerciseLibrary> filteredExercises = new ArrayList<>();
    
    private String selectedMuscleGroup = null;
    private String selectedEquipment = null;
    private boolean showFavoritesOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_library);

        sessionManager = new SessionManager(this);
        exercisesRef = FirebaseDatabase.getInstance().getReference("exerciseLibrary");
        
        initViews();
        setupRecyclerView();
        setupFilters();
        loadExercises();
    }

    private void initViews() {
        rvExercises = findViewById(R.id.rvExercises);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        etSearch = findViewById(R.id.etSearch);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddExercise).setOnClickListener(v -> Toast.makeText(this, "Admin only feature", Toast.LENGTH_SHORT).show());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { filterExercises(); }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new ExerciseLibraryAdapter(this);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        rvExercises.setAdapter(adapter);
    }

    private void setupFilters() {
        Chip chipAll = findViewById(R.id.chipAllExercises);
        Chip chipMuscle = findViewById(R.id.chipMuscleGroup);
        Chip chipEquip = findViewById(R.id.chipEquipment);
        Chip chipFav = findViewById(R.id.chipFavorites);

        chipAll.setOnClickListener(v -> {
            selectedMuscleGroup = null;
            selectedEquipment = null;
            showFavoritesOnly = false;
            filterExercises();
        });

        chipMuscle.setOnClickListener(v -> showMuscleGroupDialog());
        chipEquip.setOnClickListener(v -> showEquipmentDialog());
        chipFav.setOnClickListener(v -> {
            showFavoritesOnly = !showFavoritesOnly;
            filterExercises();
        });
    }

    private void loadExercises() {
        exercisesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allExercises.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ExerciseLibrary ex = ds.getValue(ExerciseLibrary.class);
                        if (ex != null) allExercises.add(ex);
                    }
                } else {
                    preloadSampleExercises();
                }
                filterExercises();
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void filterExercises() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        filteredExercises.clear();

        for (ExerciseLibrary ex : allExercises) {
            boolean matchesSearch = query.isEmpty() || ex.getName().toLowerCase().contains(query);
            boolean matchesMuscle = selectedMuscleGroup == null || ex.getMuscleGroup().equals(selectedMuscleGroup);
            boolean matchesEquip = selectedEquipment == null || ex.getEquipment().equals(selectedEquipment);
            boolean matchesFav = !showFavoritesOnly || ex.isFavorite();

            if (matchesSearch && matchesMuscle && matchesEquip && matchesFav) filteredExercises.add(ex);
        }

        adapter.setExercises(filteredExercises);
        layoutEmpty.setVisibility(filteredExercises.isEmpty() ? View.VISIBLE : View.GONE);
        rvExercises.setVisibility(filteredExercises.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showMuscleGroupDialog() {
        String[] groups = {"Chest", "Back", "Legs", "Arms", "Shoulders", "Core"};
        new AlertDialog.Builder(this).setTitle("Muscle Group").setItems(groups, (d, w) -> {
            selectedMuscleGroup = groups[w];
            filterExercises();
        }).show();
    }

    private void showEquipmentDialog() {
        String[] equip = {"None", "Dumbbells", "Barbell", "Machine", "Cable"};
        new AlertDialog.Builder(this).setTitle("Equipment").setItems(equip, (d, w) -> {
            selectedEquipment = equip[w];
            filterExercises();
        }).show();
    }

    @Override
    public void onExerciseClick(ExerciseLibrary exercise) {
        Intent intent = new Intent(this, ExerciseDetailActivity.class);
        intent.putExtra("exerciseId", exercise.getId());
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(ExerciseLibrary exercise) {
        exercise.setFavorite(!exercise.isFavorite());
        exercisesRef.child(exercise.getId()).child("favorite").setValue(exercise.isFavorite());
        adapter.notifyDataSetChanged();
    }

    private void preloadSampleExercises() {
        // ... (keeping sample data logic)
    }
}
