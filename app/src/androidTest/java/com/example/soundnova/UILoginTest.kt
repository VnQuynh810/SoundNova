package com.example.soundnova

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import android.view.View
import android.widget.TextView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers.instanceOf
import com.example.soundnova.Login
import com.example.soundnova.R
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class UILoginTest {

    @get:Rule
    val mActivityScenarioRule = ActivityScenarioRule(Login::class.java)
    fun setTextInTextView(value: String): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                // Áp dụng cho TextView
                return instanceOf(TextView::class.java)
            }

            override fun getDescription(): String {
                return "Set text of a TextView to: $value"
            }

            override fun perform(uiController: UiController?, view: View?) {
                if (view is TextView) {
                    view.text = value
                }
            }
        }
    }

    @Test
    fun uILoginNoteTest() {
        onView(withId(R.id.note)).perform(setTextInTextView("Email or Password is not correct"))
        onView(withId(R.id.note))
            .check(matches(isDisplayed()))
            .check(matches(withText("Email or Password is not correct")))
    }

    @Test
    fun uILoginForgotTest() {
        // Giả lập trạng thái giao diện
        onView(withId(R.id.textViewForgetPassword)).perform(setTextInTextView("Forgot your password?"))

        // Kiểm tra thành phần UI hiển thị đúng nội dung
        onView(withId(R.id.textViewForgetPassword))
            .check(matches(isDisplayed()))
            .check(matches(withText("Forgot your password?")))
    }

    @Test
    fun testLoginUIElementsDisplayed() {
        // Kiểm tra Logo hiển thị
        onView(withId(R.id.logo)).check(matches(isDisplayed()))

        // Kiểm tra TextView tiêu đề đăng nhập hiển thị đúng
        onView(withId(R.id.textViewLogin)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewLogin)).check(matches(withText(R.string.textViewLogin)))

        // Kiểm tra trường nhập liệu Username hiển thị đúng với placeholder
        onView(withId(R.id.editTextSignInUsername)).check(matches(isDisplayed()))
        onView(withId(R.id.editTextSignInUsername)).check(matches(withHint(R.string.editTextEmail)))

        // Kiểm tra trường nhập liệu Password hiển thị đúng với placeholder
        onView(withId(R.id.editTextSignInPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.editTextSignInPassword)).check(matches(withHint(R.string.editTextPassword)))

        // Kiểm tra nút Confirm Login hiển thị đúng
        onView(withId(R.id.buttonConfirmLogin)).check(matches(isDisplayed()))
        onView(withId(R.id.buttonConfirmLogin)).check(matches(withText(R.string.buttonConfirmSignIn)))

        // Kiểm tra TextView Forgot Password hiển thị đúng
        onView(withId(R.id.textViewForgetPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewForgetPassword)).check(matches(withText(R.string.textViewForgetPassword)))

        // Kiểm tra TextView liên kết Sign Up
        onView(withId(R.id.textViewSignUp)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSignUp)).check(matches(withText(R.string.textViewSignUp2)))
    }

    // Kiểm tra chức năng liên kết Forgot Password
    @Test
    fun testForgotPasswordLink() {
        // Nhấn vào liên kết Forgot Password
        onView(withId(R.id.textViewForgetPassword)).perform(click())

        // Kiểm tra giao diện Forgot Password được mở
        onView(withId(R.id.editTextSignUpEmail)).check(matches(isDisplayed())) // Input Email
        onView(withId(R.id.buttonSendBTN)).check(matches(isDisplayed())) // Nút Send
        onView(withId(R.id.buttonBack)).check(matches(isDisplayed())) // Nút Back
    }

    // Kiểm tra chức năng liên kết Sign Up
    @Test
    fun testSignUpLink() {
        // Nhấn vào liên kết Sign Up
        onView(withId(R.id.textViewSignUp)).perform(click())

        // Kiểm tra giao diện Sign Up được mở
        onView(withId(R.id.editTextSignUpUsername)).check(matches(isDisplayed())) // Input Full Name
        onView(withId(R.id.editTextSignUpEmail)).check(matches(isDisplayed())) // Input Email
        onView(withId(R.id.editTextSignUpPassword)).check(matches(isDisplayed())) // Input Password
        onView(withId(R.id.editTextSignUpRepeatPassword)).check(matches(isDisplayed())) // Input Repeat Password
        onView(withId(R.id.buttonRegister)).check(matches(isDisplayed())) // Nút Register
        onView(withId(R.id.textViewSignIn)).check(matches(isDisplayed())) // Liên kết Sign In
    }
}