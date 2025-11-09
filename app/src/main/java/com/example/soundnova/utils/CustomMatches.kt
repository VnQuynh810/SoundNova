package com.example.soundnova.utils

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

fun withDrawable(resourceId: Int): Matcher<View> {
    return object : BoundedMatcher<View, ImageView>(ImageView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with drawable resource id: $resourceId")
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        override fun matchesSafely(imageView: ImageView): Boolean {
            val expectedDrawable = imageView.context.getDrawable(resourceId) as? BitmapDrawable
            val actualDrawable = imageView.drawable as? BitmapDrawable
            return expectedDrawable?.bitmap?.sameAs(actualDrawable?.bitmap) == true
        }
    }
}
