package com.example.taskmanager.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.taskmanager.task.Task;
import com.example.taskmanager.task.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase _db;
    public static final String DB_NAME = "TASK_MANAGER.db";
    public static final int DB_VERSION = 1;
    public static final String DB_CREATE = "" +
            "CREATE TABLE TASK(" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "TITLE TEXT NOT NULL," +
            "DESCRIPTION TEXT," +
            "EXPIRES_ON INTEGER," +
            "COMPLETED_ON INTEGER " +
            "   CHECK(COMPLETED_ON IS NULL OR COMPLETED_ON <= unixepoch('now'))," +
            "STATUS INTEGER NOT NULL " +
            "   CHECK(STATUS >= 0 AND STATUS <= 2)" +
            ");";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        _db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if(i1>i){
            sqLiteDatabase.execSQL("DROP TABLE TASK;");
            sqLiteDatabase.execSQL(DB_CREATE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("Range")
    public List<Task> selectTasks() {
        String selectQuery = "SELECT * FROM TASK ORDER BY EXPIRES_ON";
        Cursor c = _db.rawQuery(selectQuery, null);
        List<Task> tasks = new ArrayList<>();

        while (c.moveToNext()) {
            Task currentTask = new Task();
            currentTask.setId(c.getInt(c.getColumnIndex("ID")));
            currentTask.setTitle(c.getString(c.getColumnIndex("TITLE")));
            currentTask.setDescription(c.getString(c.getColumnIndex("DESCRIPTION")));
            currentTask.setExpiresOn(
                    getLocalDateFromEpochSeconds(c.getLong(c.getColumnIndex("EXPIRES_ON"))));
            currentTask.setCompletedOn(
                    getLocalDateTimeFromEpochSeconds(c.getLong(c.getColumnIndex("COMPLETED_ON"))));
            currentTask.setStatus(TaskStatus.getStatus(
                    c.getInt(c.getColumnIndex("STATUS"))));

            tasks.add(currentTask);
        }

        return tasks;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void insertTask(Task task) {
        String insertQuery = "INSERT INTO TASK(" +
                "TITLE, DESCRIPTION, EXPIRES_ON, STATUS) " +
                "VALUES(?,?,?,?)";

        _db.execSQL(insertQuery, new Object[] {
                task.getTitle(),
                task.getDescription(),
                getDateEpochSeconds(task.getExpiresOn()),
                TaskStatus.TODO.getValue()
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateTask(Task task) {
        String updateQuery = "UPDATE TASK SET " +
                "TITLE = ?, DESCRIPTION = ?, EXPIRES_ON = ?, " +
                "COMPLETED_ON = ?, STATUS = ? " +
                "WHERE ID = ?";
        _db.execSQL(updateQuery, new Object[] {
                task.getTitle(),
                task.getDescription(),
                getDateEpochSeconds(task.getExpiresOn()),
                getDateTimeEpochSeconds(task.getCompletedOn()),
                task.getStatus().getValue(),
                task.getId()
        });
    }

    public void deleteTask(Task task) {
        String deleteQuery = "DELETE FROM TASK WHERE ID = ?";
        _db.execSQL(deleteQuery, new Object[]{task.getId()});
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    private Long getDateEpochSeconds(@Nullable LocalDate date) {
        if(date == null) {
            return null;
        }

        ZoneId zoneId = ZoneId.systemDefault();
        Long epoch = date.atStartOfDay(zoneId).toEpochSecond();

        return epoch;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    private LocalDate getLocalDateFromEpochSeconds(@Nullable Long epochSeconds) {
        if(epochSeconds == null || epochSeconds == 0) {
            return null;
        }

        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate date = Instant.ofEpochSecond(epochSeconds).atZone(zoneId).toLocalDate();
        return date;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    private Long getDateTimeEpochSeconds(@Nullable LocalDateTime dateTime) {
        if(dateTime == null) {
            return  null;
        }

        ZoneId zoneId = ZoneId.systemDefault();
        Long epoch = dateTime.atZone(zoneId).toEpochSecond();

        return epoch;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    private LocalDateTime getLocalDateTimeFromEpochSeconds(@Nullable Long epochSeconds) {
        if(epochSeconds == null || epochSeconds == 0) {
            return null;
        }

        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime dateTime = Instant.ofEpochSecond(epochSeconds).atZone(zoneId).toLocalDateTime();
        return dateTime;
    }
}
