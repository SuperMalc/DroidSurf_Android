package com.droidsurf.hostservice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import static com.droidsurf.hostservice.ActivityMain.SHARED_PREFS;
import static com.droidsurf.hostservice.CreditDialog.URL1;

public class IntroActivity extends AppCompatActivity {

    private static final String INTRO = null;

    // ScreenPager
    private ViewPager screenPager;
    IntroViewPagerAdapter introViewPagerAdapter;
    TabLayout tabIndicator;
    Button btnNext;
    TextView tvSkip;
    int position = 0;
    Button btnGetStarted;
    Animation btnAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
        // make the activity on full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
         */

        // when this activity is about to be launched we need to check if it has been opened or not
        if (restorePrefData()) {
            Intent mainActivity = new Intent(getApplicationContext(), ActivityMain.class);
            startActivity(mainActivity);
            finish();
        }

        setContentView(R.layout.activity_intro);

        /**
        // hide the action bar
        getSupportActionBar().hide();
         */

        // ini views
        tvSkip = findViewById(R.id.tv_skip);
        btnNext = findViewById(R.id.btn_next);
        btnGetStarted = findViewById(R.id.btn_get_started);
        tabIndicator = findViewById(R.id.tab_indicator);
        btnAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_animation);

        String welcomeMsg = getResources().getString(R.string.intro_1);
        String welcomeText = getResources().getString(R.string.intro_1A);
        String introMsg2 = getResources().getString(R.string.intro_2);
        String introMsg2A = getResources().getString(R.string.intro_2A);
        String intro_server = getResources().getString(R.string.intro_server);
        String intro_server2 = getResources().getString(R.string.intro_server2);


        // fill list screen
        final List<ScreenItem> mList = new ArrayList<>();
        mList.add(new ScreenItem(welcomeMsg, welcomeText, R.drawable.app_icon_512));
        mList.add(new ScreenItem(introMsg2,introMsg2A,R.drawable.connect2_512));
        mList.add(new ScreenItem(intro_server,intro_server2,R.drawable.computer_512));

        // setup viewpager
        screenPager = (ViewPager) findViewById(R.id.screen_viewpager);
        introViewPagerAdapter = new IntroViewPagerAdapter(this, mList);
        screenPager.setAdapter(introViewPagerAdapter);

        // setup tablayout with viewpaper
        tabIndicator.setupWithViewPager(screenPager);

        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open main activity
                Intent mainActivity = new Intent(getApplicationContext(), ActivityMain.class);
                startActivity(mainActivity);
                // also we need to save a boolean value to storage so next time when the user run the app
                // we could know that he is already checked the intro activity
                // i'm going to use shared preferences to thant process
                savePrefsData();
                finish();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = screenPager.getCurrentItem();
                if (position < mList.size()) {

                    position++;
                    screenPager.setCurrentItem(position);
                }

                if (position == mList.size() - 1) { //when we reach the last screen

                    //TODO : show the GETSTARTED Button and hide the indicator and the next button

                    loadLastScreen();
                }
            }
        });
        // tablayout add change listener

        tabIndicator.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == mList.size()-1) {

                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Get Started button click listener
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open main activity
                Intent mainActivity = new Intent(getApplicationContext(), ActivityMain.class);
                startActivity(mainActivity);
                // also we need to save a boolean value to storage so next time when the user run the app
                // we could know that he is already checked the intro activity
                // i'm going to use shared preferences to thant process
                savePrefsData();
                finish();
            }
        });
    }

    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        boolean isIntroActivityOpenedBefore = pref.getBoolean(INTRO, false);
        return isIntroActivityOpenedBefore;
    }

    private void savePrefsData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(INTRO, true);
        editor.commit();
    }

    // show the GETSTARTED Button and hide the indicator and the next button

    private void loadLastScreen() {
        btnNext.setVisibility(View.INVISIBLE);
        btnGetStarted.setVisibility(View.VISIBLE);
        tabIndicator.setVisibility(View.INVISIBLE);
        // TODO : ADD an animation the getstarted button
        // setup animation
        btnGetStarted.setAnimation(btnAnim);
    }
}