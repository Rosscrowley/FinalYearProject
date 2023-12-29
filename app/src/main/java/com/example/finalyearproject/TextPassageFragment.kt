package com.example.finalyearproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class TextPassageFragment : Fragment() {

    private var passageText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        passageText = arguments?.getString("passage_text")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.text_passage_fragment, container, false)
        val textViewPassage = view.findViewById<TextView>(R.id.textView_passage)
        textViewPassage.text = passageText
        return view
    }

    companion object {
        fun newInstance(passageText: String) = TextPassageFragment().apply {
            arguments = Bundle().apply {
                putString("passage_text", passageText)
            }
        }
    }
}