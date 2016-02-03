package com.amrutpatil.moviebuff;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


/**
 * Created by Amrut on 2/2/16.
 */
@RunWith(AndroidJUnit4.class)

public class MainActivityEspressoTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    //Check if sort by is clickable
    @Test
    public void checkViewLoaded() {
        onView(withId(R.id.action_sort_by)).perform(click());
    }
}
