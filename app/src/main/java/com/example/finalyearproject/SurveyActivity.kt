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
        SoundItem("S", "Say, boSS"),
        SoundItem("B", "Buy, Big"),
        SoundItem("P", "Park, looP"),
        SoundItem("T", "Town, leTTer"),
        SoundItem("L", "Long, taLL"),
        SoundItem("F", "Father, Fun"),
        SoundItem("D", "Day, maD"),
        SoundItem("C", "Cake, laCk"),
        SoundItem("M", "Man, pluM"),
        SoundItem("J", "Just, Joke"),
        SoundItem("Z", "Zoo, buZZ"),
        SoundItem("A", "At, jAcket"),
        SoundItem("E", "yOU, fOOd"),
        SoundItem("I", "In, Ice"),
        SoundItem("O", "Odd, Only"),
        SoundItem("U", "Under, Unique"),
        SoundItem("G", "Great, joG"),
        SoundItem("H", "Hungry, Hood"),
        SoundItem("K", "Kent, Kind"),
        SoundItem("N", "Nope, mooN"),
        SoundItem("R", "Run, aRk"),
        SoundItem("V", "Vas, caVe"),
        SoundItem("W", "Wind, knoW"),
        SoundItem("Q", "Queen, Quiet"),
        SoundItem("Y", "Young, Yummy"),
        SoundItem("X", "oX, X-ray"),
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

