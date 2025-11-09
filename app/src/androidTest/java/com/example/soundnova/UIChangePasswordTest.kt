package com.example.soundnova

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class UIChangePasswordTest {

    @get:Rule
    val fragmentScenarioRule = FragmentScenario.launchInContainer(ChangePassword::class.java)

    @Test
    fun testChangePasswordUIElementsDisplayed() {
        // Kiểm tra logo hiển thị
        onView(withId(R.id.logo)).check(matches(isDisplayed()))

        // Kiểm tra TextView "Change Password" hiển thị đúng
        onView(withId(R.id.textViewChangePassword)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewChangePassword)).check(matches(withText(R.string.textViewChangePassword)))

        // Kiểm tra TextView "Please Enter Your Information"
        onView(withId(R.id.textViewPlz)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewPlz)).check(matches(withText(R.string.textViewPlz)))

        // Kiểm tra EditText "Current Password"
        onView(withId(R.id.editTextCurrentPass)).check(matches(isDisplayed()))
        onView(withId(R.id.editTextCurrentPass)).check(matches(withHint(R.string.editTextCurrentPass)))

        // Kiểm tra EditText "New Password"
        onView(withId(R.id.editTextNewPass)).check(matches(isDisplayed()))
        onView(withId(R.id.editTextNewPass)).check(matches(withHint(R.string.editTextNewPass)))

        // Kiểm tra EditText "Confirm New Password"
        onView(withId(R.id.editTextConfirmNewPass)).check(matches(isDisplayed()))
        onView(withId(R.id.editTextConfirmNewPass)).check(matches(withHint(R.string.editTextConfirmNewPass)))

        // Kiểm tra TextView "Note"
        onView(withId(R.id.note)).check(matches(isDisplayed()))
        onView(withId(R.id.note)).check(matches(withText("")))

        // Kiểm tra Button "Send Change"
        onView(withId(R.id.buttonSendChange)).check(matches(isDisplayed()))
        onView(withId(R.id.buttonSendChange)).check(matches(withText(R.string.buttonSendChange)))

        // Kiểm tra Button "Cancel"
        onView(withId(R.id.buttonCancel)).check(matches(isDisplayed()))
        onView(withId(R.id.buttonCancel)).check(matches(withText(R.string.cancel)))
    }

    @Test
    fun testChangePasswordFunctionality() {
        // Nhập mật khẩu hiện tại
        onView(withId(R.id.editTextCurrentPass)).perform(typeText("currentPass123"), closeSoftKeyboard())

        // Nhập mật khẩu mới
        onView(withId(R.id.editTextNewPass)).perform(typeText("newPass456"), closeSoftKeyboard())

        // Nhập lại mật khẩu mới
        onView(withId(R.id.editTextConfirmNewPass)).perform(typeText("newPass456"), closeSoftKeyboard())

        // Nhấn nút "Send Change"
        onView(withId(R.id.buttonSendChange)).perform(click())

        // Kiểm tra rằng thông báo thay đổi mật khẩu được hiển thị
        onView(withId(R.id.note)).check(matches(withText("Password changed successfully!")))
    }

    @Test
    fun testCancelFunctionality() {
        fun testCancelFunctionality() {
            // Nhấn nút "Cancel"
            onView(withId(R.id.buttonCancel)).perform(click())

            // Kiểm tra rằng HomeActivity đã được mở
            onView(withId(R.id.textWelcome)).check(matches(isDisplayed()))

            // Kiểm tra rằng RecyclerView "Recommend Songs" cũng hiển thị (tùy chọn)
            onView(withId(R.id.recyclerViewRecommendSongs)).check(matches(isDisplayed()))
        }
    }


}
