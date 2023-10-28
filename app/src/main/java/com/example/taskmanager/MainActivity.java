package com.example.taskmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.taskmanager.utility.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    protected TabLayout tabs;
    protected ViewPager2 viewPager;
    protected ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabs = findViewById(R.id.mainTabs);
        viewPager = findViewById(R.id.viewPager);
        //This adapter is needed to load the correct fragment.
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            /**
             * Tells the view pager to load the corresponding fragment when
             * the tab is changed.
             * @param tab The tab that was selected
             */
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //Makes sure the selected page corresponds to its tab
                TabLayout.Tab tab = tabs.getTabAt(position);
                if(tab != null) {
                    tab.select();
                }
            }
        });

        //Selects a specific tab if one was chosen for the creation of the activity
        tabs.selectTab(tabs.getTabAt(getIntent().getIntExtra(SaveTaskActivity.ARG_CALLER_TAB_INDEX, 0)));
    }

    /**
     * Starts the activity which is responsible for task creation and editing.
     * @param v The clicked view
     */
    public void openSaveTaskHandler(View v) {
        Intent i = new Intent(this, SaveTaskActivity.class);
        startActivity(i);
    }

    /**
     * Start the activity which provides inspirational quotes to the user.
     * @param v The clicked view
     */
    public void openQuotesHandler(View v) {
        Intent i = new Intent(this, QuotesActivity.class);
        startActivity(i);
    }
}