package com.example.finalyearproject

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalyearproject.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app").reference

        binding.dobEntered.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDatePickerDialog()
            }
        }

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val email = binding.emailEntered.text.toString()
            val pass = binding.passEntered.text.toString()
            val confirmPass = binding.confirmPassEntered.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val name = binding.nameEntered.text.toString()
                            val dob = binding.dobEntered.text.toString()
                            val email = binding.emailEntered.text.toString()

                            val user = User(name, dob,email, feared_sounds = emptyList(), firstLogin = true, lastLoginDateString = "", consecutiveLogins = 0)
                            val userId = firebaseAuth.currentUser?.uid

                            if (isValidDob(dob)) {
                                userId?.let {
                                    database.child("users").child(it).setValue(user)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val intent =
                                                    Intent(this, SignInActivity::class.java)
                                                startActivity(intent)
                                            } else {
                                                Toast.makeText(
                                                    this,
                                                    "Failed to save user data",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }
                            } else {
                                Toast.makeText(this, "Please input valid date of birth", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun isValidDob(input: String): Boolean {
        return input.matches("\\d{2}/\\d{2}/\\d{4}".toRegex())
    }

    private fun showDatePickerDialog() {
        // Temporarily set inputType to none to prevent the keyboard from showing
        binding.dobEntered.inputType = InputType.TYPE_NULL

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)
                binding.dobEntered.setText(formattedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.setOnDismissListener {
            // Revert inputType to none after DatePickerDialog is dismissed
            binding.dobEntered.inputType = InputType.TYPE_NULL
        }

        datePickerDialog.show()
    }
}