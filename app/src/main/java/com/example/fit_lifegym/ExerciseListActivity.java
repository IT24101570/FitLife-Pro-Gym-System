package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fit_lifegym.adapters.ExerciseListAdapter;
import com.example.fit_lifegym.models.ExerciseAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExerciseListActivity extends AppCompatActivity {

    private String subCategoryName;
    private String type;
    private String difficulty;
    private int count;
    private List<ExerciseAction> exerciseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);

        subCategoryName = getIntent().getStringExtra("sub_category_name");
        type = getIntent().getStringExtra("type");
        difficulty = getIntent().getStringExtra("difficulty");
        count = getIntent().getIntExtra("exercise_count", 10);

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(subCategoryName);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        RecyclerView rvExercises = findViewById(R.id.rvExercises);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));

        loadExercises();
        
        ExerciseListAdapter adapter = new ExerciseListAdapter(this, exerciseList);
        rvExercises.setAdapter(adapter);

        findViewById(R.id.btnStartNow).setOnClickListener(v -> {
            Intent intent = new Intent(ExerciseListActivity.this, ExercisePlayerActivity.class);
            intent.putExtra("exercise_list", (ArrayList<ExerciseAction>) exerciseList);
            startActivity(intent);
        });
    }

    private void loadExercises() {
        exerciseList = new ArrayList<>();
        List<String> folders = getFoldersForType(type);
        Collections.shuffle(folders);

        int duration = 30;
        int reps = 12;
        if ("advance".equals(difficulty)) {
            duration = 45;
            reps = 20;
            count = 15;
        } else if ("intermediate".equals(difficulty)) {
            duration = 35;
            reps = 15;
            count = 12;
        } else if ("beginner".equals(difficulty)) {
            duration = 30;
            reps = 10;
            count = 8;
        }

        int exerciseCounter = 0;
        for (int i = 0; i < Math.min(count, folders.size()); i++) {
            String folder = folders.get(i);
            String name = folder.replace("_", " ");
            
            // Randomly decide if it's timed or reps for variety, 
            // but usually static exercises are timed and active ones are reps.
            // For now, let's alternate or use a simple logic.
            boolean isTimed = (i % 2 == 0); 
            
            if (isTimed) {
                exerciseList.add(new ExerciseAction(name, folder, duration));
            } else {
                exerciseList.add(new ExerciseAction(name, folder, reps, false));
            }
            
            exerciseCounter++;
            // Add rest after every 3 exercises
            if (exerciseCounter % 3 == 0 && i < Math.min(count, folders.size()) - 1) {
                exerciseList.add(ExerciseAction.createRest(20)); // 20s rest
            }
        }
    }

    private List<String> getFoldersForType(String type) {
        List<String> folders = new ArrayList<>();
        switch (type) {
            case "abs":
                folders.add("ABDOMINALCRUNCHES");
                folders.add("BICYCLECRUNCHES");
                folders.add("CRUNCHESWITHLEGSRAISED");
                folders.add("FLUTTERKICKS");
                folders.add("HEELSTOTHEHEAVEN");
                folders.add("LEGIN&OUTS");
                folders.add("LEGRAISES");
                folders.add("LONGARMCRUNCHES");
                folders.add("MOUNTAINCLIMBER");
                folders.add("PLANK");
                folders.add("REVERSECRUNCHES");
                folders.add("RUSSIANTWIST");
                folders.add("SITUPS");
                folders.add("SITUPTWIST");
                folders.add("VCRUNCH");
                folders.add("VHOLD");
                folders.add("VUP");
                folders.add("XMANCRUNCH");
                folders.add("ALTVUP");
                folders.add("BODYSAW");
                folders.add("DEADBUG");
                folders.add("CROSSOVERCRUNCH");
                folders.add("STARFISHCRUNCH");
                folders.add("HEELTOUCH");
                folders.add("PLANKJACKS");
                folders.add("PLANKHIPDIPS");
                break;
            case "chest":
                folders.add("CHESTPRESSPULSE");
                folders.add("DUMBBELLBENCHPRESS");
                folders.add("DUMBBELLCHESTFLY");
                folders.add("PUSHUPS");
                folders.add("WIDEARMPUSHUPS");
                folders.add("INCLINEPUSHUPS");
                folders.add("DECLINEPUSHUPS");
                folders.add("BOXPUSHUPS");
                folders.add("KNEEPUSHUPS");
                folders.add("SPIDERMANPUSHUPS");
                folders.add("HINDUPUSHUPS");
                folders.add("MILITARYPUSHUPS");
                folders.add("STAGGEREDPUSHUPS");
                folders.add("DIAMONDPUSHUPS");
                folders.add("WALLPUSHUPS");
                folders.add("COBRASTRETCH");
                folders.add("CHESTSTRETCH");
                break;
            case "arm":
                folders.add("ARMCIRCLESCLOCKWISE");
                folders.add("ARMCURLSCRUNCHLEFT");
                folders.add("ARMCURLSCRUNCHRIGHT");
                folders.add("ARMSCISSORS");
                folders.add("TRICEPSDIPS");
                folders.add("TRICEPSKICKBACKS");
                folders.add("DUMBBELLPULLOVER");
                folders.add("DOORWAYCURLSLEFT");
                folders.add("DOORWAYCURLSRIGHT");
                folders.add("ALTERNATEDUMBBELLHAMMERCURL");
                folders.add("ARMRAISES");
                folders.add("SIDEARMRAISE");
                folders.add("CLOCKWISEARMSWINGS");
                folders.add("COUNTERCLOCKWISEARMSWINGS");
                folders.add("TRICEPSSTRETCHLEFT");
                folders.add("TRICEPSSTRETCHRIGHT");
                folders.add("FLOORTRICEPDIPS");
                break;
            case "leg":
                folders.add("BACKWARDLUNGE");
                folders.add("LUNGES");
                folders.add("SQUATS");
                folders.add("SUMOSQUAT");
                folders.add("WALLSIT");
                folders.add("JUMPINGSQUATS");
                folders.add("CALFRAISEWITHSPLAYEDFOOT");
                folders.add("DONKEYKICKSLEFT");
                folders.add("DONKEYKICKSRIGHT");
                folders.add("FIREHYDRANTLEFT");
                folders.add("FIREHYDRANTRIGHT");
                folders.add("BRIDGE");
                folders.add("BUTTKICKS");
                folders.add("CURTSYLUNGES");
                folders.add("SIDELEGRAISELEFT");
                folders.add("SIDELEGRAISERIGHT");
                folders.add("WALLCALFRAISES");
                folders.add("SQUATPULSES");
                break;
            case "shoulder_back":
                folders.add("BENTOVERROW");
                folders.add("REVERSEFLYS");
                folders.add("RHOMBOIDPULLS");
                folders.add("SHOULDERGATORS");
                folders.add("REVERSESNOWANGELS");
                folders.add("HYPEREXTENSION");
                folders.add("PRONETRICEPSPUSHUPS");
                folders.add("CATCOWPOSE");
                folders.add("SUPERMAN");
                folders.add("FLOORYRAISES");
                folders.add("SWIMMERANDSUPERMAN");
                folders.add("SHOULDERSTRETCH");
                folders.add("CLOCKWISESHOULDERROLLS");
                folders.add("COUNTERCLOCKWISESHOULDERROLLS");
                break;
            default:
                folders.add("JUMPINGJACKS");
                folders.add("HIGHSTEPPING");
                folders.add("BURPEES");
                break;
        }
        return folders;
    }
}
