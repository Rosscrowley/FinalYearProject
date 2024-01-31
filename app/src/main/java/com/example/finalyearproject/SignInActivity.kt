package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalyearproject.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val databaseReference = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val email = binding.emailEntered.text.toString()
            val pass = binding.passEntered.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update last login date and consecutive login count
                        firebaseAuth.currentUser?.uid?.let { userId ->
                            updateLoginInformation(userId)
                        }
                    } else {
                        Toast.makeText(this@SignInActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@SignInActivity, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLoginInformation(userId: String) {
        databaseReference.child("users").child(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    Log.d("SignInActivity", "isFirstLogin before updateLoginDetails: ${it.firstLogin}")
                    updateLoginDetails(it)
                    updateLoginDetails(it)
                    snapshot.ref.setValue(it)
                    proceedToNextActivity(it.firstLogin)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    fun updateLoginDetails(user: User) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val lastLoginDate = user.lastLoginDateString?.let { dateString ->
            try {
                dateFormat.parse(dateString)
            } catch (e: ParseException) {
                null  // Return null if the date string cannot be parsed
            }
        }
        val currentDate = Calendar.getInstance().time
        val currentDateString = dateFormat.format(currentDate)

        if (lastLoginDate != null) {
            val calendar = Calendar.getInstance()
            calendar.time = lastLoginDate
            calendar.add(Calendar.DATE, 1)  // Increment the last login date by one day

            if (dateFormat.format(calendar.time) == currentDateString) {
                // The user logged in on consecutive days
                user.consecutiveLogins += 1
            } else if (currentDateString != user.lastLoginDateString) {
                // Reset if it's not a consecutive day and the login day is different
                user.consecutiveLogins = 1
            }
        } else {
            // If it's the user's first login or the last login date is not available
            user.consecutiveLogins = 1
        }

        user.lastLoginDateString = currentDateString  // Update the last login date

        Log.d("SignInActivity", "isFirstLogin after updateLoginDetails: ${user.firstLogin}")
    }

    private fun proceedToNextActivity(isFirstLogin: Boolean) {
        val intent = if (isFirstLogin) {
            Intent(this@SignInActivity, SurveyActivity::class.java)
        } else {
            Intent(this@SignInActivity, MainActivity::class.java)
        }
        startActivity(intent)
    }
}