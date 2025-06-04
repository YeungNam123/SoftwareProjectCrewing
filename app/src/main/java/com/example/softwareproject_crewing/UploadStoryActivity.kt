package com.example.softwareproject_crewing

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.ZoneId

class UploadStoryActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var tagEditText: EditText
    private lateinit var uploadButton: Button
    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_story)

        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        tagEditText = findViewById(R.id.tagEditText)
        uploadButton = findViewById(R.id.uploadButton)

        uploadButton.setOnClickListener {
            uploadStory()
        }


    }

    private fun uploadStory() {
        val title = titleEditText.text.toString().trim()
        val content = contentEditText.text.toString().trim()
        val tags = tagEditText.text.toString().split(",").map { it.trim() }

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val story = hashMapOf(
            "title" to title,
            "content" to content,
            "createdAt" to Timestamp.now(),
            "authorId" to user?.uid,
            "tags" to tags
        )

        db.collection("stories")
            .add(story)
            .addOnSuccessListener {
                Toast.makeText(this, "업로드 성공!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "업로드 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
