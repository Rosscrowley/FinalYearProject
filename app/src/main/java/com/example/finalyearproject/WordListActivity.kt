package com.example.finalyearproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WordListActivity  : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_list)

        val wordsRecyclerView = findViewById<RecyclerView>(R.id.wordsRecyclerView)
        wordsRecyclerView.layoutManager = LinearLayoutManager(this)
        wordsRecyclerView.adapter = WordListAdapter(listOf("sunset", "apple", "sky", "grass","house", "car", "tree", "book", "phone", "water", "food", "friend", "family", "light", "door", "window", "chair", "table", "night", "day", "sun", "moon", "star", "cloud", "rain", "snow", "wind", "street", "road", "garden", "park", "school", "work", "play", "music", "movie", "picture", "game", "animal", "cat", "dog", "bird", "fish", "flower", "leaf", "grass", "tree", "fruit", "vegetable", "meat", "bread", "milk", "coffee", "tea","university", "celebration", "information", "electricity", "imagination", "communication", "revolutionary", "environmental", "personality", "international", "photography", "relationship", "understanding", "development", "entertainment", "technology", "independence", "advertisement", "controversial", "extraordinary", "mathematics", "architecture", "experimental", "philosophy", "psychology", "democracy", "temperature", "geography", "biography", "economy", "literature", "sustainability", "conversation", "generation", "alternative", "significant", "vegetarian", "government", "celebrity", "manipulation", "hospitality", "unbelievable", "responsibility", "administration", "constitution", "enthusiastic", "incredible", "educational", "industrial", "organization"))
    }
}