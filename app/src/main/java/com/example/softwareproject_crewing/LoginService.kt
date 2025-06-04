package com.example.softwareproject_crewing

object LoginService {
    fun login(id: String, pw: String, callback: (Boolean) -> Unit) {
        FirebaseService.loginUser(id, pw) { userId ->
            if (userId != null) {
                val sessionCreated = SessionManager.createSession(userId)
                callback(sessionCreated)
            } else {
                callback(false)
            }
        }
    }

    fun logout() {
        val userId = FirebaseService.getCurrentUserId()
        if (userId != null) {
            SessionManager.destroySession(userId)
        }
        FirebaseService.logoutUser()
    }
}
