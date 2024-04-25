package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoiceRecordListActivity : AppCompatActivity() {

    private lateinit var records: ArrayList<AudioRecord>
    private lateinit var db: AppDatabase
    private lateinit var mAdapter: VoiceRecordAdapter
    private lateinit var closeButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_recordings)

        closeButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            startActivity(Intent(this@VoiceRecordListActivity, WaveAnalysisPageActivity::class.java))
        }

        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val recyclerView = findViewById<RecyclerView>(R.id.audioRecordingsRCV)
        recyclerView.layoutManager = LinearLayoutManager(this)

        records = ArrayList()
        mAdapter = VoiceRecordAdapter(records) { audioRecord ->
            val intent = Intent(this, AudioPlayerActivity::class.java).apply {
                putExtra(AudioPlayerActivity.EXTRA_AUDIO_FILE_PATH, audioRecord.filePath)
            }
            startActivity(intent)
        }
        recyclerView.adapter = mAdapter

        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            (recyclerView.layoutManager as LinearLayoutManager).orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        db = DatabaseManager.getDatabase(this)

        fetchAll(currentUserID)
    }

    private fun fetchAll(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val queryResult = db.audioRecordingDao().getRecordsForUser(userId)
            withContext(Dispatchers.Main) {
                records.clear()
                records.addAll(queryResult)
                mAdapter.notifyDataSetChanged()
            }
        }
    }
}