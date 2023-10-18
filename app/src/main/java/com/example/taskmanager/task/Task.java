package com.example.taskmanager.task;

import android.os.Build;

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
}
