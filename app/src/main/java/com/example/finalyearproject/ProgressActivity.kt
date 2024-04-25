package com.example.finalyearproject

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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


class ProgressActivity : AppCompatActivity() {

    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var pieChart: PieChart
    private var exerciseType: String? = null
    private lateinit var nameToIdMap: Map<String, String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_page)

        val infoIcon: ImageView = findViewById(R.id.infoIcon)
        infoIcon.setOnClickListener {
            showPieChartInfo()
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

            pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e is PieEntry) {
                        val exerciseName = e.label
                        val exerciseId = nameToIdMap[exerciseName]
                        if (exerciseId != null) {
                            val intent = Intent(this@ProgressActivity, ExerciseProgressGraphActivity::class.java)
                            intent.putExtra("EXERCISE_ID", exerciseId)
                            startActivity(intent)
                        }
                    }
                }

                override fun onNothingSelected() {
                }
            })

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
                            exerciseType = userScoreSnapshot.key ?: return
                            val count = userScoreSnapshot.childrenCount.toInt()
                            exerciseCounts[exerciseType!!] = count
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
        val exerciseNameMap = mapOf(
            "Reading1" to "Reading",
            "SyllableCounting1" to "Syllable Counting",
            "TongueTwister1" to "Tongue Twisters",
            "Breathing1" to "Breathing",
            "ProgressiveMuscle1" to "Progressive Muscle",
            "FlexibleRateTechnique1" to "Flexible Rate"
        )

       nameToIdMap = exerciseNameMap.entries.associate { (key, value) -> value to key }

        val entries = ArrayList<PieEntry>()

        exerciseCounts.forEach { (exerciseId, count) ->
            val exerciseName = exerciseNameMap[exerciseId] ?: "Unknown Exercise"
            entries.add(PieEntry(count.toFloat(), exerciseName))
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

    private fun showPieChartInfo() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exercise Chart")
        builder.setMessage("The Chart shows your exercise usage split.\n\n" + "Ideally you want the pie chart segments to be even!\n\n" + "Click a segment to view how you have been getting on over the past week!")
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}