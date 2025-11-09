package com.example.soundnova.utils

import android.view.View
import android.widget.SeekBar
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom

fun setProgress(progress: Int): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(SeekBar::class.java)
        }

        override fun getDescription(): String {
            return "Set progress on SeekBar to $progress"
        }

        override fun perform(uiController: UiController, view: View) {
            val seekBar = view as SeekBar
            seekBar.progress = progress
        }
    }
}
