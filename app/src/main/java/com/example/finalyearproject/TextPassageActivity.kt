package com.example.finalyearproject

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.finalyearproject.ChooseReadingTopicActivity.Companion.EXTRA_CATEGORY_NAME
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TextPassageActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.viewpager2)

        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME)
        viewPager = findViewById(R.id.viewPager_passages)

        viewPager.adapter = PassagesAdapter(this, emptyList())

        getPassagesForCategory(categoryName) { passages ->
            // This will be called with the list of passages
            // Make sure this is run on the main thread if it updates the UI
            runOnUiThread {
                viewPager.adapter = PassagesAdapter(this, passages)
            }
        }
    }

    private fun getPassagesForCategory(categoryName: String?, callback: (List<String>) -> Unit) {
        Log.d("PassagesActivity", "Fetching passages for category: $categoryName")

        val databaseReference = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").reference
        val passagesReference = databaseReference.child("reading_texts").child(categoryName ?: "")

        Log.d("PassagesActivity", "Database reference: $passagesReference")
        passagesReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("PassagesActivity", "I am in the onDataChange function")
                Log.d("PassagesActivity", "onDataChange called with snapshot: $dataSnapshot")
                val passages = mutableListOf<String>()
                for (snapshot in dataSnapshot.children) {
                    Log.d("PassagesActivity", "I am in the for loop")
                    Log.d("PassagesActivity", "Key: ${snapshot.key}, Value: ${snapshot.value}")
                    snapshot.getValue(String::class.java)?.let {
                        passages.add(it)
                        Log.d("PassagesActivity", "Fetched passage: $it") // Log each passage
                    }
                }
                Log.d("PassagesActivity", "Total passages fetched: ${passages.size}") // Log total count
                callback(passages)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
                Log.w("PassagesActivity", "loadPost:onCancelled", databaseError.toException()) // Log errors

                // Add a more detailed log with the error message
                Log.w("PassagesActivity", "Error fetching data: ${databaseError.message}")
            }
        })
    }


}