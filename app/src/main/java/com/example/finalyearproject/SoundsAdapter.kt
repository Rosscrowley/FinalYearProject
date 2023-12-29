package com.example.finalyearproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SoundsAdapter(private val soundsList: List<SoundItem>) : RecyclerView.Adapter<SoundsAdapter.SoundViewHolder>() {

    class SoundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox_sound)
        val soundTextView: TextView = itemView.findViewById(R.id.textView_sound)
        val examplesTextView: TextView = itemView.findViewById(R.id.textView_examples)
        val playButton: ImageButton = itemView.findViewById(R.id.imageButton_play)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sounds_view, parent, false)
        return SoundViewHolder(view)
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val soundItem = soundsList[position]

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = soundItem.isSelected
        holder.soundTextView.text = soundItem.sound
        holder.examplesTextView.text = soundItem.examples

        // Set up a click listener for the play button
        holder.playButton.setOnClickListener {
            // TODO: Play the sound for the item
        }

        // Handle checkbox changes
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            // Update your model with the new selection state
            soundItem.isSelected = isChecked
        }
    }

    override fun getItemCount() = soundsList.size
}