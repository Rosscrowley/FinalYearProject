package com.example.finalyearproject

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AudioRecordingDao {
    @Insert
    fun insertRecording(recording: AudioRecord): Long

    @Delete
    fun deleteRecording(recording: AudioRecord)

    @Query("SELECT * FROM audioRecords")
    fun getAllRecordings(): List<AudioRecord>

    @Query("SELECT * FROM audioRecords WHERE userId = :userId")
    fun getRecordsForUser(userId: String): List<AudioRecord>
}