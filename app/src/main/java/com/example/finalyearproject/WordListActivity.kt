package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WordListActivity  : AppCompatActivity() {

    private lateinit var closeButton: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_list)

        closeButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            startActivity(Intent(this@WordListActivity, WaveAnalysisPageActivity::class.java))
        }

        val wordsRecyclerView = findViewById<RecyclerView>(R.id.wordsRecyclerView)
        wordsRecyclerView.layoutManager = LinearLayoutManager(this)
        wordsRecyclerView.adapter = WordListAdapter(listOf("university", "celebration", "information", "electricity", "imagination", "communication", "revolutionary", "environmental", "personality", "international", "photography", "relationship", "understanding", "development", "entertainment", "technology", "independence", "advertisement", "controversial", "extraordinary", "mathematics", "architecture", "experimental", "philosophy", "psychology", "democracy", "temperature", "geography", "biography", "economy", "literature", "sustainability", "conversation", "generation", "alternative", "significant", "vegetarian", "government", "celebrity", "manipulation", "hospitality", "unbelievable", "responsibility", "administration", "constitution", "enthusiastic", "incredible", "educational", "industrial", "organization"))
    }
}