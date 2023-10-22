package com.example.taskmanager.task;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import com.example.taskmanager.R;
import com.example.taskmanager.interfaces.Validation;
import com.example.taskmanager.utility.App;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task implements Validation {
    private int id = 0;
    private String title = "";
    private String description = "";
    private LocalDate expiresOn = null;
    private LocalDateTime completedOn = null;
    private TaskStatus status = TaskStatus.TODO;

    public  final String ARG_TASK_ID = "task-id";
    public final String ARG_TASK_TITLE = "task-title";
    public final String ARG_TASK_DESCRIPTION = "task-description";
    public final String ARG_TASK_EXPIRES_ON = "task-expiresOn";
    public  final String ARG_TASK_COMPLETED_ON = "task-completedOn";
    public  final String ARG_TASK_STATUS = "task-status";

    public Task() {

    }
    public Task(String title) {
        setTitle(title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            expiresOn = LocalDate.now();
        }
    }

    public Task(String title, String description) {
        this(title);
        setDescription(description);
    }

    public Task(String title, String description, LocalDate expiresOn) {
        this(title, description);
        setExpiresOn(expiresOn);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.trim();
    }

    public LocalDate getExpiresOn() {
        return expiresOn;
    }

    public void setExpiresOn(LocalDate expiresOn) {
        this.expiresOn = expiresOn;
    }

    public LocalDateTime getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(LocalDateTime completedOn) {
        this.completedOn = completedOn;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void validate() throws Exception {
        int titleLimit = App.getContext().getResources().getInteger(R.integer.task_title_limit);
        int descriptionLimit = App.getContext().getResources().getInteger(R.integer.task_description_limit);

        if(title == null || title.length() == 0) {
            throw new Exception("The task must have a title!");
        }

        if(title.length() > titleLimit) {
            throw  new Exception("The task title is too long! It is limited to " +
                    titleLimit + " characters.");
        }

        if(description.length() > descriptionLimit) {
            throw  new Exception("The task description is too long! It is limited to " +
                    descriptionLimit + " characters.");
        }

        if(expiresOn != null && expiresOn.isBefore(LocalDate.now())) {
            throw new Exception("The task expiry date should not be before the current date!");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void reevaluateStatus() {
        if(expiresOn != null && !getStatus().equals(TaskStatus.Finished)) {
            if(expiresOn.isBefore(LocalDate.now())) {
                setStatus(TaskStatus.Failed);
            }
            else {
                setStatus(TaskStatus.TODO);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Bundle getBundleData() {
        Bundle taskData = new Bundle();
        taskData.putInt(ARG_TASK_ID, getId());
        taskData.putString(ARG_TASK_TITLE, getTitle());
        taskData.putString(ARG_TASK_DESCRIPTION, getDescription());
        taskData.putString(ARG_TASK_EXPIRES_ON,
                (getExpiresOn() != null)?
                        getExpiresOn().format(App.APP_DATE_FORMATTER):null);
        taskData.putString(ARG_TASK_COMPLETED_ON,
                (getCompletedOn() != null)?
                        getCompletedOn().format(App.APP_DATE_TIME_FORMATTER):null);
        taskData.putString(ARG_TASK_STATUS,
                (getStatus() != null)?
                        getStatus().name():null);

        return  taskData;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressWarnings("ConstantConditions")
    public void setDataFromBundle(Bundle taskData) {
        setId(taskData.getInt(ARG_TASK_ID));
        setTitle(taskData.getString(ARG_TASK_TITLE));
        setDescription(taskData.getString(ARG_TASK_DESCRIPTION));
        setExpiresOn((taskData.getString(ARG_TASK_EXPIRES_ON) != null)?
                LocalDate.parse(taskData.getString(ARG_TASK_EXPIRES_ON), App.APP_DATE_FORMATTER):null);
        setCompletedOn((taskData.getString(ARG_TASK_COMPLETED_ON) != null)?
                LocalDateTime.parse(taskData.getString(ARG_TASK_COMPLETED_ON), App.APP_DATE_TIME_FORMATTER):null);
        setStatus((taskData.getString(ARG_TASK_STATUS) != null)?
                TaskStatus.valueOf((taskData.getString(ARG_TASK_STATUS))):TaskStatus.TODO);
    }
}
