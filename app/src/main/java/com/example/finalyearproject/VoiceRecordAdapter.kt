package com.example.finalyearproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class VoiceRecordAdapter(
    private var audioRecords: List<AudioRecord>,
    private val onItemClick: (AudioRecord) -> Unit
) : RecyclerView.Adapter<VoiceRecordAdapter.AudioRecordingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioRecordingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.voice_recording_view, parent, false)
        return AudioRecordingViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: AudioRecordingViewHolder, position: Int) {
        val audioRecord = audioRecords[position]
        holder.bind(audioRecord)
    }

    override fun getItemCount(): Int = audioRecords.size

    class AudioRecordingViewHolder(itemView: View, private val onItemClick: (AudioRecord) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.audioName)
        private val dateTextView: TextView = itemView.findViewById(R.id.textDate)

        fun bind(audioRecord: AudioRecord) {
            nameTextView.text = audioRecord.filename
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateTextView.text = sdf.format(Date(audioRecord.timestamp))

            // Set the click listener to invoke the onItemClick lambda with the current AudioRecord
            itemView.setOnClickListener {
                onItemClick(audioRecord)
            }
        }
    }
}