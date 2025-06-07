package com.example.softwareproject_crewing

import android.os.Bundle
import android.util.Log
import android.view.View
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
    private lateinit var deleteButton: Button
    private lateinit var crewCountText: TextView
    private lateinit var increaseCrewButton: Button
    private lateinit var decreaseCrewButton: Button

    private val db = FirebaseFirestore.getInstance()
    private var storyId: String? = null
    private var authorId: String? = null
    private var crewCount: Int = 0
    private lateinit var commentAdapter: CommentAdapter
    private val commentObjects = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_detail)

        titleText = findViewById(R.id.detailTitle)
        contentText = findViewById(R.id.detailContent)
        commentListView = findViewById(R.id.commentListView)
        commentInput = findViewById(R.id.commentEditText)
        submitCommentButton = findViewById(R.id.submitCommentButton)
        deleteButton = findViewById(R.id.deleteButton)
        crewCountText = findViewById(R.id.crewCountText)
        increaseCrewButton = findViewById(R.id.increaseCrewButton)
        decreaseCrewButton = findViewById(R.id.decreaseCrewButton)

        storyId = intent.getStringExtra("storyId")
        Log.d("StoryDetail", "받은 storyId: $storyId")

        if (storyId == null) {
            Toast.makeText(this, "오류: storyId를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        deleteButton.visibility = View.GONE

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        commentAdapter = CommentAdapter(this, commentObjects, currentUserId) { comment ->
            deleteComment(comment)
        }
        commentListView.adapter = commentAdapter

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

        deleteButton.setOnClickListener {
            deleteStory()
        }

        increaseCrewButton.setOnClickListener { updateCrewCount(1) }
        decreaseCrewButton.setOnClickListener { updateCrewCount(-1) }
    }

    private fun loadStory() {
        db.collection("stories").document(storyId!!).get()
            .addOnSuccessListener { doc ->
                titleText.text = doc.getString("title") ?: "(제목 없음)"
                contentText.text = doc.getString("content") ?: "(내용 없음)"
                authorId = doc.getString("authorId")
                crewCount = (doc.getLong("crewCount") ?: 0).toInt()
                crewCountText.text = crewCount.toString()

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                val isOwner = currentUserId == authorId

                if (isOwner) {
                    deleteButton.visibility = View.VISIBLE
                    increaseCrewButton.visibility = View.VISIBLE
                    decreaseCrewButton.visibility = View.VISIBLE
                } else {
                    increaseCrewButton.visibility = View.GONE
                    decreaseCrewButton.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "글 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateCrewCount(delta: Int) {
        val newCount = (crewCount + delta).coerceIn(0, 10)
        if (newCount != crewCount) {
            db.collection("stories").document(storyId!!)
                .update("crewCount", newCount)
                .addOnSuccessListener {
                    crewCount = newCount
                    crewCountText.text = crewCount.toString()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "조원 수 변경 실패", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadComments() {
        db.collection("stories").document(storyId!!)
            .collection("comments")
            .orderBy("createdAt")
            .get()
            .addOnSuccessListener { result ->
                commentObjects.clear()
                for (doc in result) {
                    val comment = Comment(
                        id = doc.id,
                        content = doc.getString("content") ?: "",
                        nickname = doc.getString("nickname") ?: "(익명)",
                        major = doc.getString("major") ?: "(전공없음)",
                        authorId = doc.getString("authorId") ?: ""
                    )
                    commentObjects.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "댓글 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteComment(comment: Comment) {
        db.collection("stories").document(storyId!!)
            .collection("comments").document(comment.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                loadComments()
            }
            .addOnFailureListener {
                Toast.makeText(this, "댓글 삭제 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteStory() {
        db.collection("stories").document(storyId!!)
            .collection("comments").get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                for (comment in snapshot) {
                    batch.delete(comment.reference)
                }
                batch.commit().addOnSuccessListener {
                    db.collection("stories").document(storyId!!).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "글 삭제 실패", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }
}


data class Comment(
    val id: String,
    val content: String,
    val nickname: String,
    val major: String,
    val authorId: String
)

