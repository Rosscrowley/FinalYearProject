import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearproject.ExerciseActivity
import com.example.finalyearproject.R

class DailyExerciseAdapter(
    private var exercises: List<ExerciseActivity>,
    private val activityContext: Context
) : RecyclerView.Adapter<DailyExerciseAdapter.ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.daily_exercise_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exercises[position], activityContext)
    }

    override fun getItemCount() = exercises.size

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.exerciseName)
        private val startButton: Button = itemView.findViewById(R.id.startButton)

        fun bind(exercise: ExerciseActivity, context: Context) {
            nameTextView.text = exercise.name
            startButton.setOnClickListener {
                val intent = Intent(context, exercise.activityClass)
                context.startActivity(intent)
            }
        }
    }
    fun updateExercises(newExercises: List<ExerciseActivity>) {
        exercises = newExercises
        notifyDataSetChanged()
    }
}