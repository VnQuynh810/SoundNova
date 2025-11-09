package com.example.soundnova

import androidx.fragment.app.testing.FragmentScenario
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.soundnova.screens.home_screen.HomeFragment
import com.example.soundnova.screens.music_player.MusicPlayerFragment
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test

class UIHomeTest {

//    @get:Rule
//    val activityRule = ActivityScenarioRule(HomeActivity::class.java)

    @get:Rule
    val fragmentScenarioRule = FragmentScenario.launchInContainer(HomeFragment::class.java)

    @Test
    fun testHomeUIElementsDisplayed() {
        // Kiểm tra TextView "Welcome"
        onView(withId(R.id.textWelcome)).check(matches(isDisplayed()))
        onView(withId(R.id.textWelcome)).check(matches(withText(R.string.textWelcome)))

        // Kiểm tra TextView "Feel Like Today"
        onView(withId(R.id.textFeelLikeToday)).check(matches(isDisplayed()))
        onView(withId(R.id.textFeelLikeToday)).check(matches(withText(R.string.textFeelLikeToday)))

        // Kiểm tra TextView "Recommendation"
        onView(withId(R.id.textRecommendation)).check(matches(isDisplayed()))
        onView(withId(R.id.textRecommendation)).check(matches(withText("Recommendation")))

        // Kiểm tra RecyclerView "Recommend Songs"
        onView(withId(R.id.recyclerViewRecommendSongs)).check(matches(isDisplayed()))

        // Kiểm tra TextView "Recent"
        onView(withId(R.id.textRecent)).check(matches(isDisplayed()))
        onView(withId(R.id.textRecent)).check(matches(withText(R.string.tabRecent)))

        // Kiểm tra RecyclerView "Recent Songs"
        onView(withId(R.id.recyclerViewRecentSongs)).check(matches(isDisplayed()))

        // Kiểm tra TextView "Popular Albums"
        onView(withId(R.id.textPopularAlbums)).check(matches(isDisplayed()))
        onView(withId(R.id.textPopularAlbums)).check(matches(withText("Popular albums")))

        // Kiểm tra RecyclerView "Popular Albums"
        onView(withId(R.id.recyclerViewPopularAlbums)).check(matches(isDisplayed()))

        // Kiểm tra TextView "Favorite Artists"
        onView(withId(R.id.textFavoriteArtists)).check(matches(isDisplayed()))
        onView(withId(R.id.textFavoriteArtists)).check(matches(withText(R.string.textFavorite)))

        // Kiểm tra RecyclerView "Favorite Artists"
        onView(withId(R.id.recyclerViewFavoriteArtists)).check(matches(isDisplayed()))

    }

    @Test
    fun testRecyclerViewItemClick() {
        // Nhấn vào item đầu tiên trong "Recommend Songs"
        onView(withId(R.id.recyclerViewRecommendSongs))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Kiểm tra rằng giao diện "player_activity.xml" đã được hiển thị thông qua các ID đặc trưng
        onView(withId(R.id.song_name)).check(matches(isDisplayed()))
        onView(withId(R.id.song_artist)).check(matches(isDisplayed()))
        onView(withId(R.id.cover_art)).check(matches(isDisplayed()))

        // Kiểm tra rằng tên bài hát và nghệ sĩ không rỗng (hoặc có giá trị hợp lệ)
        onView(withId(R.id.song_name)).check(matches(not(withText(""))))
        onView(withId(R.id.song_artist)).check(matches(not(withText(""))))
    }

}