package com.example.softwareproject_crewing

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

class CommentAdapter(
    private val context: Context,
    private val commentList: List<Comment>,
    private val currentUserId: String,
    private val onDeleteClick: (Comment) -> Unit
) : BaseAdapter() {

    override fun getCount() = commentList.size
    override fun getItem(position: Int) = commentList[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_comment, parent, false)

        val comment = commentList[position]
        val text = view.findViewById<TextView>(R.id.commentText)
        val deleteButton = view.findViewById<Button>(R.id.commentDeleteButton)

        text.text = "${comment.content}\nby ${comment.nickname} (${comment.major})"

        if (comment.authorId == currentUserId) {
            deleteButton.visibility = View.VISIBLE
            deleteButton.setOnClickListener { onDeleteClick(comment) }
        } else {
            deleteButton.visibility = View.GONE
        }

        return view
    }
}
