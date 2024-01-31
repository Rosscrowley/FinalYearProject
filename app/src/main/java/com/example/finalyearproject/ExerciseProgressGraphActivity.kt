package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExerciseProgressGraphActivity : AppCompatActivity(), OnChartValueSelectedListener {

    private lateinit var scoreChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exercise_graph_activity)

        scoreChart = findViewById(R.id.score_chart)

        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            val intent = Intent(this@ExerciseProgressGraphActivity, ProgressActivity::class.java)
            startActivity(intent)
        }

        val exerciseId = intent.getStringExtra("EXERCISE_ID") ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        scoreChart.setOnChartValueSelectedListener(this)

        if (userId != null) {
            fetchUserScores(userId, exerciseId) { scores ->
                populateLineChart(scores)
            }
        }
    }

    private fun fetchUserScores(userId: String, exerciseId: String, callback: (List<ExerciseScore>) -> Unit) {
        val database = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app")
        val ref = database.getReference("userScores/$userId/$exerciseId")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val scores = snapshot.children.mapNotNull { it.getValue(ExerciseScore::class.java) }
                Log.d("ExerciseProgressGraph", "Fetched scores: $scores")
                callback(scores)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors, e.g., show an error message
                Log.e("ExerciseProgressGraph", "Error fetching scores: ${error.message}")
            }
        })
    }

    private fun populateLineChart(scores: List<ExerciseScore>) {
        // Group scores by date and calculate the average score for each date
        val averageScoresByDate = scores
            .groupBy { it.date }
            .mapValues { (_, dailyScores) ->
                dailyScores.map { it.score }.average().toFloat()
            }

        // Convert the map to chart entries
        val entries = averageScoresByDate.map { (date, avgScore) ->
            Entry(convertDateToXValue(date), avgScore)
        }

        val dataSet = LineDataSet(entries, "Average User Scores")

        Log.d("ExerciseProgressGraph", "Plotting entries: $entries")

        dataSet.setDrawCircles(true)
        dataSet.setCircleColor(R.color.gold) // Set the color of the circles
        dataSet.circleRadius = 5f // Set the radius of the circles

        val lineData = LineData(dataSet)
        scoreChart.data = lineData

        val xAxis = scoreChart.xAxis
        xAxis.valueFormatter = DateAxisValueFormatter()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.textSize = 10f
        xAxis.labelRotationAngle = -45f

        scoreChart.invalidate() // refresh chart
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        e?.let {
            val date = convertXValueToDate(it.x)
            val score = it.y
            Toast.makeText(this, "Your average score on $date was $score", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onNothingSelected() {

    }

    private fun convertXValueToDate(xValue: Float): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = Date(xValue.toLong())
        return format.format(date)
    }
    private fun convertDateToXValue(dateStr: String): Float {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateStr) ?: return 0f
        // Convert to a timestamp representing the start of the day
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis.toFloat()
    }

    class DateAxisValueFormatter : ValueFormatter() {
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        override fun getFormattedValue(value: Float): String {
            val date = Date(value.toLong())
            return dateFormat.format(date)
        }
    }
}