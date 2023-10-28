package com.example.taskmanager.utility;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.taskmanager.fragments.TaskFragment;
import com.example.taskmanager.task.TaskStatus;

/**
 * Adapter which is responsible for showing the correct fragment in
 * the app's view pager.
 */
public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new TaskFragment(TaskStatus.TODO);
            case 1:
                return new TaskFragment(TaskStatus.Finished);
            case 2:
                return new TaskFragment(TaskStatus.Failed);
            default:
                return new TaskFragment();
        }
    }

    /*Note that the item count must match the number of returnable fragments for correct behavior.
      It should also match your tab count.*/
    @Override
    public int getItemCount() {
        return 3;
    }
}
