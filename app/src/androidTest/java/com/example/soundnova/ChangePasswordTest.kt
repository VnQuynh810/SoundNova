import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.soundnova.ChangePassword
import com.example.soundnova.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ChangePasswordTest {

    @Test
    fun testInvalidCurrentPassword() {
        // Khởi chạy Fragment
        launchFragmentInContainer<ChangePassword>()

        // Nhập dữ liệu không hợp lệ
        onView(withId(R.id.editTextCurrentPass))
            .perform(typeText("wrongPassword"), closeSoftKeyboard())
        onView(withId(R.id.editTextNewPass))
            .perform(typeText("ValidNewPassword123!"), closeSoftKeyboard())
        onView(withId(R.id.editTextConfirmNewPass))
            .perform(typeText("ValidNewPassword123!"), closeSoftKeyboard())

        onView(withId(R.id.buttonSendChange)).perform(click())
        Thread.sleep(1000)
        // đã chạy đang hiện ra user state is not save
        // Kiểm tra thông báo lỗi
        onView(withId(R.id.note)).check(matches(withText("Password is not correct")))
    }



@Test
    fun testEmptyCurrentPassword() {
        launchFragmentInContainer<ChangePassword>()
        onView(withId(R.id.editTextCurrentPass))
            .perform(typeText(""))
        onView(withId(R.id.editTextNewPass))
            .perform(typeText("Valid1NewPassword123!"))
        onView(withId(R.id.editTextConfirmNewPass))
            .perform(typeText("Valid2NewPassword123!"))
        onView(withId(R.id.buttonSendChange)).perform(click())
        onView(withId(R.id.note)).check(matches(withText("Password is not correct")))
    }


    @Test
    fun testInvalidNewPassword() {

        onView(withId(R.id.editTextCurrentPass))
            .perform(typeText("validPassword"))
        onView(withId(R.id.editTextNewPass))
            .perform(typeText("short*"))
        onView(withId(R.id.editTextConfirmNewPass))
            .perform(typeText("short*"))
        onView(withId(R.id.buttonSendChange)).perform(click())
        onView(withId(R.id.note)).check(matches(withText("New password is not correct")))

    }

    @Test
    fun testMismatchedNewPassword() {
        onView(withId(R.id.editTextCurrentPass))
            .perform(typeText("validPassword"))
        onView(withId(R.id.editTextNewPass))
            .perform(typeText("ValidNewPassword123!"))
        onView(withId(R.id.editTextConfirmNewPass))
            .perform(typeText("MismatchedPassword123!"))
        onView(withId(R.id.buttonSendChange)).perform(click())
        onView(withId(R.id.note)).check(matches(withText("New password is not correct")))
    }

    @Test
    fun testSuccessfulPasswordChange() {

        onView(withId(R.id.editTextCurrentPass))
            .perform(typeText("validPassword"))
        onView(withId(R.id.editTextNewPass))
            .perform(typeText("ValidNewPassword123!"))
        onView(withId(R.id.editTextConfirmNewPass))
            .perform(typeText("ValidNewPassword123!"))
        onView(withId(R.id.buttonSendChange)).perform(click())

        // Kiểm tra Intent được gửi đi bằng Espresso Intents
//        Intents.init()
//        intended(hasComponent(HomeActivity::class.java.name))
//        Intents.release()
    }
}


