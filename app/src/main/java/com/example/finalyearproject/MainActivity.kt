package com.example.finalyearproject

import DailyExerciseAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), DailyExerciseAdapter.ExerciseClickListener  {

    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var pronunCard: CardView
    private lateinit var exerciseCard: CardView
    private lateinit var adapter: DailyExerciseAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var recommendationService: ExerciseRecommendationService
    private val exerciseRepository = ExerciseRepository()
    private var dailyExercises: List<ExerciseActivity> = listOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        adapter = DailyExerciseAdapter(listOf(), this, this)
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


        recommendationService = ExerciseRecommendationService(exerciseRepository)


//        Example usage
//        fetchRecommendations()
        initializeOrFetchExercises()
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



    override fun onExerciseStart(exerciseName: String, activityClassName: String) {
        try {
            val clazz = Class.forName(activityClassName)
            val intent = Intent(this, clazz)
            Log.d("MainActivity", "Starting $exerciseName for result.")
            startForResult.launch(intent)
        } catch (e: ClassNotFoundException) {
            Log.e("MainActivity", "Activity class not found: $activityClassName", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeOrFetchExercises() {
        if (isNewDay() || fetchSavedDailyExercises().isNullOrEmpty()) {
            // It's a new day or no exercises are saved, fetch and save new exercises
            fetchAndSaveDailyExercises()
        } else {
            // Use saved exercises
            dailyExercises = fetchSavedDailyExercises()!!
            updateRecyclerView(dailyExercises)
        }
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun checkForNewDayOrFetchSavedExercises() {
//        Log.d("MainActivity", "Checking for new day or fetching saved exercises.")
//        val savedExercises = fetchSavedDailyExercises()
//        if (savedExercises == null || isNewDay()) {
//            Log.d("MainActivity", "New day or no saved exercises found, fetching new exercises.")
//            fetchAndSaveDailyExercises()
//        } else {
//            Log.d("MainActivity", "Using saved exercises.")
//            dailyExercises = savedExercises
//            updateRecyclerView(savedExercises)
//            checkExerciseCompletionStatus()
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun fetchDailyExercises() {
//        Log.d("MainActivity", "Fetching daily exercises.")
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        CoroutineScope(Dispatchers.IO).launch {
//            val exercises = recommendationService.fetchAndRecommendExercises(userId)
//            Log.d("MainActivity", "Fetched exercises: $exercises")
//            dailyExercises = exercises
//            withContext(Dispatchers.Main) {
//                updateRecyclerView(exercises)
//            }
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchAndSaveDailyExercises() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val exercises = recommendationService.fetchAndRecommendExercises(userId)
            withContext(Dispatchers.Main) {
                saveDailyExercises(exercises, userId)
                dailyExercises = exercises
                updateRecyclerView(exercises)
            }
        }
    }

    private fun saveDailyExercises(exercises: List<ExerciseActivity>, userId: String) {
        val sharedPreferences = getSharedPreferences("${userId}_DailyExercisesPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val jsonExercises = gson.toJson(exercises)
        editor.putString("${userId}_dailyExercises", jsonExercises)
        editor.putLong("${userId}_lastSavedDate", System.currentTimeMillis())
        editor.apply()
    }

    private fun fetchSavedDailyExercises(): List<ExerciseActivity>? {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val sharedPreferences = getSharedPreferences("${userId}_DailyExercisesPrefs", Context.MODE_PRIVATE)
        val jsonExercises = sharedPreferences.getString("${userId}_dailyExercises", null)
        return jsonExercises?.let {
            val type = object : TypeToken<List<ExerciseActivity>>() {}.type
            Gson().fromJson(it, type)
        }
    }

    private fun isNewDay(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val sharedPreferences = getSharedPreferences("${userId}_DailyExercisesPrefs", Context.MODE_PRIVATE)
        val lastSavedDate = sharedPreferences.getLong("${userId}_lastSavedDate", 0L)
        val lastSavedDay = Calendar.getInstance().apply { timeInMillis = lastSavedDate }.get(Calendar.DAY_OF_YEAR)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return currentDay != lastSavedDay
    }

    private fun updateRecyclerView(exercises: List<ExerciseActivity>) {
        Log.d("MainActivity", "Updating RecyclerView with exercises: $exercises")
        adapter.updateExercises(exercises)
    }

    private fun checkExerciseCompletionStatus() {
        Log.d("MainActivity", "Starting to fetch exercise completion status...")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseReference = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userProgress/$userId/dailyExercises")

        val currentDate = getCurrentDate()

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Iterate over the exercises in the snapshot
                    snapshot.children.forEach { exerciseSnapshot ->
                        Log.d("MainActivity", "Processing snapshot for exercise: ${exerciseSnapshot.key}")
                        val completed = exerciseSnapshot.child("completed").getValue(Boolean::class.java) ?: false
                        val completionDate = exerciseSnapshot.child("date").getValue(String::class.java)

                        Log.d("MainActivity", "Fetched completion status for: ${exerciseSnapshot.key}, Completed: $completed")

                        if (completed && completionDate == currentDate) {
                            // Find the matching ExerciseActivity in the list and update its completed status
                            dailyExercises.find { it.name == exerciseSnapshot.key }?.let { exercise ->
                                Log.d("MainActivity", "Marking exercise: ${exercise.name} as completed.")
                                exercise.completed = true
                            }
                        }
                    }
                    runOnUiThread {
                        Log.d("MainActivity", "UI update initiated.")
                        adapter.updateExercises(dailyExercises)
                        Log.d("MainActivity", "UI should now be updated.")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to read exercise completion status.", error.toException())
            }
        })
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("MainActivity", "Result OK received, checking exercise completion status.")
            checkExerciseCompletionStatus()
        } else {
            Log.d("MainActivity", "Received result code: ${result.resultCode}")
        }
    }

    override fun onResume() {
        super.onResume()
        checkExerciseCompletionStatus()
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

}







