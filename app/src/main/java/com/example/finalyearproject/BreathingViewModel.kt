package com.example.finalyearproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class BreathingState {
    Inhale,
    Hold,
    Exhale
}

class BreathingViewModel : ViewModel() {
    private val _state = MutableLiveData<BreathingState>()
    val state: LiveData<BreathingState> = _state
    private var exerciseJob: Job? = null

    fun startExercise() {
        // Cancel any ongoing exercise job
        exerciseJob?.cancel()
        exerciseJob = viewModelScope.launch {
            while (isActive) { // isActive is a property of coroutine Job
                // Breathe in
                _state.postValue(BreathingState.Inhale)
                delay(4000) // Duration of breathing in

                // Hold breath
                _state.postValue(BreathingState.Hold)
                delay(4000) // Duration of holding breath

                // Breathe out
                _state.postValue(BreathingState.Exhale)
                delay(4000) // Duration of breathing out

                // Hold breath
                _state.postValue(BreathingState.Hold)
                delay(4000) // Duration of holding breath
            }
        }
    }

    fun pauseExercise() {
        // Pause the exercise by cancelling the coroutine
        exerciseJob?.cancel()
    }

    fun restartExercise() {
        // Restart the exercise
        startExercise()
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel the job when the ViewModel is cleared to avoid memory leaks
        exerciseJob?.cancel()
    }
}