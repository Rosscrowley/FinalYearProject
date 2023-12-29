package com.example.finalyearproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReadingTopicsAdapter(private val topics: List<String>, private val onTopicSelected: (String) -> Unit) :
    RecyclerView.Adapter<ReadingTopicsAdapter.TopicViewHolder>() {

    class TopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val topicTextView: TextView = itemView.findViewById(R.id.textView_topic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reading_topic_view, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topics[position]
        holder.topicTextView.text = topic
        holder.itemView.setOnClickListener { onTopicSelected(topic) }
    }

    override fun getItemCount() = topics.size
}