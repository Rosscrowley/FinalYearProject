package com.example.finalyearproject
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ChooseTongueTwisterActivity : AppCompatActivity() {

    private lateinit var adapter: TongueTwisterAdapter

    companion object {
        const val TONGUE_TWISTER_CONTENT = "com.example.finalyearproject.CATEGORY_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_tongue_twister)

        val recyclerView: RecyclerView = findViewById(R.id.tongueTwisterRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Optionally, add dividers between RecyclerView items
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            (recyclerView.layoutManager as LinearLayoutManager).orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            val intent = Intent(this@ChooseTongueTwisterActivity, ExercisesActivity::class.java)
            startActivity(intent)
        }

        // Fetch the user's feared sounds
        fetchUserFearedSounds()
    }

    private fun fetchUserFearedSounds() {
        // Initialize Firebase Authentication
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Check if the user is logged in
        if (currentUser != null) {
            // User is logged in, fetch their feared sounds from the database
            val userId = currentUser.uid
            val userRef = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Get the feared sounds from the user's data
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        val fearedSounds = user.feared_sounds

                        // Log the fetched feared sounds for debugging
                        Log.d("FearedSounds", "Feared Sounds: $fearedSounds")

                        // Fetch tongue twisters based on the user's feared sounds
                        fetchTongueTwisters(fearedSounds)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun fetchTongueTwisters(fearedSounds: List<String>) {
        // Initialize your RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.tongueTwisterRecyclerView)

        // Initialize Firebase Database reference
        val tongueTwistersRef = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").getReference("tongue_twisters")

        // Create an empty list to store tongue twisters
        val tongueTwistersList = mutableListOf<TongueTwister>()

        // Set up the Firebase ValueEventListener to fetch data
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the existing list
                tongueTwistersList.clear()

                // Iterate through the DataSnapshot to fetch tongue twisters
                for (data in snapshot.children) {
                    val tongueTwister = data.getValue(TongueTwister::class.java)
                    if (tongueTwister != null) {
                        // Log the content of each fetched tongue twister
                        Log.d("TongueTwisterContent", "Content: ${tongueTwister.content}")

                        tongueTwistersList.add(tongueTwister)
                    }
                }

                // Sort tongue twisters by the count of feared sounds
                tongueTwistersList.sortByDescending { twister ->
                    fearedSounds.sumBy { sound ->
                        twister.content.toLowerCase().count { it == sound.toLowerCase()[0] }
                    }
                }

                // Create and set the adapter with the fetched data
                adapter = TongueTwisterAdapter(tongueTwistersList) { tongueTwister ->
                    // Handle click, start SelectedTongueTwisterActivity with the selected item
                    val intent = Intent(this@ChooseTongueTwisterActivity, SelectedTongueTwisterActivity::class.java)
                    intent.putExtra(TONGUE_TWISTER_CONTENT, tongueTwister.content)
                    Log.d("SelectedTongueTwisterActivity", "Content passed: ${tongueTwister.content}")
                    startActivity(intent)
                }

                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }

        // Add the ValueEventListener to the DatabaseReference
        tongueTwistersRef.addValueEventListener(valueEventListener)
    }
}