package com.example.taskmanager.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.taskmanager.R;
import com.example.taskmanager.databinding.FragmentTodoTaskBinding;
import com.example.taskmanager.task.Task;
import com.example.taskmanager.utility.App;

import java.time.LocalDate;
import java.util.List;

public class MyTaskRecyclerViewAdapter extends RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder> {

    private final List<Task> mValues;

    public MyTaskRecyclerViewAdapter(List<Task> items) {
        mValues = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentTodoTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.task = mValues.get(position);
        holder.taskTitle.setText(mValues.get(position).getTitle());
        LocalDate taskDate = mValues.get(position).getExpiresOn();
        holder.taskExpiresOn.setText(
                (taskDate == null)?
                        App.getContext().getResources().getString(R.string.task_expiry_date_text_view_default)
                        :
                        (App.getContext().getResources().getString(R.string.task_expires_on_beginning) +
                                taskDate.format(App.APP_DATE_FORMATTER)));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView taskTitle;
        public final TextView taskExpiresOn;
        public Task task;

        public ViewHolder(FragmentTodoTaskBinding binding) {
            super(binding.getRoot());
            taskTitle = binding.todoTaskTitle;
            taskExpiresOn = binding.todoTaskExpiresOn;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + taskTitle.getText() + "'";
        }
    }
}