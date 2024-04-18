package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class SurveyActivity : AppCompatActivity() {

    private lateinit var doneButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var soundsRecyclerView: RecyclerView
    private lateinit var soundsAdapter: SoundsAdapter
    private var soundsList = mutableListOf(
        SoundItem("HH", "House, Hot"),
        SoundItem("NG", "SiNG, loNG"),
        SoundItem("AA", "fAther, smArt"),
        SoundItem("AY", "mY, whY"),
        SoundItem("OY", "bOY, tOY"),
        SoundItem("CH", "CHair, CHina"),
        SoundItem("JH", "Just, Gym"),
        SoundItem("EY", "sAY, EIght"),
        SoundItem("AE", "At, jAcket"),
        SoundItem("UW", "yOU, fOOd"),
        SoundItem("S", "Say, boSS"),
        SoundItem("B", "Buy, Big"),
        SoundItem("Z", "Zoo, buZZ")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)

        soundsRecyclerView = findViewById(R.id.soundsRecyclerView)
        soundsRecyclerView.layoutManager = LinearLayoutManager(this)
        soundsAdapter = SoundsAdapter(soundsList)
        soundsRecyclerView.adapter = soundsAdapter

        firebaseAuth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").reference

        fetchExistingSounds()

        doneButton = findViewById(R.id.button_done)
        doneButton.setOnClickListener {
            val selectedSounds = soundsList.filter { it.isSelected }.map { it.sound }
            saveSelectedSoundsToDatabase(selectedSounds)
        }
    }

    private fun fetchExistingSounds() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            database.child("users").child(userId).child("feared_sounds")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val existingSounds = snapshot.getValue<List<String>>() ?: emptyList()
                        updateSoundsList(existingSounds)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(
                            this@SurveyActivity,
                            "Failed to fetch data: ${databaseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun updateSoundsList(existingSounds: List<String>) {
        soundsList.forEach { item ->
            item.isSelected = existingSounds.contains(item.sound)
        }
        soundsAdapter.notifyDataSetChanged()
    }

    private fun saveSelectedSoundsToDatabase(selectedSounds: List<String>) {
        val userId = firebaseAuth.currentUser?.uid
        userId?.let { uid ->
            database.child("users").child(uid).child("firstLogin").setValue(false)
                .addOnSuccessListener {
                    database.child("users").child(uid).child("feared_sounds")
                        .setValue(selectedSounds)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error saving sounds: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
        }
    }
}

