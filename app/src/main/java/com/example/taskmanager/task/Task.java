package com.example.taskmanager.task;

import android.os.Bundle;

import com.example.taskmanager.R;
import com.example.taskmanager.interfaces.Validation;
import com.example.taskmanager.utility.App;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A class which represents task data and has utilities for working with said data.
 */
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
        expiresOn = LocalDate.now();
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

    /**
     * Validates the current data of the task.
     * @throws Exception Exception due to failed validation which must be caught at higher level
     */
    @Override
    public void validate() throws Exception {
        int titleLimit = App.getContext().getResources().getInteger(R.integer.task_title_limit);
        int descriptionLimit = App.getContext().getResources().getInteger(R.integer.task_description_limit);

        if(title == null || title.length() == 0) {
            throw new Exception(App.getContext().getResources()
                    .getString(R.string.missing_task_title_message));
        }

        if(title.length() > titleLimit) {
            throw  new Exception("The task title is too long! It is limited to " +
                    titleLimit + " characters.");
        }

        if(description.length() > descriptionLimit) {
            throw  new Exception("The task description is too long! It is limited to " +
                    descriptionLimit + " characters.");
        }

        if(expiresOn != null && getStatus() != null &&
                getStatus().equals(TaskStatus.TODO) && expiresOn.isBefore(LocalDate.now())) {
            throw new Exception(App.getContext().getResources()
                    .getString(R.string.incorrectly_set_task_expiry_date_message));
        }
    }

    /**
     * Determines whether the incomplete task is expired or not and
     * updates the reevaluated status.
     */
    public void reevaluateStatus() {
        if(getStatus() == null) {
            setStatus(TaskStatus.TODO);
        }

        if(!getStatus().equals(TaskStatus.Finished)) {
            if(expiresOn != null && expiresOn.isBefore(LocalDate.now())) {
                setStatus(TaskStatus.Failed);
            }
            else {
                setStatus(TaskStatus.TODO);
            }
        }
    }

    /**
     * Bundles the data of the current task in order to make it passable to
     * an intent.
     * @return A bundle with all the task data
     */
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

    /**
     * Updates the data of the current task using existing data from a bundle.
     * @param taskData A bundle containing task data
     */
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
