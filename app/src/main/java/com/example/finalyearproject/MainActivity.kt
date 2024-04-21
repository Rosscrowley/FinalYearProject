package com.example.finalyearproject

import DailyExerciseAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
import java.util.TimeZone

class MainActivity : AppCompatActivity(), DailyExerciseAdapter.ExerciseClickListener  {

    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var pronunCard: CardView
    private lateinit var exerciseCard: CardView
    private lateinit var adapter: DailyExerciseAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var recommendationService: ExerciseRecommendationService
    private val exerciseRepository = ExerciseRepository()
    private var dailyExercises: List<ExerciseActivity> = listOf()
    private lateinit var barChart: BarChart

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


        initializeOrFetchExercises()
        barChart = findViewById(R.id.xpBarchart)
        setupBarChart()
        addGoalLine(10000f)
        fetchXpDataAndUpdateChart()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                navigateProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateProfile() {

        val intent = Intent(this, ProfileActivity::class.java)
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
        builder.setMessage("Every day, you'll receive a personalised set of exercises designed to help you improve your speech and confidence. These exercises are selected based on your progress and activities from previous days.\n\n" +
                "Complete the exercises to track your progress. Your completion status for each exercise is updated in real-time, so you can see your achievements at a glance. Remember, consistency is key to improvement, so try to complete your daily exercises regularly, which will also earn you XP! \n\n" +
                "If you complete all exercises for the day, new ones will be available the next day. This ensures you have a fresh set of challenges to keep your learning journey engaging.")
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

    private fun setupBarChart() {
        barChart.apply {
            description.text = "Weekly XP Progress"
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
                granularity = 1f
                isGranularityEnabled = true
            }
            axisLeft.apply {
                axisMinimum = 0f
                granularity = 2000f // set increments to 2000
                isGranularityEnabled = true
                axisMaximum = 12000f
            }
            axisRight.isEnabled = false
        }
    }

    private fun addGoalLine(goal: Float) {
        val goalLine = LimitLine(goal, "Goal: $goal XP").apply {
            lineWidth = 4f
            lineColor = Color.RED // Ensure the color stands out
            enableDashedLine(10f, 10f, 0f)
            textColor = Color.RED
            textSize = 12f
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        }
        barChart.axisLeft.addLimitLine(goalLine)
    }

    private fun updateBarChart(data: List<BarEntry>) {
        val dataSet = BarDataSet(data, "Daily XP")

        val barColor = ContextCompat.getColor(this, R.color.appColour2)
        dataSet.color = barColor

        val data = BarData(dataSet)
        barChart.data = data
        barChart.notifyDataSetChanged()
        barChart.invalidate()
    }
    private fun fetchXpDataAndUpdateChart() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val userXpRef = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users/$uid/dailyXp")

            userXpRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val xpData = mutableListOf<BarEntry>()
                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Brussels"))

                    calendar.firstDayOfWeek = Calendar.MONDAY
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

                    val daysOfWeek = IntArray(7) // Array to store XP for each day of the week

                    for (i in 0 until 7) {
                        val date = formatter.format(calendar.time)
                        val xpValue = snapshot.child(date).getValue(Int::class.java) ?: 0
                        Log.d("Fetching Data", "Date: $date, XP: $xpValue")
                        daysOfWeek[i] = xpValue
                        xpData.add(BarEntry(i.toFloat(), xpValue.toFloat()))

                        calendar.add(Calendar.DATE, 1)  // Move to the next day
                    }

                    updateBarChart(xpData)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Failed to fetch XP data", error.toException())
                }
            })
        }
    }

}







