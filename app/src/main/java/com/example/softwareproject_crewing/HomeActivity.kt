package com.example.softwareproject_crewing

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        val email = FirebaseAuth.getInstance().currentUser?.email
        welcomeText.text = "환영합니다, $email 님!"

        logoutButton.setOnClickListener {
            LoginService.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}