package com.example.fit_lifegym.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WorkoutEntity workout);

    @Update
    void update(WorkoutEntity workout);

    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY startTime DESC")
    List<WorkoutEntity> getAllWorkouts(String userId);

    @Query("SELECT * FROM workouts WHERE isSynced = 0")
    List<WorkoutEntity> getUnsyncedWorkouts();

    @Query("DELETE FROM workouts WHERE localId = :localId")
    void delete(int localId);
}
