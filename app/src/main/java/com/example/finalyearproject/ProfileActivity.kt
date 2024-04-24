package com.example.finalyearproject
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        updateUserData()
        calculateAndDisplayRank()

        val logOutButton: Button = findViewById(R.id.logOutBtn)
        logOutButton.setOnClickListener {
            performLogout()
        }

        val updateFearedSoundsText: TextView = findViewById(R.id.updateFearedSounds)
        updateFearedSoundsText.setOnClickListener {
            updateFearedSounds()
        }
        val homeButton: TextView = findViewById(R.id.homeBtn)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun updateUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").reference
            database.child("users").child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").getValue(String::class.java) ?: "User"
                    val xp = snapshot.child("xp").getValue(Int::class.java) ?: 0
                    val dob = snapshot.child("dob").getValue(String::class.java) ?: "Not Set"
                    val fearedSounds = snapshot.child("feared_sounds").children.map { it.getValue(String::class.java) }.joinToString(", ")
                    val streak = snapshot.child("consecutiveLogins").getValue(Int::class.java) ?: 0

                    findViewById<TextView>(R.id.userName).text = userName
                    findViewById<TextView>(R.id.userXp).text = xp.toString()
                    findViewById<TextView>(R.id.Dob).text = dob
                    findViewById<TextView>(R.id.fearedSounds).text = fearedSounds
                    findViewById<TextView>(R.id.consecDays).text = streak.toString()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle possible errors
                }
            })
        }
    }

    private fun calculateAndDisplayRank() {
        lifecycleScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val database = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").reference.child("users")
            val job = launch {
                val (xpList, currentUserXP) = fetchUserData(database, userId)
                val rank = calculatePercentile(xpList, currentUserXP)
                updateUI(rank)
            }
            job.join()
        }
    }

    private suspend fun fetchUserData(database: DatabaseReference, userId: String?): Pair<List<Int>, Int> {
        val deferred = kotlinx.coroutines.CompletableDeferred<Pair<List<Int>, Int>>()
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val xpList = mutableListOf<Int>()
                var currentUserXP = 0
                snapshot.children.forEach {
                    val xp = it.child("xp").getValue(Int::class.java) ?: 0
                    xpList.add(xp)
                    if (it.key == userId) currentUserXP = xp
                }
                deferred.complete(xpList to currentUserXP)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                deferred.completeExceptionally(Exception(databaseError.message))
            }
        })
        return deferred.await()
    }

    //Needs adjusting
    private fun calculatePercentile(xpList: List<Int>, currentUserXP: Int): Double {
        val sortedList = xpList.sortedDescending()
        val rankPosition = sortedList.indexOf(currentUserXP) + 1
        val percentile = (rankPosition.toDouble() / sortedList.size.toDouble()) * 100.0

        return if (percentile <= 0.01) 0.01 else percentile
    }

    private fun updateUI(percentile: Double) {
        val rankTextView: TextView = findViewById(R.id.userRank)
        val formattedPercentile = String.format("%.2f", 100.0 - percentile)
        rankTextView.text = "Top $formattedPercentile%"
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateFearedSounds() {
        val intent = Intent(this, SurveyActivity::class.java)
        startActivity(intent)
    }
}
