package com.example.fit_lifegym.models;

public class ExerciseLibrary {
    private String id;
    private String name;
    private String description;
    private String muscleGroup;
    private String equipment;
    private String difficulty;
    private String videoUrl;
    private String instructions;
    private boolean isFavorite;
    private String imageUrl;
    private int caloriesPerMinute;
    private boolean isPremium;

    public ExerciseLibrary() {
        // Required empty constructor for Firebase
    }

    public ExerciseLibrary(String id, String name, String description, String muscleGroup,
                          String equipment, String difficulty, String videoUrl,
                          String instructions, String imageUrl, int caloriesPerMinute) {
        this(id, name, description, muscleGroup, equipment, difficulty, videoUrl, instructions, imageUrl, caloriesPerMinute, false);
    }

    public ExerciseLibrary(String id, String name, String description, String muscleGroup,
                          String equipment, String difficulty, String videoUrl,
                          String instructions, String imageUrl, int caloriesPerMinute, boolean isPremium) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.equipment = equipment;
        this.difficulty = difficulty;
        this.videoUrl = videoUrl;
        this.instructions = instructions;
        this.isFavorite = false;
        this.imageUrl = imageUrl;
        this.caloriesPerMinute = caloriesPerMinute;
        this.isPremium = isPremium;
    }

    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getCaloriesPerMinute() { return caloriesPerMinute; }
    public void setCaloriesPerMinute(int caloriesPerMinute) { this.caloriesPerMinute = caloriesPerMinute; }

    // Constants for muscle groups
    public static final String MUSCLE_CHEST = "Chest";
    public static final String MUSCLE_BACK = "Back";
    public static final String MUSCLE_SHOULDERS = "Shoulders";
    public static final String MUSCLE_ARMS = "Arms";
    public static final String MUSCLE_LEGS = "Legs";
    public static final String MUSCLE_CORE = "Core";
    public static final String MUSCLE_CARDIO = "Cardio";
    public static final String MUSCLE_FULL_BODY = "Full Body";

    // Constants for equipment
    public static final String EQUIPMENT_NONE = "None";
    public static final String EQUIPMENT_DUMBBELLS = "Dumbbells";
    public static final String EQUIPMENT_BARBELL = "Barbell";
    public static final String EQUIPMENT_MACHINE = "Machine";
    public static final String EQUIPMENT_CABLE = "Cable";
    public static final String EQUIPMENT_KETTLEBELL = "Kettlebell";
    public static final String EQUIPMENT_RESISTANCE_BAND = "Resistance Band";
    public static final String EQUIPMENT_BENCH = "Bench";
    public static final String EQUIPMENT_PULL_UP_BAR = "Pull-up Bar";
    public static final String EQUIPMENT_CARDIO_MACHINE = "Cardio Machine";

    // Constants for difficulty
    public static final String DIFFICULTY_BEGINNER = "Beginner";
    public static final String DIFFICULTY_INTERMEDIATE = "Intermediate";
    public static final String DIFFICULTY_ADVANCED = "Advanced";
}
