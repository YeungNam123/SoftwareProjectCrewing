package com.example.softwareproject_crewing

object SessionManager {
    private val activeSessions = mutableSetOf<String>()

    fun createSession(userId: String): Boolean {
        activeSessions.add(userId)
        return true
    }

    fun destroySession(userId: String) {
        activeSessions.remove(userId)
    }

    fun isLoggedIn(userId: String): Boolean {
        return activeSessions.contains(userId)
    }
}