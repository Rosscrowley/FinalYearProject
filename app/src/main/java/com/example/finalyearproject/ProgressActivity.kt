package com.example.finalyearproject

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ProgressActivity : AppCompatActivity() , OnChartValueSelectedListener {

    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_page)

        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView)
        exercisesRecyclerView.layoutManager = LinearLayoutManager(this)

        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        exercisesRecyclerView.addItemDecoration(dividerItemDecoration)

        val exercises = listOf(Exercise("Reading1", "Reading Progress"), Exercise("TongueTwister1", "Tongue Twister Progress"), Exercise("SyllableCounting1", "Syllable Counting Progress")) // Example data
        exercisesRecyclerView.adapter = ExerciseAdapter(exercises) { exercise ->
            val intent = Intent(this, ExerciseProgressGraphActivity::class.java)
            intent.putExtra("EXERCISE_ID", exercise.id)
            startActivity(intent)
        }

        bottomNavigationView = findViewById(R.id.nav_view)
        bottomNavigationView.selectedItemId = R.id.menu_item_progress
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_home -> {
                    // Navigate to Home
                    val intent = Intent(this@ProgressActivity, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_exercise -> {
                    // Navigate to Exercise
                    val intent = Intent(this@ProgressActivity, ExercisesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_wave_analysis -> {
                    // Navigate to Exercise
                    val intent = Intent(this@ProgressActivity, WaveAnalysisPageActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        pieChart = findViewById(R.id.pieChart)
        setupPieChart()
        fetchExerciseCountsAndLoadChartData()
    }

    private fun setupPieChart() {
        pieChart.apply {
            isDrawHoleEnabled = true
            setUsePercentValues(true)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
            centerText = "Exercise Split"
            setCenterTextSize(24f)
            description.isEnabled = false
            legend.isEnabled = false

            setOnChartValueSelectedListener(this@ProgressActivity)

        }
    }
    private fun fetchExerciseCountsAndLoadChartData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("userScores/$userId")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val exerciseCounts = hashMapOf<String, Int>()

                        // Count each type of exercise
                        snapshot.children.forEach { userScoreSnapshot ->
                            val exerciseType = userScoreSnapshot.key ?: return
                            val count = userScoreSnapshot.childrenCount.toInt()
                            exerciseCounts[exerciseType] = count
                        }

                        // Load the data into the PieChart
                        loadPieChartData(this@ProgressActivity, exerciseCounts)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ProgressActivity", "Failed to fetch user scores: ${error.message}")
                    }
                })
        }
    }
    private fun loadPieChartData(context: Context, exerciseCounts: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()

        exerciseCounts.forEach { (exerciseType, count) ->
            entries.add(PieEntry(count.toFloat(), exerciseType))
        }

        val dataSet = PieDataSet(entries, "Exercise Effort")

        val colors = listOf(
            ContextCompat.getColor(context, R.color.pcRed),
            ContextCompat.getColor(context, R.color.pcBlue),
            ContextCompat.getColor(context, R.color.pcGreen),
            ContextCompat.getColor(context, R.color.pcYellow),
            ContextCompat.getColor(context, R.color.pcPurple),
            ContextCompat.getColor(context, R.color.pcPink)
        )

        dataSet.colors = colors

        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val data = PieData(dataSet).apply {
            setValueTextSize(12f)
            setValueTextColor(Color.BLACK)
        }

        pieChart.data = data
        pieChart.invalidate() // Refresh the chart
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e is PieEntry) {
            pieChart.centerText = String.format("%.1f%%", e.value)
        }
    }

    override fun onNothingSelected() {
        pieChart.centerText = "Exercise Split"
    }
}