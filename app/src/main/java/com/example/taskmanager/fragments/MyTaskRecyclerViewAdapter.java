package com.example.taskmanager.fragments;

import static androidx.core.content.ContextCompat.startActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanager.R;
import com.example.taskmanager.SaveTaskActivity;
import com.example.taskmanager.databinding.FragmentTaskBinding;
import com.example.taskmanager.task.Task;
import com.example.taskmanager.task.TaskStatus;
import com.example.taskmanager.utility.App;
import com.example.taskmanager.utility.DatabaseHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * This utility class helps with the data manipulation of the task list items.
 */
@SuppressLint("NotifyDataSetChanged")
public class MyTaskRecyclerViewAdapter extends RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder> {
    private final List<Task> mValues;
    /**
     * Used for determining the layout and functionalities of each task list item
     */
    private final TaskStatus taskListType;

    public MyTaskRecyclerViewAdapter(@NonNull DatabaseHelper _db, @NonNull TaskStatus taskListType) {
        mValues = _db.selectTasks(taskListType);
        this.taskListType = taskListType;
    }

    public List<Task> getValues() {
        return mValues;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    /*This place is very important because here we can modify the
      task fragment views and add event listeners to these views (ex. onClickListeners).
      These task fragments are reused to create the task list fragment layout.*/
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.task = mValues.get(position);
        holder.taskTitle.setText(mValues.get(position).getTitle());
        LocalDate taskDate = mValues.get(position).getExpiresOn();
        holder.taskExpiresOn.setText(
                (taskDate == null)?
                        App.getContext().getResources().getString(R.string.task_expiry_date_text_view_default):taskDate.format(App.APP_DATE_FORMATTER));

        if(taskListType.equals(TaskStatus.Finished)) {
            holder.taskExpiresOn.setVisibility(View.GONE);
            holder.taskCompletedOn.setVisibility(View.VISIBLE);
            String completedOn = App.getContext().getResources()
                    .getString(R.string.task_completed_on_text_view_beginning) +
                    holder.task.getCompletedOn().format(App.APP_DATE_TIME_FORMATTER);
            holder.taskCompletedOn.setText(completedOn);
        }
        else if(taskDate == null) {
            holder.taskExpiresOn.setCompoundDrawables(null, null, null, null);
        }
        else {
            if(taskListType.equals(TaskStatus.TODO)) {
                holder.taskExpiresOn.setCompoundDrawablesWithIntrinsicBounds(
                        AppCompatResources.getDrawable(holder.taskExpiresOn.getContext(), R.drawable.ic_clock),
                        null, null, null);
            }
            else if(taskListType.equals(TaskStatus.Failed)) {
                holder.taskExpiresOn.setCompoundDrawablesWithIntrinsicBounds(
                        AppCompatResources
                                .getDrawable(holder.taskExpiresOn.getContext(), R.drawable.ic_clock_red),
                        null, null, null);
            }
        }

        if(taskListType.equals(TaskStatus.Finished)) {
            holder.completeTaskButton.setVisibility(View.GONE);
            holder.undoTaskCompletionButton.setVisibility(View.VISIBLE);
            holder.undoTaskCompletionButton.setOnClickListener(view -> {
                DatabaseHelper _db = null;
                Context context = holder.undoTaskCompletionButton.getContext();

                try {
                    holder.task.setStatus(TaskStatus.TODO);
                    holder.task.reevaluateStatus();
                    _db = new DatabaseHelper(context);
                    _db.updateTask(holder.task);
                    mValues.remove(holder.task);
                    notifyDataSetChanged();

                    Toast.makeText(context,
                            App.getContext().getResources().getString(R.string.task_undo_completion_success_message),
                            Toast.LENGTH_LONG).show();

                }
                catch (Exception e) {
                    Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
                finally {
                    if (_db != null) {
                        _db.close();
                    }
                }
            });
        }
        else {
            holder.completeTaskButton.setOnClickListener(view -> {
                DatabaseHelper _db = null;
                Context context = holder.completeTaskButton.getContext();

                try {
                    holder.task.setStatus(TaskStatus.Finished);
                    holder.task.setCompletedOn(LocalDateTime.now());
                    _db = new DatabaseHelper(context);
                    _db.updateTask(holder.task);
                    mValues.remove(holder.task);
                    notifyDataSetChanged();

                    Toast.makeText(context,
                            App.getContext().getResources().getString(R.string.task_complete_success_message),
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                } finally {
                    if (_db != null) {
                        _db.close();
                    }
                }
            });
        }

        holder.deleteTaskButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    holder.deleteTaskButton.getContext());
            builder.setCancelable(true);
            builder.setTitle(App.getContext().getResources()
                    .getString(R.string.task_delete_confirmation_title));
            builder.setMessage(App.getContext().getResources()
                    .getString(R.string.task_delete_confirmation_message));
            builder.setPositiveButton(App.getContext().getResources()
                            .getString(R.string.delete_alert_positive),
                    (dialog, which) ->
                    {

                        Context context = holder.deleteTaskButton.getContext();
                        try (DatabaseHelper _db = new DatabaseHelper(context)) {
                            _db.deleteTask(holder.task);
                            mValues.remove(holder.task);
                            notifyDataSetChanged();

                            Toast.makeText(context,
                                    String.format(App.getContext().getResources()
                                            .getString(R.string.task_delete_success_message)),
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel,
                    (dialog, which) -> dialog.cancel());

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        //Opens a task for editing/viewing details
        holder.itemView.setOnClickListener(view -> {
            Context context = holder.itemView.getContext();
            Intent i = new Intent(context, SaveTaskActivity.class);
            Bundle taskData = holder.task.getBundleData();
            i.putExtras(taskData);
            startActivity(context, i, null);
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }


    /**
     * This utility class helps with the access
     * to the views in the task item fragment. This is the fragment
     * which is used multiple times in the task lists for each tab.
     */
    @SuppressLint("ViewHolder")
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView taskTitle;
        public final TextView taskExpiresOn;
        public final TextView taskCompletedOn;

        public final ImageButton completeTaskButton;
        public final ImageButton undoTaskCompletionButton;
        public final ImageButton deleteTaskButton;
        public Task task;

        public ViewHolder(FragmentTaskBinding binding) {
            super(binding.getRoot());
            taskTitle = binding.taskTitle;
            taskExpiresOn = binding.taskExpiresOn;
            taskCompletedOn = binding.finishedTaskCompletedOn;
            completeTaskButton = binding.completeTaskButton;
            undoTaskCompletionButton = binding.undoTaskCompletionButton;
            deleteTaskButton = binding.deleteTaskButton;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + taskTitle.getText() + "'";
        }
    }
}