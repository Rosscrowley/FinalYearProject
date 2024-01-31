package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SurveyActivity : AppCompatActivity() {

    private lateinit var doneButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var soundsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)

        soundsRecyclerView = findViewById(R.id.soundsRecyclerView)
        soundsRecyclerView.layoutManager = LinearLayoutManager(this)

        val soundsList = listOf(
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
            SoundItem("Z", "Zoo, buZZ"),


        )

        // Set the adapter for the RecyclerView
        soundsRecyclerView.adapter = SoundsAdapter(soundsList)

        // Initialize Firebase Auth and Database Reference
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").reference

        doneButton = findViewById(R.id.button_done)
        doneButton.setOnClickListener{

            val selectedSounds = soundsList.filter { it.isSelected }.map { it.sound }


            saveSelectedSoundsToDatabase(selectedSounds)
        }
    }

    private fun saveSelectedSoundsToDatabase(selectedSounds: List<String>) {
        val userId = firebaseAuth.currentUser?.uid
        userId?.let { uid ->
            // Assuming you want to store the selected sounds under the "feared_sounds" node
            database.child("users").child(uid).child("firstLogin").setValue(false).addOnSuccessListener {
                database.child("users").child(uid).child("feared_sounds").setValue(selectedSounds)
                    .addOnSuccessListener {
                        // Handle success
                        Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Finish this activity so the user can't go back
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                        Toast.makeText(this, "Error saving sounds: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

        }

    }
}

