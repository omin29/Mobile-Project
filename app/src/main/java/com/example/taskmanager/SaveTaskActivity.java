package com.example.taskmanager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanager.task.Task;
import com.example.taskmanager.task.TaskStatus;
import com.example.taskmanager.utility.App;
import com.example.taskmanager.utility.DatabaseHelper;

import java.time.LocalDate;

/**
 * Allows creation and editing of tasks.
 */
public class SaveTaskActivity extends AppCompatActivity {
    protected EditText taskTitleEditText, taskDescriptionEditText;
    protected TextView taskExpiryDateTextView, taskCompletedOnTextView;
    protected ImageButton removeTaskExpiryDateButton;
    protected Task currentTask;
    protected DatabaseHelper _db;
    public static final String ARG_CALLER_TAB_INDEX = "caller-tab-index";
    protected int callerTabIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_task);
        taskTitleEditText = findViewById(R.id.taskTitleEditText);
        taskDescriptionEditText = findViewById(R.id.taskDescriptionEditText);
        taskExpiryDateTextView = findViewById(R.id.taskExpiryDateTextView);
        removeTaskExpiryDateButton = findViewById(R.id.removeTaskExpiryDateButton);
        taskCompletedOnTextView = findViewById(R.id.taskCompletedOnTextView);

        //Will determine whether a task is being edited and take the appropriate course of action
        loadPotentialTaskData();
    }

    /**
     * Returns the user back to the main activity without making changes.
     * @param v The clicked view
     */
    public void cancelTaskCreationHandler(View v) {
        goToMainActivity();
    }

    /**
     * Opens a date picker dialog which allows the user to set an expiry date
     * for the task. The user input is validated before UI changes occur.
     * @param v The clicked view
     */
    public void setExpiryDateHandler(View v) {
        DatePickerDialog dialog = new DatePickerDialog(
                this, (datePicker, year, month, dayOfMonth) -> {
            /*Adding 1 to the month value because the date picker dialog will
              return values between 0 and 11 when we need to provide a value
              between 1 and 12 to create LocalDate instance.*/
            LocalDate newDate = LocalDate.of(year, month + 1, dayOfMonth);

            if(newDate.isBefore(LocalDate.now())) {
                Toast.makeText(App.getContext(),
                        getResources().getString(R.string.incorrectly_set_task_expiry_date_message),
                        Toast.LENGTH_LONG).show();
                return;
            }

            taskExpiryDateTextView.setText(newDate.format(App.APP_DATE_FORMATTER));
            removeTaskExpiryDateButton.setVisibility(View.VISIBLE);
        }, LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1, LocalDate.now().getDayOfMonth());
        //We subtract 1 from month value above because the date dialog works with values between 0 and 11.

        dialog.show();
    }

    /**
     * Removes the expiry date of the task.
     * @param v The clicked view
     */
    public void removeTaskExpiryDateHandler(View v) {
        taskExpiryDateTextView.setText(getResources().getString(R.string.task_expiry_date_text_view_default));
        removeTaskExpiryDateButton.setVisibility(View.GONE);
    }

    /**
     * Creates a new task or edits the data of existing one depending on the circumstances.
     * After a successful save, the user will be returned to the tab which lists tasks of the
     * same type.
     * @param v The clicked view
     */
    public void taskSaveHandler(View v) {
        try {
            //When creating
            if(currentTask == null) {
                currentTask = new Task(
                        taskTitleEditText.getText().toString(),
                        taskDescriptionEditText.getText().toString(),
                        getLocalDateFromFormattedText(taskExpiryDateTextView.getText().toString())
                );
            }
            //When editing
            else {
                currentTask.setTitle(taskTitleEditText.getText().toString());
                currentTask.setDescription(taskDescriptionEditText.getText().toString());
                currentTask.setExpiresOn(getLocalDateFromFormattedText(taskExpiryDateTextView.getText().toString()));
                currentTask.reevaluateStatus();
            }

            currentTask.validate();
            _db = new DatabaseHelper(this);

            if(currentTask.getId() == 0) {
                _db.insertTask(currentTask);
            }
            else {
                _db.updateTask(currentTask);
            }

            Toast.makeText(this,
                    getResources().getString(R.string.task_save_success_message),
                    Toast.LENGTH_LONG).show();
            goToMainActivity();
        }
        catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        finally {
            if(_db != null) {
                _db.close();
                _db = null;
            }

            /*The current task is reset in case the creation of a new task failed.
              This is done in order to avoid attempting an update on a task
              which isn't in the database.*/
            if(currentTask != null && currentTask.getId() == 0) {
                currentTask = null;
            }
        }
    }

    /**
     * Converts a date from text representation to LocalDate instance using the date formatter
     * for this app.
     * @param formattedText A local date formatted as text using the format specific for this app
     * @return The date as LocalDate instance
     */
    @Nullable
    private LocalDate getLocalDateFromFormattedText(String formattedText) {
        if(formattedText.equals(getResources().getString(R.string.task_expiry_date_text_view_default))) {
            return null;
        }

        return LocalDate.parse(formattedText, App.APP_DATE_FORMATTER);
    }

    /**
     * Starts the main activity. It also specifies which tab should be opened in
     * the main activity depending on the actions performed in this activity.
     */
    private void goToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(ARG_CALLER_TAB_INDEX, callerTabIndex);
        startActivity(i);
    }

    /**
     * Checks whether an editing attempt is made and if that's the case, it prepares
     * the data of the task for editing. Finished tasks can't be edited and this method
     * takes that into account.
     */
    private void loadPotentialTaskData() {
        Bundle taskData = getIntent().getExtras();
        if(taskData != null) {
            currentTask = new Task();
            currentTask.setDataFromBundle(taskData);

            if(currentTask.getTitle() != null) {
                taskTitleEditText.setText(currentTask.getTitle());
            }

            if(currentTask.getDescription() != null) {
                taskDescriptionEditText.setText(currentTask.getDescription());
            }

            if(currentTask.getExpiresOn() != null) {
                taskExpiryDateTextView.setText(
                        currentTask.getExpiresOn().format(App.APP_DATE_FORMATTER));
                removeTaskExpiryDateButton.setVisibility(View.VISIBLE);
            }

            if(currentTask.getStatus() != null) {
                /*Where the user should be returned after this activity finishes
                  is based on the status of the opened task.*/
                callerTabIndex = currentTask.getStatus().getValue();

                if(currentTask.getStatus().equals(TaskStatus.Finished)) {
                    showFinishedTaskData();
                }
            }
        }
    }

    /**
     * Reconfigures the layout of this activity in order to only show the finished task.
     * Editing options will be disabled.
     */
    private void showFinishedTaskData() {
        //Rearranges top part of the screen
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams)findViewById(R.id.saveTaskLinearLayoutTop)
                .getLayoutParams();
        marginParams.leftMargin = 0;
        findViewById(R.id.saveTaskBackButtonLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.goBackImageButton).setOnClickListener((view)-> onBackPressed());
        findViewById(R.id.taskCancelButton).setVisibility(View.GONE);
        findViewById(R.id.taskSaveButton).setVisibility(View.GONE);

        //Disables and recolors inputs. Hides unrelated views.
        taskTitleEditText.setEnabled(false);
        taskTitleEditText.setTextColor(
                getResources().getColor(
                        com.google.android.material.R.color.material_dynamic_neutral40, getTheme()));
        taskDescriptionEditText.setEnabled(false);
        taskDescriptionEditText.setTextColor(
                getResources().getColor(
                        com.google.android.material.R.color.material_dynamic_neutral40, getTheme()));
        taskExpiryDateTextView.setVisibility(View.GONE);
        removeTaskExpiryDateButton.setVisibility(View.GONE);

        //Shows completion date and time
        taskCompletedOnTextView.setVisibility(View.VISIBLE);
        String completedOn = getResources().getString(R.string.task_completed_on_text_view_beginning) +
                currentTask.getCompletedOn().format(App.APP_DATE_TIME_FORMATTER);
        taskCompletedOnTextView.setText(completedOn);
    }
}