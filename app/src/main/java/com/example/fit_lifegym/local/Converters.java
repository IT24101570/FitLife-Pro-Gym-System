package com.example.fit_lifegym.local;

import androidx.room.TypeConverter;

import com.example.fit_lifegym.models.Exercise;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromExerciseLogList(List<Exercise.ExerciseLog> list) {
        if (list == null) return null;
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<Exercise.ExerciseLog> toExerciseLogList(String value) {
        if (value == null) return null;
        Gson gson = new Gson();
        Type type = new TypeToken<List<Exercise.ExerciseLog>>() {}.getType();
        return gson.fromJson(value, type);
    }
}
