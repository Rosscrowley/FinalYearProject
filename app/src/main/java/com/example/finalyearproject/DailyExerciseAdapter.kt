
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearproject.ExerciseActivity
import com.example.finalyearproject.R

class DailyExerciseAdapter(
    private var exercises: List<ExerciseActivity>,
    private val activityContext: Context,
    private val exerciseClickListener: ExerciseClickListener
) : RecyclerView.Adapter<DailyExerciseAdapter.ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.daily_exercise_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exercises[position], activityContext, exerciseClickListener)
    }

    interface ExerciseClickListener {
        fun onExerciseStart(exerciseName: String, activityClassName: String)
    }

    override fun getItemCount() = exercises.size

    fun updateExercises(newExercises: List<ExerciseActivity>) {
        Log.d("DailyExerciseAdapter", "Received new exercises list of size: ${newExercises.size}")

        exercises = newExercises
        notifyDataSetChanged()
    }

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.exerciseName)
        private val completionStatusTextView: TextView = itemView.findViewById(R.id.textView5)
        private val startButton: Button = itemView.findViewById(R.id.startButton)
        private val exerciseIcon: ImageView = itemView.findViewById(R.id.exerciseIcon)


        fun bind(exercise: ExerciseActivity, context: Context, clickListener: ExerciseClickListener) {
            Log.d("ExerciseViewHolder", "Binding data for exercise: ${exercise.name}, Completed: ${exercise.completed}")
            nameTextView.text = exercise.name
            exerciseIcon.setImageResource(getImageResource(exercise.name))


            if (exercise.completed) {
                completionStatusTextView.text = "Completed"
                completionStatusTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.DEComplete))
            } else {
                completionStatusTextView.text = "Incomplete"
                completionStatusTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.DEIncomplete))
            }

            startButton.setOnClickListener {
                clickListener.onExerciseStart(exercise.name, exercise.activityClassName ?: "")
            }
        }

        private fun getImageResource(exerciseName: String): Int {
            return when (exerciseName) {
                "Breathing Exercise" -> R.drawable.breathing_exercises
                "Reading Comprehension" -> R.drawable.reading_exercise
                "Tongue Twisters" -> R.drawable.tongue_twister_exercise
                "Syllable Counting" -> R.drawable.syllable_counting_exercise
                "Flexible Rate Technique" -> R.drawable.flex_rate_exercise
                "Progressive Muscle Exercise" -> R.drawable.muscle_exercise
                else -> R.drawable.tongue_twister_exercise // Fallback icon
            }
        }
    }
}
