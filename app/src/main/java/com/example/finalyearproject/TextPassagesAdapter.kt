package com.example.finalyearproject

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PassagesAdapter(fragmentActivity: FragmentActivity,  private val passages: List<String>) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = passages.size

    override fun createFragment(position: Int): Fragment {
        return TextPassageFragment.newInstance(passages[position])
    }
}