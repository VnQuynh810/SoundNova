package com.example.soundnova

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.action.ViewActions
import org.hamcrest.CoreMatchers.containsString
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.After
import org.junit.Before
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
open class BaseTest {

    protected val idlingResource = CountingIdlingResource("idlingResource")

    @Before
    open fun setUp() {
        // Tăng thời gian chờ
        IdlingPolicies.setIdlingResourceTimeout(60, TimeUnit.SECONDS)
        IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS)

        // Đăng ký IdlingResource
        IdlingRegistry.getInstance().register(idlingResource)
    }

    @After
    open fun tearDown() {
        // Hủy đăng ký IdlingResource
        IdlingRegistry.getInstance().unregister(idlingResource)
    }
}

class SignInTest : BaseTest() {

    @get:Rule
    val activityRule = ActivityScenarioRule(Login::class.java)

    @Test
    fun testLoginSuccess() {
//        idlingResource.increment()
        Log.d("SignInTest", "Trước khi nhập username")
        onView(withId(R.id.editTextSignInUsername))
                .check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.editTextSignInUsername))
            .perform(typeText("testhm@gmail.com"), ViewActions.closeSoftKeyboard())
        Log.d("SignInTest", "Đã nhập username")
        onView(withId(R.id.editTextSignInPassword))
            .perform(typeText("Abc123456@"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.buttonConfirmLogin))
               .check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.buttonConfirmLogin)).perform(click())

    }

    @Test
    fun testValidUserInvalidPassword() {
//        idlingResource.increment()
        onView(withId(R.id.editTextSignInUsername))
            .perform(typeText("testhm@gmail.com"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.editTextSignInPassword))
            .perform(typeText("1234567"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.buttonConfirmLogin)).perform(click())
        Thread.sleep(1000)
//        onView(withId(R.id.note)).check(matches(withText("Email or Password is not correct")))
        onView(withId(R.id.note)).check(matches(withText(containsString("Email or Password is not correct"))))

    }
//
    @Test
    fun testEmptyPassword() {
//        idlingResource.increment()
        onView(withId(R.id.editTextSignInUsername))
            .perform(typeText("user@gmail.com"))
        onView(withId(R.id.editTextSignInPassword))
            .perform(typeText(""))
        onView(withId(R.id.buttonConfirmLogin)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.note)).check(matches(withText("Password is empty")))
//        idlingResource.decrement()
    }
//
    @Test
    fun testEmptyEmail() {
        onView(withId(R.id.editTextSignInUsername))
            .perform(typeText(""))
        onView(withId(R.id.editTextSignInPassword))
            .perform(typeText("123456789"))
        onView(withId(R.id.buttonConfirmLogin)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.note)).check(matches(withText("Email is empty")))
    }

    @Test
    fun testValidPasswordInvalidUsername() {

        onView(withId(R.id.editTextSignInUsername))
            .perform(typeText("testhm@gmai.com"))
        onView(withId(R.id.editTextSignInPassword))
            .perform(typeText("Abc123456@"))
        onView(withId(R.id.buttonConfirmLogin)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.note)).check(matches(withText("Email or Password is not correct")))

    }

    @Test
    fun testInvalidPasswordInvalidUsername() {

        onView(withId(R.id.editTextSignInUsername))
            .perform(typeText("hm@gmai.com"))
        onView(withId(R.id.editTextSignInPassword))
            .perform(typeText("Abc12345"))
        onView(withId(R.id.buttonConfirmLogin)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.note)).check(matches(withText("Email or Password is not correct")))

    }


    @Test
    fun testEmptyUsernameEmptyPassword() {
        onView(withId(R.id.editTextSignInUsername))
            .perform(typeText(""))
        onView(withId(R.id.editTextSignInPassword))
            .perform(typeText(""))
        onView(withId(R.id.buttonConfirmLogin)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.note)).check(matches(withText("Email is empty")))

    }
}
