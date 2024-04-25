package com.example.finalyearproject

import android.content.Intent
import android.content.res.Resources
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

class AudioPlayerActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_AUDIO_FILE_PATH = "EXTRA_AUDIO_FILE_PATH"
        private const val TAG = "AudioPlayerActivity"
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateWaveformRunnable: Runnable
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var playButton: ImageButton
    private lateinit var waveformView: WaveformView
    private lateinit var horizontalScrollView: HorizontalScrollView
    private lateinit var closeButton: ImageButton
    private lateinit var audioSeekBar: SeekBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_audio)

        audioSeekBar = findViewById(R.id.seekBar)
        audioSeekBar.progress = 0

        closeButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            startActivity(Intent(this@AudioPlayerActivity, WaveAnalysisPageActivity::class.java))
        }

        waveformView = findViewById(R.id.waveformId)

        horizontalScrollView = findViewById(R.id.horizontalScrollView)
       // val timestamp = System.currentTimeMillis()
        //val outputFilePath = "${getExternalFilesDir(null)}/converted$timestamp.pcm"
        val filePath = intent.getStringExtra(EXTRA_AUDIO_FILE_PATH)
        Log.d(TAG, "File path received: $filePath")

        playButton = findViewById(R.id.btnPlay)

        if (filePath != null) {
            val timestamp = System.currentTimeMillis()
            val outputFilePath = "${getExternalFilesDir(null)}/converted$timestamp.pcm"
            // Convert MP3 to PCM for waveform visualization
            convertMp3ToPcm(filePath, outputFilePath) {

                readAndSetPcmData(outputFilePath)
            }

            initializeMediaPlayer(filePath)
        } else {
            Log.e(TAG, "File path is null")
        }

        initWaveformUpdater()

        playButton.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()

            } else {
                Log.d(TAG, "Starting playback")
                mediaPlayer?.start()
                handler.post(updateWaveformRunnable) // Ensure this is being called

            }
        }
    }

    private fun initWaveformUpdater() {
        updateWaveformRunnable = Runnable {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    val currentPosition = mp.currentPosition.toLong()
                    waveformView.setPlaybackPosition(currentPosition)
                    updateProgressBar(currentPosition, mp.duration)

                    val scrollPosition = calculateScrollPosition(currentPosition)
                    horizontalScrollView.scrollTo(scrollPosition, 0) // Scroll to the calculated position

                    handler.postDelayed(updateWaveformRunnable, 100) // Schedule the next update
                }
            }
        }
    }

    private fun calculateScrollPosition(currentPositionMs: Long): Int {
        val proportionPlayed = currentPositionMs.toFloat() / mediaPlayer?.duration!!
        val totalWidth = waveformView.width
        val scrollPosition = (proportionPlayed * totalWidth).toInt()

        // Adjust the scroll position to keep the playback indicator centered
        val scrollViewWidth = horizontalScrollView.width
        val centeredPosition = (scrollPosition - scrollViewWidth / 2).coerceAtLeast(0)

        return centeredPosition
    }

    private fun readAndSetPcmData(filePath: String) {
        val audioData = readPcmFile(filePath)
        runOnUiThread {
            if (audioData != null) {
                Log.d(TAG, "Setting audio data for waveform")
                waveformView.setAudioData(audioData)
            }else {
                Log.e(TAG, "Failed to load audio data")
            }

        }
    }

    private fun initializeMediaPlayer(filePath: String) {
        mediaPlayer = MediaPlayer().apply {
            try {
                Log.d(TAG, "Initializing MediaPlayer with path: $filePath")
                setDataSource(filePath)
                prepareAsync() // Prepare the MediaPlayer asynchronously

                setOnPreparedListener {
                    Log.d(TAG, "MediaPlayer is prepared and ready to play")
                    val durationMs = this.duration.toLong()
                    playButton.isEnabled = true
                    waveformView.totalDurationMs = durationMs
                    adjustWaveformWidth(durationMs)
                }

                setOnCompletionListener {
                    Log.d(TAG, "Playback completed")

                }

                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "Playback error occurred: what = $what, extra = $extra")
                    true
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error initializing MediaPlayer: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun readPcmFile(filePath: String, scale: Float = 10.5f, silenceThreshold: Short = 1100): ShortArray? {
        try {
            val file = File(filePath)
            val size = file.length().toInt() / 2 // 2 bytes per short
            val tempAudioData = ShortArray(size)

            val inputStream = FileInputStream(file).channel
            val byteBuffer = ByteBuffer.allocateDirect(size * 2)
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
            while (inputStream.read(byteBuffer) > 0) {
            }
            byteBuffer.flip()
            byteBuffer.asShortBuffer().get(tempAudioData)
            inputStream.close()

            val scaledAndTrimmedAudioData = tempAudioData
                .map { (it * scale).toInt().toShort() } // Scale
                .filterIndexed { index, value ->
                    abs(value.toInt()) > silenceThreshold || (index > 0 && abs(tempAudioData[index - 1].toInt()) > silenceThreshold) || (index < tempAudioData.size - 1 && abs(tempAudioData[index + 1].toInt()) > silenceThreshold)
                }
                .toShortArray()

            return scaledAndTrimmedAudioData

        } catch (e: IOException) {
            Log.e("AudioPlayerActivity", "Error reading PCM file: ${e.message}")
            e.printStackTrace()
        }
        return null
    }

    private fun convertMp3ToPcm(inputFilePath: String, outputFilePath: String, onConversionComplete: () -> Unit) {
        val cmd = arrayOf("-i", inputFilePath, "-f", "s16le", "-acodec", "pcm_s16le", outputFilePath)
        FFmpeg.executeAsync(cmd) { _, returnCode ->
            if (returnCode == RETURN_CODE_SUCCESS) {
                Log.i(TAG, "Conversion successful")
                onConversionComplete()
            } else {
                Log.e(TAG, "Conversion failed with return code: $returnCode")
            }
        }
    }

    private fun adjustWaveformWidth(durationMs: Long) {
        Log.d(TAG, "Audio duration: $durationMs ms")

        var pixelsPerSecond = 1000

        if (durationMs > 60000) {
            pixelsPerSecond += 50
        }

        val totalWidth = ((durationMs / 1000F) * pixelsPerSecond).toInt()

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val baseMinWidth = 2000
        val minWidth = Math.max(screenWidth, baseMinWidth)

        val waveformWidth = Math.max(minWidth, totalWidth)

        Log.d(TAG, "Calculated waveform width: $waveformWidth")

      waveformView.setDynamicWidth(waveformWidth)

        Log.d(TAG, "Waveform width dynamically set to: $waveformWidth pixels")
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        Log.d(TAG, "MediaPlayer resources released")
        mediaPlayer = null
    }

    override fun onStart() {
        super.onStart()
        // Start updating the waveform when the activity starts
        handler.post(updateWaveformRunnable)
    }

    override fun onStop() {
        super.onStop()
        // Stop updating the waveform when the activity stops
        handler.removeCallbacks(updateWaveformRunnable)
    }
    private fun updateProgressBar(currentPosition: Long, totalDuration: Int) {
        val progress = (currentPosition.toDouble() / totalDuration * 100).toInt()
        audioSeekBar.progress = progress
    }
}






