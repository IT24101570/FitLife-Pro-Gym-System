package com.example.fit_lifegym.models;

import java.io.Serializable;

public class ExerciseAction implements Serializable {
    private String name;
    private String folderName;
    private int durationSeconds;
    private int repetitions;
    private boolean isTimed;
    private boolean isRest;

    public ExerciseAction(String name, String folderName, int durationSeconds) {
        this.name = name;
        this.folderName = folderName;
        this.durationSeconds = durationSeconds;
        this.isTimed = true;
        this.isRest = false;
    }

    public ExerciseAction(String name, String folderName, int repetitions, boolean isTimed) {
        this.name = name;
        this.folderName = folderName;
        this.repetitions = repetitions;
        this.isTimed = isTimed;
        this.isRest = false;
    }

    public static ExerciseAction createRest(int seconds) {
        ExerciseAction rest = new ExerciseAction("REST", "", seconds);
        rest.isRest = true;
        return rest;
    }

    public String getName() { return name; }
    public String getFolderName() { return folderName; }
    public int getDurationSeconds() { return durationSeconds; }
    public int getRepetitions() { return repetitions; }
    public boolean isTimed() { return isTimed; }
    public boolean isRest() { return isRest; }
}
