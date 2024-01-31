package com.example.finalyearproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExerciseAdapter(private val exercises: List<Exercise>, private val onClick: (Exercise) -> Unit) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    class ExerciseViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(exercise: Exercise, onClick: (Exercise) -> Unit) {
            view.findViewById<TextView>(R.id.exercise_name).text = exercise.name
            view.setOnClickListener { onClick(exercise) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.exercise_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exercises[position], onClick)
    }

    override fun getItemCount() = exercises.size
}