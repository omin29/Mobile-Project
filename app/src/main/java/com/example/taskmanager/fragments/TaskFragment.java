package com.example.taskmanager.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.taskmanager.R;
import com.example.taskmanager.task.Task;
import com.example.taskmanager.task.TaskStatus;
import com.example.taskmanager.utility.App;
import com.example.taskmanager.utility.DatabaseHelper;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class TaskFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_TASK_FILTER = "task-filter";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private TaskStatus mTaskFilter = null;

    private MyTaskRecyclerViewAdapter adapter = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskFragment() {
    }

    public TaskFragment(TaskStatus taskStatus) {
        this();
        this.mTaskFilter = taskStatus;
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TaskFragment newInstance(int columnCount, TaskStatus taskStatus) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(ARG_TASK_FILTER, taskStatus.name());
        fragment.setArguments(args);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();
        refreshTabList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mTaskFilter = TaskStatus.valueOf(getArguments().getString(ARG_TASK_FILTER));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo_task_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            DatabaseHelper _db = null;
            try {
                _db = new DatabaseHelper(context);

                if(mTaskFilter != null) {
                    recyclerView.setAdapter(new MyTaskRecyclerViewAdapter(_db.selectTasks(mTaskFilter)));
                }
                else {
                    recyclerView.setAdapter(new MyTaskRecyclerViewAdapter(_db.selectTasks(null)));
                }

                if(recyclerView.getAdapter() != null) {
                    adapter = (MyTaskRecyclerViewAdapter)recyclerView.getAdapter();
                }
            }
            catch (Exception e) {
                Toast.makeText(context, getResources().getString(R.string.tasks_load_failure_message),
                        Toast.LENGTH_LONG).show();
            }
            finally {
                if(_db != null){
                    _db.close();
                    _db = null;
                }
            }
        }
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void refreshTabList() {
        if (adapter != null) {
            DatabaseHelper _db = null;
            try {
                _db = new DatabaseHelper(getContext());
                List<Task> tasks = adapter.getmValues();
                List<Task> currentTasks = _db.selectTasks(mTaskFilter);
                tasks.clear();
                tasks.addAll(currentTasks);
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                Toast.makeText(getContext(), getResources().getString(R.string.tasks_load_failure_message),
                        Toast.LENGTH_LONG).show();
            } finally {
                if (_db != null) {
                    _db.close();
                    _db = null;
                }
            }
        }
    }
}