package com.example.taskmanager.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.taskmanager.task.Task;
import com.example.taskmanager.task.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class which handles database operations with tasks.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private final SQLiteDatabase _db;
    public static final String DB_NAME = "TASK_MANAGER.db";
    public static final int DB_VERSION = 2;
    public static final String DB_CREATE = "" +
            "CREATE TABLE TASK(" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "TITLE TEXT NOT NULL," +
            "DESCRIPTION TEXT," +
            "EXPIRES_ON INTEGER," +
            "COMPLETED_ON INTEGER, " +
            "STATUS INTEGER NOT NULL " +
            "   CHECK(STATUS >= 0 AND STATUS <= 2)" +
            ");";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        _db = this.getWritableDatabase();
        updateStatusForExpiredTasks();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if(i1>i){
            //The table is rebuilt if the database version has been updated.
            sqLiteDatabase.execSQL("DROP TABLE TASK;");
            sqLiteDatabase.execSQL(DB_CREATE);
        }
    }

    /**
     * Returns a list of tasks from the database. The returned tasks can be filtered by status.
     * @param statusFilter Task status used for filtering the tasks which will be returned
     * @return A list of extracted tasks from the database
     */
    @SuppressLint("Range")
    public List<Task> selectTasks(@Nullable TaskStatus statusFilter) {
        //Default select query when no status filter is specified
        String selectQuery = "SELECT * FROM TASK ORDER BY EXPIRES_ON";

        if(statusFilter != null) {
            //Statuses correspond to TaskStatus enum values
            if (statusFilter.equals(TaskStatus.TODO)) {
                selectQuery = "SELECT * FROM " +
                        "(SELECT * from 'TASK' " +
                        "WHERE STATUS = 0 AND EXPIRES_ON >= unixepoch('now', 'start of day') ORDER BY EXPIRES_ON) " +
                        "UNION ALL " +
                        "SELECT * from 'TASK' WHERE STATUS = 0 AND EXPIRES_ON IS NULL";
            } else if (statusFilter.equals(TaskStatus.Finished)) {
                selectQuery = "SELECT * FROM 'TASK' WHERE STATUS = 1 ORDER BY COMPLETED_ON DESC";
            } else if (statusFilter.equals(TaskStatus.Failed)) {
                selectQuery = "SELECT * FROM 'TASK' " +
                        "WHERE STATUS = 2 OR (EXPIRES_ON < unixepoch('now', 'start of day') AND STATUS = 0) ORDER BY EXPIRES_ON";
            }
        }

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
        c.close();

        return tasks;
    }

    /**
     * Inserts a new task in the database.
     * @param task Object which represents task data
     */
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

    /**
     * Updates an existing task in the database.
     * @param task Object which represents task data
     */
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

    /**
     * Deletes a specific task from the database.
     * @param task Object which represents task data
     */
    public void deleteTask(Task task) {
        String deleteQuery = "DELETE FROM TASK WHERE ID = ?";
        _db.execSQL(deleteQuery, new Object[]{task.getId()});
    }

    /**
     * Converts LocalDate instance to Unix epoch seconds if possible.
     * @param date LocalDate instance of a date
     * @return Unix epoch representation of provided LocalDate
     */
    @Nullable
    private Long getDateEpochSeconds(@Nullable LocalDate date) {
        if(date == null) {
            return null;
        }

        ZoneId zoneId = ZoneId.systemDefault();
        return date.atStartOfDay(zoneId).toEpochSecond();
    }

    /**
     * Converts Unix epoch seconds to its LocalDate equivalent if possible.
     * @param epochSeconds Unix epoch seconds
     * @return The LocalDate representation of the provided Unix epoch seconds
     */
    @Nullable
    private LocalDate getLocalDateFromEpochSeconds(@Nullable Long epochSeconds) {
        if(epochSeconds == null || epochSeconds == 0) {
            return null;
        }

        ZoneId zoneId = ZoneId.systemDefault();
        return Instant.ofEpochSecond(epochSeconds).atZone(zoneId).toLocalDate();
    }

    /**
     * Converts LocalDateTime instance to Unix epoch seconds if possible.
     * @param dateTime LocalDateTime instance of date and time
     * @return Unix epoch representation of provided LocalDateTime
     */
    @Nullable
    private Long getDateTimeEpochSeconds(@Nullable LocalDateTime dateTime) {
        if(dateTime == null) {
            return  null;
        }

        ZoneId zoneId = ZoneId.systemDefault();
        return dateTime.atZone(zoneId).toEpochSecond();
    }

    /**
     * Converts Unix epoch seconds to its LocalDateTime equivalent if possible.
     * @param epochSeconds Unix epoch seconds
     * @return The LocalDateTime representation of the provided Unix epoch seconds
     */
    @Nullable
    private LocalDateTime getLocalDateTimeFromEpochSeconds(@Nullable Long epochSeconds) {
        if(epochSeconds == null || epochSeconds == 0) {
            return null;
        }

        ZoneId zoneId = ZoneId.systemDefault();
        return Instant.ofEpochSecond(epochSeconds).atZone(zoneId).toLocalDateTime();
    }

    /**
     * Updates the status of incomplete tasks in the database which have expired.
     */
    private  void updateStatusForExpiredTasks(){
        String updateStatusQuery = "UPDATE 'TASK' " +
                "SET STATUS = 2 " +
                "WHERE EXPIRES_ON < unixepoch('now', 'start of day') AND " +
                "STATUS = 0";
        _db.execSQL(updateStatusQuery);
    }
}
