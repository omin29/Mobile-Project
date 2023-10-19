package com.example.taskmanager.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
                        App.getContext().getResources().getString(R.string.task_expiry_date_text_view_default):taskDate.format(App.APP_DATE_FORMATTER));

        if(taskDate == null) {
            holder.taskExpiresOn.setCompoundDrawables(null, null, null, null);
        }
        else {
            holder.taskExpiresOn.setCompoundDrawablesWithIntrinsicBounds(
                    AppCompatResources.getDrawable(holder.taskExpiresOn.getContext(), R.drawable.ic_clock),
                    null, null, null);
        }

        holder.completeTodoTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(holder.completeTodoTaskButton.getContext(),
                        String.format("You tried to complete \"%s\"", holder.task.getTitle()),
                        Toast.LENGTH_LONG).show();
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
                            Toast.makeText(holder.deleteTodoTaskButton.getContext(),
                                String.format("You tried to delete \"%s\"", holder.task.getTitle()),
                                Toast.LENGTH_LONG).show();
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
                Toast.makeText(holder.itemView.getContext(),
                        String.format("You tried to open \"%s\"", holder.task.getTitle()),
                        Toast.LENGTH_LONG).show();
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