package com.example.soundnova

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class UISignupTest {

    @get:Rule
    val fragmentScenarioRule = FragmentScenario.launchInContainer(Register::class.java)

    @Test
    fun testUIElementsDisplayed() {
        // Kiểm tra logo hiển thị
        onView(withId(R.id.logo)).check(matches(isDisplayed()))

        // Kiểm tra các input fields hiển thị
        onView(withId(R.id.editTextSignUpUsername)).check(matches(isDisplayed()))
        onView(withId(R.id.editTextSignUpUsername)).check(matches(withHint(R.string.editTextSignUpUsername)))

        onView(withId(R.id.editTextSignUpEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.editTextSignUpEmail)).check(matches(withHint(R.string.editTextSignUpEmail)))



        onView(withId(R.id.editTextSignUpPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.editTextSignUpPassword)).check(matches(withHint(R.string.editTextPassword)))

        onView(withId(R.id.editTextSignUpRepeatPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.editTextSignUpRepeatPassword)).check(matches(withHint(R.string.editTextRepeatPassword)))

        // Kiểm tra nút đăng ký hiển thị
        onView(withId(R.id.buttonRegister)).check(matches(isDisplayed()))
        onView(withId(R.id.buttonRegister)).check(matches(withText(R.string.buttonRegister)))

        // Kiểm tra liên kết đăng nhập tài khoản
        onView(withId(R.id.textViewSignIn)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSignIn)).check(matches(withText(R.string.buttonConfirmSignIn)))
    }

    @Test
    fun testSignUpFunctionality() {
        // Nhập thông tin hợp lệ
        onView(withId(R.id.editTextSignUpUsername)).perform(typeText("John Doe"), closeSoftKeyboard())
        onView(withId(R.id.editTextSignUpEmail)).perform(typeText("john.doe@example.com"), closeSoftKeyboard())

        onView(withId(R.id.editTextSignUpPassword)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.editTextSignUpRepeatPassword)).perform(typeText("password123"), closeSoftKeyboard())

        // Nhấn nút đăng ký
        onView(withId(R.id.buttonRegister)).perform(click())

        // Kiểm tra thông báo hoặc giao diện sau đăng ký thành công
        onView(withId(R.id.note)).check(matches(withText("Sign up successful")))
    }

    @Test
    fun testSignUpWithEmptyFields() {
        // Để trống tất cả các trường và nhấn nút đăng ký
        onView(withId(R.id.buttonRegister)).perform(click())

        // Kiểm tra thông báo lỗi
        onView(withId(R.id.note)).check(matches(withText("Please fill out all fields")))
    }

    @Test
    fun testSignUpWithInvalidEmail() {
        // Nhập thông tin với email không hợp lệ
        onView(withId(R.id.editTextSignUpUsername)).perform(typeText("John Doe"), closeSoftKeyboard())
        onView(withId(R.id.editTextSignUpEmail)).perform(typeText("invalid-email"), closeSoftKeyboard())

        onView(withId(R.id.editTextSignUpPassword)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.editTextSignUpRepeatPassword)).perform(typeText("password123"), closeSoftKeyboard())

        // Nhấn nút đăng ký
        onView(withId(R.id.buttonRegister)).perform(click())

        // Kiểm tra thông báo lỗi
        onView(withId(R.id.note)).check(matches(withText("Invalid email address")))
    }

    @Test
    fun testSignUpWithMismatchedPasswords() {
        // Nhập mật khẩu và xác nhận mật khẩu không khớp
        onView(withId(R.id.editTextSignUpUsername)).perform(typeText("John Doe"), closeSoftKeyboard())
        onView(withId(R.id.editTextSignUpEmail)).perform(typeText("john.doe@example.com"), closeSoftKeyboard())

        onView(withId(R.id.editTextSignUpPassword)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.editTextSignUpRepeatPassword)).perform(typeText("differentpassword"), closeSoftKeyboard())

        // Nhấn nút đăng ký
        onView(withId(R.id.buttonRegister)).perform(click())

        // Kiểm tra thông báo lỗi
        onView(withId(R.id.note)).check(matches(withText("Passwords do not match")))
    }

    @Test
    fun testSignInLink() {
        // Nhấn vào liên kết "Sign In"
        onView(withId(R.id.textViewSignIn)).perform(click())

        // Kiểm tra xem giao diện đăng nhập được mở
        onView(withId(R.id.editTextSignInUsername)).check(matches(isDisplayed()))
    }
}
