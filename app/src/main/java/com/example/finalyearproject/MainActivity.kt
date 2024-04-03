package com.example.finalyearproject

import DailyExerciseAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var pronunCard: CardView
    private lateinit var exerciseCard: CardView
    private val adapter = DailyExerciseAdapter(listOf(), this)
    private lateinit var recyclerView: RecyclerView
    private lateinit var recommendationService: ExerciseRecommendationService


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.dailyExercisesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            (recyclerView.layoutManager as LinearLayoutManager).orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)


        val infoIcon: ImageView = findViewById(R.id.infoIcon)
        infoIcon.setOnClickListener {
            showExercisesInfoDialog()
        }


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        pronunCard = findViewById(R.id.pronunTest)
        pronunCard.setOnClickListener{
            val intent = Intent(this@MainActivity, SpeechActivity::class.java)
            startActivity(intent)
        }

        exerciseCard = findViewById(R.id.exercisesCard)
        exerciseCard.setOnClickListener{
            val intent = Intent(this@MainActivity, ExercisesActivity::class.java)
            startActivity(intent)
        }
        updateWelcomeMessage()

        bottomNavigationView = findViewById(R.id.nav_view)
        bottomNavigationView.selectedItemId = R.id.menu_item_home
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_progress -> {
                    // Navigate to Progress
                    val intent = Intent(this@MainActivity, ProgressActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_exercise -> {
                    // Navigate to Exercise
                    val intent = Intent(this@MainActivity, ExercisesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_wave_analysis -> {
                    // Navigate to Exercise
                    val intent = Intent(this@MainActivity, WaveAnalysisPageActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        val exerciseRepository = ExerciseRepository()
        recommendationService = ExerciseRecommendationService(exerciseRepository)

        // Example usage
        fetchRecommendations()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                performLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()

       val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun updateWelcomeMessage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").reference
            database.child("users").child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").getValue(String::class.java) ?: "User"
                    val loginStreak = snapshot.child("consecutiveLogins").getValue(Int::class.java) ?: 0

                    val welcomeTextView: TextView = findViewById(R.id.welcomeName)
                    welcomeTextView.text = "Welcome back, $userName!"

                    val streakTextView: TextView = findViewById(R.id.streakText)
                    streakTextView.text = if (loginStreak == 1) "You started a new streak! come back everyday to build up your streak!" else "$loginStreak day streak! Keep up the good work!"
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun showExercisesInfoDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("How Daily Exercises Work")
        builder.setMessage(".")
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchRecommendations() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val recommendations = recommendationService.fetchAndRecommendExercises(userId)
                withContext(Dispatchers.Main) {
                    val recyclerView: RecyclerView = findViewById(R.id.dailyExercisesRecyclerView)
                    (recyclerView.adapter as? DailyExerciseAdapter)?.updateExercises(recommendations)

                }
            }
        }
    }
}







