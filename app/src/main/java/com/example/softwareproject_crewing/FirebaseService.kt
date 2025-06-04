package com.example.softwareproject_crewing

import com.google.firebase.auth.FirebaseAuth

object FirebaseService {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun loginUser(id: String, pw: String, callback: (String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(id, pw)
            .addOnSuccessListener {
                val uid = firebaseAuth.currentUser?.uid
                callback(uid)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun logoutUser() {
        firebaseAuth.signOut()
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}