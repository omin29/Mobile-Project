package com.example.taskmanager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanager.task.Task;
import com.example.taskmanager.task.TaskStatus;
import com.example.taskmanager.utility.App;
import com.example.taskmanager.utility.DatabaseHelper;

import java.time.LocalDate;

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

        loadPotentialTaskData();
    }

    public void cancelTaskCreationHandler(View v) {
        goToMainActivity();
    }

    public void setExpiryDateHandler(View v) {
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
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
            }
        }, LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1, LocalDate.now().getDayOfMonth());
        //We subtract 1 from month value above because the date dialog works with values between 0 and 11.

        dialog.show();
    }

    public void removeTaskExpiryDateHandler(View v) {
        taskExpiryDateTextView.setText(getResources().getString(R.string.task_expiry_date_text_view_default));
        removeTaskExpiryDateButton.setVisibility(View.GONE);
    }

    public void taskSaveHandler(View v) {
        //Insert
        /*if(currentTask == null) {
            try {
                currentTask = new Task(
                        taskTitleEditText.getText().toString(),
                        taskDescriptionEditText.getText().toString(),
                        getLocalDateFromFormattedText(taskExpiryDateTextView.getText().toString())
                );
                currentTask.validate();
                _db = new DatabaseHelper(this);
                _db.insertTask(currentTask);
                Toast.makeText(this,
                        getResources().getString(R.string.task_save_success_message),
                        Toast.LENGTH_LONG).show();
                goToMainActivity();
            }
            catch (Exception e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                //Log.d("INSERT_EXCEPTION", e.getLocalizedMessage());
            }
            finally {
                if(_db != null) {
                    _db.close();
                    _db = null;
                }
                currentTask = null;
            }
        }*/

        try {
            if(currentTask == null) {
                currentTask = new Task(
                        taskTitleEditText.getText().toString(),
                        taskDescriptionEditText.getText().toString(),
                        getLocalDateFromFormattedText(taskExpiryDateTextView.getText().toString())
                );
            }
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

            if(currentTask != null && currentTask.getId() == 0) {
                currentTask = null;
            }
        }
    }

    @Nullable
    private LocalDate getLocalDateFromFormattedText(String formattedText) {
        if(formattedText.equals(getResources().getString(R.string.task_expiry_date_text_view_default))) {
            return null;
        }

        LocalDate date = LocalDate.parse(formattedText, App.APP_DATE_FORMATTER);
        return date;
    }

    private void goToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(ARG_CALLER_TAB_INDEX, callerTabIndex);
        startActivity(i);
    }

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
                callerTabIndex = currentTask.getStatus().getValue();

                if(currentTask.getStatus().equals(TaskStatus.Finished)) {
                    showFinishedTaskData();
                }
            }
        }
    }

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