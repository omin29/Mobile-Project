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
import java.util.stream.Collectors;

public class DatabaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase _db;
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
            sqLiteDatabase.execSQL("DROP TABLE TASK;");
            sqLiteDatabase.execSQL(DB_CREATE);
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    @SuppressLint("Range")
//    public List<Task> selectTasks() {
//        String selectQuery = "SELECT * FROM TASK ORDER BY EXPIRES_ON";
//        Cursor c = _db.rawQuery(selectQuery, null);
//        List<Task> tasks = new ArrayList<>();
//
//        while (c.moveToNext()) {
//            Task currentTask = new Task();
//            currentTask.setId(c.getInt(c.getColumnIndex("ID")));
//            currentTask.setTitle(c.getString(c.getColumnIndex("TITLE")));
//            currentTask.setDescription(c.getString(c.getColumnIndex("DESCRIPTION")));
//            currentTask.setExpiresOn(
//                    getLocalDateFromEpochSeconds(c.getLong(c.getColumnIndex("EXPIRES_ON"))));
//            currentTask.setCompletedOn(
//                    getLocalDateTimeFromEpochSeconds(c.getLong(c.getColumnIndex("COMPLETED_ON"))));
//            currentTask.setStatus(TaskStatus.getStatus(
//                    c.getInt(c.getColumnIndex("STATUS"))));
//            currentTask.reevaluateStatus();//If task has expired while stored in the database
//
//            tasks.add(currentTask);
//        }
//
//        if(tasks.size() > 1) {
//        /*Tasks are ordered by expiry date but we want to show the ones without
//          expiry date at the end of the list. The tasks without expiry date
//          return 0 epoch seconds and this causes them to be in the
//          beginning of the ordered list. This happens because we store
//          the expiry date in integer column in the database.*/
//            while (tasks.get(0).getExpiresOn() == null) {
//                Task taskWithoutExpiryDate = tasks.get(0);
//                tasks.remove(taskWithoutExpiryDate);
//                tasks.add(taskWithoutExpiryDate);
//            }
//
//        /*When first task has expiry date then
//          we have gone through all tasks without one
//          because the list is ordered by expiry date.*/
//        }
//
//        return tasks;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public List<Task> selectTasks(TaskStatus statusFilter) {
//        List<Task> tasks = selectTasks();
//        tasks = tasks.stream().filter(t -> t.getStatus().equals(statusFilter)).collect(Collectors.toList());
//        return tasks;
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("Range")
    public List<Task> selectTasks(@Nullable TaskStatus statusFilter) {
        String selectQuery = "";

        //Statuses correspond to TaskStatus enum values
        if(statusFilter.equals(TaskStatus.TODO)) {
            selectQuery = "SELECT * FROM " +
                    "(SELECT * from 'TASK' " +
                    "WHERE STATUS = 0 AND EXPIRES_ON >= unixepoch('now', 'start of day') ORDER BY EXPIRES_ON) " +
                    "UNION ALL " +
                    "SELECT * from 'TASK' WHERE STATUS = 0 AND EXPIRES_ON IS NULL";
        }
        else if(statusFilter.equals(TaskStatus.Finished)) {
            selectQuery = "SELECT * FROM 'TASK' WHERE STATUS = 1 ORDER BY COMPLETED_ON";
        }
        else if(statusFilter.equals(TaskStatus.Failed)) {
            selectQuery = "SELECT * FROM 'TASK' " +
                    "WHERE STATUS = 2 OR EXPIRES_ON < unixepoch('now', 'start of day') ORDER BY EXPIRES_ON";
        }
        else {
            selectQuery = "SELECT * FROM TASK ORDER BY EXPIRES_ON";
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

    private  void updateStatusForExpiredTasks(){
        String updateStatusQuery = "UPDATE 'TASK' " +
                "SET STATUS = 2 " +
                "WHERE EXPIRES_ON < unixepoch('now', 'start of day')";
        _db.execSQL(updateStatusQuery);
    }
}
