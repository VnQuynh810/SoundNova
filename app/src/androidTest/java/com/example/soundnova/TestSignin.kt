////package com.example.soundnova
////
////import com.google.firebase.auth.AuthResult
////import com.google.firebase.auth.FirebaseAuth
////import com.google.firebase.auth.FirebaseUser
////import com.google.android.gms.tasks.Tasks
////import org.junit.Test
////import org.junit.Assert.*
////import org.mockito.Mockito.*
////
////data class LoginResult(val success: Boolean, val message: String)
////
////class TestSignin {
////
////    private val mockAuth = mock(FirebaseAuth::class.java) // Mock FirebaseAuth
////    private val mockAuthResult = mock(AuthResult::class.java) // Mock AuthResult
////    private val mockUser = mock(FirebaseUser::class.java) // Mock FirebaseUser
////
////    // Hàm loginUser
////    fun loginUser(email: String, password: String): LoginResult {
////        val task = mockAuth.signInWithEmailAndPassword(email, password)
////        return if (task.isSuccessful) {
////            val user = task.result?.user
////            if (user != null) {
////                LoginResult(success = true, message = "Login successful")
////            } else {
////                LoginResult(success = false, message = "User is null")
////            }
////        } else {
////            LoginResult(success = false, message = "Login failed")
////        }
////    }
////
////    @Test
////    fun testLoginSuccesscn() {
////        // Giả lập rằng AuthResult trả về một người dùng hợp lệ khi đăng nhập
////        `when`(mockAuthResult.user).thenReturn(mockUser) // AuthResult trả về mockUser
////        `when`(mockAuth.signInWithEmailAndPassword("testhm@gmail.com", "Abc123456@"))
////            .thenReturn(Tasks.forResult(mockAuthResult)) // Trả về mockAuthResult
////
////        // Thực hiện đăng nhập (giả lập)
////        val result = loginUser("testhm@gmail.com", "Abc123456@")
////
////        // Kiểm tra kết quả
////        assertTrue(result.success)
////        assertEquals("Login successful", result.message)
////    }
////}
//
//package com.example.soundnova
//
//import android.widget.TextView
//import com.google.firebase.auth.AuthResult
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.android.gms.tasks.Tasks
//import org.junit.Test
//import org.junit.Assert.*
//import org.mockito.Mockito.*
//
//data class LoginResult(val success: Boolean, val message: String)
//
//class TestSignin {
//
//    private val mockAuth = mock(FirebaseAuth::class.java) // Mock FirebaseAuth
//    private val mockAuthResult = mock(AuthResult::class.java) // Mock AuthResult
//    private val mockUser = mock(FirebaseUser::class.java) // Mock FirebaseUser
//    private val note = mock(TextView::class.java) // Mock TextView (để kiểm tra thông báo)
//
//    // Hàm loginUser
//    fun loginUser(email: String, password: String): LoginResult {
//        val task = mockAuth.signInWithEmailAndPassword(email, password)
//        return if (task.isSuccessful) {
//            val user = task.result?.user
//            if (user != null) {
//                LoginResult(success = true, message = "Login successful")
//            } else {
//                LoginResult(success = false, message = "User is null")
//            }
//        } else {
//            LoginResult(success = false, message = "Login failed")
//        }
//    }
//
//    @Test
//    fun testLoginSuccess() {
//        // Giả lập rằng AuthResult trả về một người dùng hợp lệ khi đăng nhập
//        `when`(mockAuthResult.user).thenReturn(mockUser) // AuthResult trả về mockUser
//        `when`(mockAuth.signInWithEmailAndPassword("testhm@gmail.com", "Abc123456@"))
//            .thenReturn(Tasks.forResult(mockAuthResult)) // Trả về mockAuthResult
//
//        // Kiểm tra TextView 'note' khi login thành công
//        val result = loginUser("testhm@gmail.com", "Abc123456@")
//        assertTrue(result.success)
//
//        // Kiểm tra xem TextView có được cập nhật với thông báo thành công hay không
//        assertEquals("Email or Password is not correct", note.text)
//    }
//
//    @Test
//    fun testLoginFailure() {
//        // Giả lập trường hợp đăng nhập không thành công
//        `when`(mockAuth.signInWithEmailAndPassword("testhm@gmail.com", "wrongpassword"))
//            .thenReturn(Tasks.forException(Exception("Login failed")))
//
//        // Kiểm tra TextView 'note' khi login thất bại
//        val result = loginUser("testhm@gmail.com", "wrongpassword")
//        assertFalse(result.success)
//
//        // Kiểm tra xem TextView có được cập nhật với thông báo lỗi đúng hay không
//        assertEquals("Email or Password is not correct", note.text)
//    }
//}
