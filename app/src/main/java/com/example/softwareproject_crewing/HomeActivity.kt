package com.example.softwareproject_crewing

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var storyListView: ListView
    private val db = FirebaseFirestore.getInstance()
    private val stories = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val writeStoryButton = findViewById<Button>(R.id.writeStoryButton)
        storyListView = findViewById(R.id.storyListView)

        val email = FirebaseAuth.getInstance().currentUser?.email
        welcomeText.text = "환영합니다, $email 님!"

        logoutButton.setOnClickListener {
            LoginService.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        writeStoryButton.setOnClickListener {
            startActivity(Intent(this, UploadStoryActivity::class.java))
        }
        val refreshButton = findViewById<Button>(R.id.refreshButton)

        refreshButton.setOnClickListener {
            loadStories()
            Toast.makeText(this, "새로고침 완료", Toast.LENGTH_SHORT).show()
        }


        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stories)
        storyListView.adapter = adapter

        loadStories()
    }

    private fun loadStories() {
        db.collection("stories")
            .orderBy("createdAt")
            .get()
            .addOnSuccessListener { result ->
                stories.clear()
                for (doc in result) {
                    val title = doc.getString("title") ?: "(제목 없음)"
                    val authorId = doc.getString("authorId") ?: "(알 수 없음)"
                    val timestamp = doc.getTimestamp("createdAt")
                    val timeStr = timestamp?.toDate()?.let {
                        SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(it)
                    } ?: ""

                    stories.add("$title\nby $authorId  ·  $timeStr")
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "스토리 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }
}
