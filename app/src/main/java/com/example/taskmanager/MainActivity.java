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
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
                tabs.getTabAt(position).select();
            }
        });

        //Log.d("TASK_TEST", new Task("Water plants").getExpiresOn().toString());
        /*try {
            Task t = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                t = new Task(
                        "Hello",
                        "Greet people",
                        LocalDate.now()
                        //new String(new char[1201]).replace('\0', 'a')
                );
            }
            Log.d("T_TITLE", t.getTitle());
            t.Validate();
        }
        catch (Exception e){
            Log.d("Validation_Exception", e.getLocalizedMessage());
        }*/
    }

    public void openTaskCreationHandler(View v) {
        Intent i = new Intent(this, SaveTaskActivity.class);
        startActivity(i);
    }
}