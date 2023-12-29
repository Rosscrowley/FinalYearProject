package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChooseReadingTopicActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CATEGORY_NAME = "com.example.finalyearproject.CATEGORY_NAME"
    }

    private lateinit var topicsRecyclerView: RecyclerView
    private val topics = listOf("Science", "Food", "history", "Economics", "Entrepreneurship", "Psychology", "Self Help", "Spirituality", "Technology")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_reading_topic)

        topicsRecyclerView = findViewById(R.id.topicsRecyclerView)
        topicsRecyclerView.layoutManager = LinearLayoutManager(this)
        topicsRecyclerView.adapter = ReadingTopicsAdapter(topics) { selectedTopic ->
            // Handle topic selection
            Toast.makeText(this, "Selected: $selectedTopic", Toast.LENGTH_SHORT).show()
            onCategorySelected(selectedTopic)
        }

        // Optionally, add dividers between RecyclerView items
        val dividerItemDecoration = DividerItemDecoration(topicsRecyclerView.context,
            (topicsRecyclerView.layoutManager as LinearLayoutManager).orientation)
        topicsRecyclerView.addItemDecoration(dividerItemDecoration)

        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {

            val intent = Intent(this@ChooseReadingTopicActivity, ExercisesActivity::class.java)
            startActivity(intent)
        }
    }

    fun onCategorySelected(categoryName: String) {
        val intent = Intent(this, TextPassageActivity::class.java).apply {
            putExtra(EXTRA_CATEGORY_NAME, categoryName)
        }
        startActivity(intent)
    }


}