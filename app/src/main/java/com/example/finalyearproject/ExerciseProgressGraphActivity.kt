package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ExerciseProgressGraphActivity : AppCompatActivity(), OnChartValueSelectedListener {

    private lateinit var scoreChart: LineChart
    private val exerciseNameMap = mapOf(
        "Reading1" to "Reading Progress Graph",
        "SyllableCounting1" to "Syllable Counting Progress Graph",
        "TongueTwister1" to "Tongue Twisters Progress Graph",
        "Breathing1" to "Breathing Progress Graph",
        "ProgressiveMuscle1" to "Progressive Muscle Progress Graph",
        "FlexibleRateTechnique1" to "Flexible Rate Progress Graph"
    )
    private lateinit var last7Days: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exercise_graph_activity)

        scoreChart = findViewById(R.id.score_chart)
        val toolbarTitle: TextView = findViewById(R.id.toolbarTitle)


        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            val intent = Intent(this@ExerciseProgressGraphActivity, ProgressActivity::class.java)
            startActivity(intent)
        }

        val exerciseId = intent.getStringExtra("EXERCISE_ID") ?: return
        val exerciseName = exerciseNameMap[exerciseId] ?: "Unknown Exercise"
        toolbarTitle.text = exerciseName

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        scoreChart.setOnChartValueSelectedListener(this)

        if (userId != null) {
            fetchUserScores(userId, exerciseId) { scores ->
                populateLineChart(scores, exerciseId)
            }
        }
    }

    private fun fetchUserScores(userId: String, exerciseId: String, callback: (List<ExerciseScore>) -> Unit) {
        val database = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app")
        val ref = database.getReference("userScores/$userId/$exerciseId")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val scores = snapshot.children.mapNotNull { it.getValue(ExerciseScore::class.java) }
                val filteredScores = filterScoresByRecentDates(scores)
                Log.d("ExerciseProgressGraph", "Filtered scores: $filteredScores")
                callback(filteredScores)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ExerciseProgressGraph", "Error fetching scores: ${error.message}")
            }
        })
    }

    private fun filterScoresByRecentDates(scores: List<ExerciseScore>): List<ExerciseScore> {
        val thirtyDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return scores.filter {
            try {
                val scoreDate = dateFormat.parse(it.date)
                scoreDate != null && !scoreDate.before(thirtyDaysAgo)
            } catch (e: ParseException) {
                false
            }
        }
    }

    private fun populateLineChart(scores: List<ExerciseScore>, exerciseId: String) {

         last7Days = List(7) { i ->
            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -6 + i)
            }
        }.map {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.time)
        }


        val scoresMap = last7Days.associateWith { 0f }.toMutableMap()

        scores.forEach {
            val date = it.date
            if (date in scoresMap.keys) {
                scoresMap[date] = it.score.toFloat()
            }
        }

        // Create entries for the line chart
        val entries = scoresMap.entries.sortedBy { it.key }.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.value)
        }

        val dataSet = LineDataSet(entries, "Average User Scores")
        configureDataSetAppearance(dataSet, exerciseId)


        val lineData = LineData(dataSet)
        scoreChart.data = lineData
        setupXAxis(last7Days)
        setupYAxis(exerciseId)

        scoreChart.invalidate()
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
        val index = xValue.toInt()
        if (index >= 0 && index < last7Days.size) {
            return last7Days[index]
        }
        return "Invalid date"
    }
//    private fun convertDateToXValue(dateStr: String): Float {
//        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        format.timeZone = TimeZone.getTimeZone("Europe/Brussels")
//        val date = format.parse(dateStr) ?: return 0f
//        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Brussels"))
//        calendar.time = date
//        calendar.set(Calendar.HOUR_OF_DAY, 12)
//        calendar.set(Calendar.MINUTE, 0)
//        calendar.set(Calendar.SECOND, 0)
//        calendar.set(Calendar.MILLISECOND, 0)
//        return calendar.timeInMillis.toFloat()
//    }

    private fun configureDataSetAppearance(dataSet: LineDataSet, exerciseId: String) {
        dataSet.setDrawCircles(true)
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.appColour))
        dataSet.circleRadius = 5f
    }
    private fun setupXAxis(dates: List<String>) {
        val xAxis = scoreChart.xAxis
        xAxis.valueFormatter = DateAxisValueFormatter(dates)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.textSize = 10f
        xAxis.labelRotationAngle = -45f
        xAxis.setLabelCount(dates.size, true)
    }

    private fun setupYAxis(exerciseId: String) {
        val yAxis = scoreChart.axisLeft
        scoreChart.axisRight.isEnabled = false
        when (exerciseId) {
            "Reading1", "TongueTwister1" -> {
                yAxis.axisMaximum = 5f
                yAxis.axisMinimum = 0f
                yAxis.granularity = 1f
            }
            else -> {
                yAxis.axisMaximum = 10f
                yAxis.axisMinimum = 0f
                yAxis.granularity = 1f
            }
        }
    }

    class DateAxisValueFormatter(private val dates: List<String>) : ValueFormatter() {
        private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        init {
            dateFormat.timeZone = TimeZone.getTimeZone("Europe/Brussels") // Consistent time zone
        }

        override fun getFormattedValue(value: Float): String {
            val dateIndex = value.toInt()
            if (dateIndex >= 0 && dateIndex < dates.size) {
                return dates[dateIndex]
            }
            return ""
        }
    }
}