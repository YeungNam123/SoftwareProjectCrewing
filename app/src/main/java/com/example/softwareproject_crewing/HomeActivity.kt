package com.example.softwareproject_crewing

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var storyListView: ListView
    private lateinit var searchEditText: EditText
    private val db = FirebaseFirestore.getInstance()
    private val stories = mutableListOf<String>()
    private val storyDocIds = mutableListOf<String>()
    private val fullStoryList = mutableListOf<Pair<String, String>>() // Pair<표시내용, 문서ID>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val writeStoryButton = findViewById<Button>(R.id.writeStoryButton)
        val refreshButton = findViewById<Button>(R.id.refreshButton)
        storyListView = findViewById(R.id.storyListView)
        searchEditText = findViewById(R.id.searchEditText)

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

        refreshButton.setOnClickListener {
            loadStories()
            Toast.makeText(this, "새로고침 완료", Toast.LENGTH_SHORT).show()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterStories(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        storyListView.setOnItemClickListener { _, _, position, _ ->
            if (position < storyDocIds.size) {
                val docId = storyDocIds[position]
                val intent = Intent(this, StoryDetailActivity::class.java)
                intent.putExtra("storyId", docId)
                startActivity(intent)
            }
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stories)
        storyListView.adapter = adapter

        loadStories()
    }

    private fun loadStories() {
        db.collection("stories")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                fullStoryList.clear()

                for (doc in result) {
                    val title = doc.getString("title") ?: "(제목 없음)"
                    val tags = (doc.get("tags") as? List<*>)?.joinToString(", ") ?: ""
                    val authorId = doc.getString("authorId") ?: "(알 수 없음)"
                    val timestamp = doc.getTimestamp("createdAt")
                    val timeStr = timestamp?.toDate()?.let {
                        SimpleDateFormat("MM월 dd일 (E) HH:mm", Locale.KOREA).format(it)
                    } ?: ""

                    db.collection("users").document(authorId).get()
                        .addOnSuccessListener { userDoc ->
                            val nickname = userDoc.getString("nickname") ?: "(익명)"
                            val major = userDoc.getString("major") ?: ""
                            val display = "$title\nby $nickname ($major) · $timeStr"
                            val searchable = "$title $tags $major $nickname"

                            fullStoryList.add(Pair(display, doc.id))
                            filterStories(searchEditText.text.toString())
                        }
                        .addOnFailureListener {
                            val display = "$title\nby $authorId · $timeStr"
                            fullStoryList.add(Pair(display, doc.id))
                            filterStories(searchEditText.text.toString())
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "스토리 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterStories(query: String) {
        val lower = query.lowercase(Locale.getDefault())
        val filtered = if (lower.isBlank()) {
            fullStoryList
        } else {
            fullStoryList.filter { it.first.lowercase(Locale.getDefault()).contains(lower) }
        }

        stories.clear()
        storyDocIds.clear()
        for (pair in filtered) {
            stories.add(pair.first)
            storyDocIds.add(pair.second)
        }
        adapter.notifyDataSetChanged()
    }
}

