package com.example.taskmanager.fragments;

import static androidx.core.content.ContextCompat.startActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.taskmanager.databinding.FragmentTodoTaskBinding;
import com.example.taskmanager.task.Task;
import com.example.taskmanager.task.TaskStatus;
import com.example.taskmanager.utility.App;
import com.example.taskmanager.utility.DatabaseHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@SuppressLint("NotifyDataSetChanged")
public class MyTaskRecyclerViewAdapter extends RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder> {
    private final List<Task> mValues;
    private TaskStatus taskListType;

    public MyTaskRecyclerViewAdapter(@NonNull DatabaseHelper _db, @NonNull TaskStatus taskListType) throws Exception {
        mValues = _db.selectTasks(taskListType);
        this.taskListType = taskListType;
    }

    public List<Task> getmValues() {
        return mValues;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentTodoTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.task = mValues.get(position);
        holder.taskTitle.setText(mValues.get(position).getTitle());
        LocalDate taskDate = mValues.get(position).getExpiresOn();
        holder.taskExpiresOn.setText(
                (taskDate == null)?
                        App.getContext().getResources().getString(R.string.task_expiry_date_text_view_default):taskDate.format(App.APP_DATE_FORMATTER));

        if(taskDate == null) {
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

        holder.completeTodoTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper _db = null;
                Context context = holder.completeTodoTaskButton.getContext();

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
                }
                catch (Exception e) {
                    Toast.makeText(context, e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
                finally {
                    if(_db != null) {
                        _db.close();
                        _db = null;
                    }
                }
            }
        });

        holder.deleteTodoTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        holder.deleteTodoTaskButton.getContext());
                builder.setCancelable(true);
                builder.setTitle(App.getContext().getResources()
                        .getString(R.string.task_delete_confirmation_title));
                builder.setMessage(App.getContext().getResources()
                        .getString(R.string.task_delete_confirmation_message));
                builder.setPositiveButton(App.getContext().getResources()
                                .getString(R.string.delete_alert_positive),
                        (dialog, which) ->
                        {
                            DatabaseHelper _db = null;
                            Context context = holder.deleteTodoTaskButton.getContext();

                            try {
                                _db = new DatabaseHelper(context);
                                _db.deleteTask(holder.task);
                                mValues.remove(holder.task);
                                notifyDataSetChanged();

                                Toast.makeText(context,
                                        String.format(App.getContext().getResources()
                                                .getString(R.string.task_delete_success_message)),
                                        Toast.LENGTH_LONG).show();
                            }
                            catch(Exception e) {
                                Toast.makeText(context, e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                            }
                            finally {
                                if(_db != null) {
                                    _db.close();
                                    _db = null;
                                }
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> dialog.cancel());

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = holder.itemView.getContext();
                Intent i = new Intent(context, SaveTaskActivity.class);
                Bundle taskData = holder.task.getBundleData();
                i.putExtras(taskData);
                startActivity(context, i, null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView taskTitle;
        public final TextView taskExpiresOn;

        public final ImageButton completeTodoTaskButton;
        public final ImageButton deleteTodoTaskButton;
        public Task task;

        public ViewHolder(FragmentTodoTaskBinding binding) {
            super(binding.getRoot());
            taskTitle = binding.todoTaskTitle;
            taskExpiresOn = binding.todoTaskExpiresOn;
            completeTodoTaskButton = binding.completeTodoTaskButton;
            deleteTodoTaskButton = binding.deleteTodoTaskButton;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + taskTitle.getText() + "'";
        }
    }
}