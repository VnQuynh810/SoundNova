package com.example.soundnova

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.soundnova.screens.music_player.MusicPlayerFragment
import com.example.soundnova.utils.setProgress
import com.example.soundnova.utils.withDrawable
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class PlayMusicTest {

    @get:Rule
    val fragmentScenarioRule = FragmentScenario.launchInContainer(MusicPlayerFragment::class.java)

    @Test
    fun testInitialSetup() {
        // Kiểm tra rằng song_name hiển thị đúng
        onView(withId(R.id.song_name)).check(matches(withText(R.string.song_name)))

        // Kiểm tra rằng song_artist hiển thị đúng
        onView(withId(R.id.song_artist)).check(matches(withText(R.string.song_artist)))

        // Kiểm tra rằng SeekBar được hiển thị
        onView(withId(R.id.seekBar)).check(matches(isDisplayed()))
    }

    @Test
    fun testPlayMusic() {
        // Nhấn vào nút Play
        onView(withId(R.id.play_pause)).perform(click())

        // Kiểm tra trạng thái nút đã chuyển sang Pause
        onView(withId(R.id.play_pause)).check(matches(withDrawable(R.drawable.icon_pause)))

        // Kiểm tra rằng SeekBar đã bắt đầu thay đổi
        onView(withId(R.id.seekBar)).check(matches(isEnabled()))
    }

    @Test
    fun testPauseMusic() {
        // Giả sử nhạc đang phát, nhấn nút Pause
        onView(withId(R.id.play_pause)).perform(click())

        // Kiểm tra trạng thái nút đã chuyển sang Play
        onView(withId(R.id.play_pause)).check(matches(withDrawable(R.drawable.icon_play)))
    }

    @Test
    fun testResumeMusic() {
        // Nhấn nút Play để phát nhạc
        onView(withId(R.id.play_pause)).perform(click())

        // Tạm dừng nhạc
        onView(withId(R.id.play_pause)).perform(click())

        // Tiếp tục phát nhạc
        onView(withId(R.id.play_pause)).perform(click())

        // Kiểm tra trạng thái nút là Pause
        onView(withId(R.id.play_pause)).check(matches(withDrawable(R.drawable.icon_pause)))
    }

    @Test
    fun testNextTrack() {
        // Nhấn nút Next
        onView(withId(R.id.id_next)).perform(click())

        // Kiểm tra rằng tên bài hát đã thay đổi
        onView(withId(R.id.song_name)).check(matches(not(withText(R.string.song_name))))
    }

    @Test
    fun testPreviousTrack() {
        // Nhấn nút Previous
        onView(withId(R.id.id_prev)).perform(click())

        // Kiểm tra rằng tên bài hát đã thay đổi
        onView(withId(R.id.song_name)).check(matches(not(withText(R.string.song_name))))
    }

    @Test
    fun testVolumeControl() {
        // Tìm kiếm thanh SeekBar
        onView(withId(R.id.seekBar)).perform(setProgress(50)) // Đặt giá trị âm lượng thành 50%

        // Kiểm tra rằng SeekBar đã thay đổi
        onView(withId(R.id.seekBar)).check(matches(isEnabled()))
    }

    @Test
    fun testEndOfTrack() {
        // Giả lập hết bài hát (SeekBar đạt giá trị max)
        onView(withId(R.id.seekBar)).perform(setProgress(100))

        // Kiểm tra rằng bài hát tiếp theo được phát
        onView(withId(R.id.song_name)).check(matches(not(withText(R.string.song_name))))
    }

    @Test
    fun testNotificationPlay() {
        // Nhấn nút Play
        onView(withId(R.id.play_pause)).perform(click())

        // Kiểm tra thông báo hiển thị trạng thái đang phát
        onView(withText("Now Playing")).check(matches(isDisplayed()))
    }

    @Test
    fun testNotificationPause() {
        // Nhấn nút Pause
        onView(withId(R.id.play_pause)).perform(click())

        // Kiểm tra thông báo hiển thị trạng thái đã tạm dừng
        onView(withText("Paused")).check(matches(isDisplayed()))
    }

    @Test
    fun testTrackLoadError() {
        // Giả lập tình huống lỗi tải nhạc
        onView(withId(R.id.song_name)).check(matches(withText("Error loading track")))
    }

    @Test
    fun testUIUpdates() {
        // Nhấn nút Play
        onView(withId(R.id.play_pause)).perform(click())

        // Kiểm tra rằng tên bài hát hiển thị đúng
        onView(withId(R.id.song_name)).check(matches(withText("Song Title")))

        // Kiểm tra rằng thời gian đã phát hiển thị đúng
        onView(withId(R.id.durationPlayed)).check(matches(not(withText("00:00"))))
    }
}
