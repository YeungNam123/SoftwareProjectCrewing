package com.example.softwareproject_crewing

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StoryDetailActivity : AppCompatActivity() {

    private lateinit var titleText: TextView
    private lateinit var contentText: TextView
    private lateinit var commentListView: ListView
    private lateinit var commentInput: EditText
    private lateinit var submitCommentButton: Button
    private val db = FirebaseFirestore.getInstance()
    private val comments = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private var storyId: String? = null  // nullable로 변경

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_detail)

        titleText = findViewById(R.id.detailTitle)
        contentText = findViewById(R.id.detailContent)
        commentListView = findViewById(R.id.commentListView)
        commentInput = findViewById(R.id.commentEditText)
        submitCommentButton = findViewById(R.id.submitCommentButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, comments)
        commentListView.adapter = adapter

        storyId = intent.getStringExtra("storyId")
        Log.d("StoryDetail", "받은 storyId: $storyId")

        if (storyId == null) {
            Toast.makeText(this, "오류: storyId를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadStory()
        loadComments()

        submitCommentButton.setOnClickListener {
            val content = commentInput.text.toString().trim()
            val user = FirebaseAuth.getInstance().currentUser

            if (content.isNotEmpty() && user != null) {
                db.collection("users").document(user.uid).get()
                    .addOnSuccessListener { userDoc ->
                        val nickname = userDoc.getString("nickname") ?: "(익명)"
                        val major = userDoc.getString("major") ?: "(전공없음)"

                        val comment = hashMapOf(
                            "content" to content,
                            "createdAt" to Timestamp.now(),
                            "authorId" to user.uid,
                            "nickname" to nickname,
                            "major" to major
                        )

                        db.collection("stories").document(storyId!!)
                            .collection("comments")
                            .add(comment)
                            .addOnSuccessListener {
                                commentInput.text.clear()
                                loadComments()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "댓글 업로드 실패", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "사용자 정보 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun loadStory() {
        db.collection("stories").document(storyId!!).get()
            .addOnSuccessListener { doc ->
                titleText.text = doc.getString("title") ?: "(제목 없음)"
                contentText.text = doc.getString("content") ?: "(내용 없음)"
            }
            .addOnFailureListener {
                Toast.makeText(this, "글 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadComments() {
        db.collection("stories").document(storyId!!)
            .collection("comments")
            .orderBy("createdAt")
            .get()
            .addOnSuccessListener { result ->
                comments.clear()
                for (doc in result) {
                    val text = doc.getString("content") ?: ""
                    val nickname = doc.getString("nickname") ?: "(익명)"
                    val major = doc.getString("major") ?: "(전공없음)"
                    comments.add("$text\nby $nickname ($major)")
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "댓글 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }
}
