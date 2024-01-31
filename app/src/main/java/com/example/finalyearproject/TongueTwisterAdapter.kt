package com.example.finalyearproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TongueTwisterAdapter(private val tongueTwisters: List<TongueTwister>,
                           private val listener: (TongueTwister) -> Unit)
    : RecyclerView.Adapter<TongueTwisterAdapter.TongueTwisterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TongueTwisterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tongue_twister_view, parent, false)
        return TongueTwisterViewHolder(view)
    }

    override fun onBindViewHolder(holder: TongueTwisterViewHolder, position: Int) {
        holder.bind(tongueTwisters[position], listener)
    }

    override fun getItemCount() = tongueTwisters.size

    class TongueTwisterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(tongueTwister: TongueTwister, listener: (TongueTwister) -> Unit) {
            itemView.findViewById<TextView>(R.id.tongueTwisterContentTextView).text = tongueTwister.content
            itemView.setOnClickListener { listener(tongueTwister) }
        }
    }
}