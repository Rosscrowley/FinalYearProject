package com.example.finalyearproject
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class  UserAgeUtil {

    companion object {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)


        fun getUserAgeCategory(userId: String, callback: (String) -> Unit) {

            val userRef =
                FirebaseDatabase.getInstance("https://final-year-project-6d217-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("users/$userId")
            userRef.child("dob").get().addOnSuccessListener { dataSnapshot ->
                val dobStr = dataSnapshot.value as? String
                dobStr?.let {
                    val age = calculateAge(it)
                    val category = if (age < 12) "Kids Topics" else "Mature Topics"
                    callback(category)
                }
            }.addOnFailureListener {

            }
        }

        fun calculateAge(dobString: String): Int {
            val dob = dateFormat.parse(dobString) ?: return 0
            val cal = Calendar.getInstance()
            val today = cal.time
            cal.time = dob

            var age = today.year - dob.year
            if (today.month < dob.month || (today.month == dob.month && today.day < dob.day)) {
                age--
            }
            return age
        }
    }
}