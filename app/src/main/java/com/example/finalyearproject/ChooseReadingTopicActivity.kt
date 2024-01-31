package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ChooseReadingTopicActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CATEGORY_NAME = "com.example.finalyearproject.CATEGORY_NAME"
    }

    private lateinit var topicsRecyclerView: RecyclerView
    private var topics = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_reading_topic)

        topicsRecyclerView = findViewById(R.id.topicsRecyclerView)
        topicsRecyclerView.layoutManager = LinearLayoutManager(this)

        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            val intent = Intent(this@ChooseReadingTopicActivity, ExercisesActivity::class.java)
            startActivity(intent)
        }

        fetchUserAgeCategoryAndUpdateTopics()
    }

    private fun fetchUserAgeCategoryAndUpdateTopics() {
        val userId = getCurrentUserId()

        if (userId != null) {
            UserAgeUtil.getUserAgeCategory(userId) { category ->
                // Update topics list based on age category
                topics = if (category == "Kids Topics") {
                    listOf("Animals", "Space", "Fairy Tales", "Nature") // Example kids topics
                } else {
                    listOf("Science", "Food", "History", "sport", "entertainment", "business", "culture")
                }
                setupRecyclerView()
            }
        } else {

        }
    }
    private fun setupRecyclerView() {
        topicsRecyclerView.adapter = ReadingTopicsAdapter(topics) { selectedTopic ->
            Toast.makeText(this, "Selected: $selectedTopic", Toast.LENGTH_SHORT).show()
            onCategorySelected(selectedTopic)
        }

        val dividerItemDecoration = DividerItemDecoration(topicsRecyclerView.context,
            (topicsRecyclerView.layoutManager as LinearLayoutManager).orientation)
        topicsRecyclerView.addItemDecoration(dividerItemDecoration)
    }

    fun onCategorySelected(categoryName: String) {
        val intent = Intent(this, TextPassageActivity::class.java).apply {
            putExtra(EXTRA_CATEGORY_NAME, categoryName)
        }
        startActivity(intent)
    }

    private fun getCurrentUserId(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid // Returns the user ID of the currently signed-in user or null if no user is signed in.
    }

}