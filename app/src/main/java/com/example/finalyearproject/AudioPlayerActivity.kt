package com.example.finalyearproject

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
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

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var playButton: ImageButton
    private lateinit var waveformView: WaveformView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_audio)

        waveformView = findViewById(R.id.waveformId)

        val timestamp = System.currentTimeMillis()
        val outputFilePath = "${getExternalFilesDir(null)}/converted$timestamp.pcm"
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
            // Initialize MediaPlayer with the MP3 file for playback
            initializeMediaPlayer(filePath)
        } else {
            Log.e(TAG, "File path is null")
        }

        playButton.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()

            } else {
                mediaPlayer?.start()

            }
        }
    }

    private fun readAndSetPcmData(filePath: String) {
        // Assuming readPcmFile returns ShortArray suitable for your waveform visualization
        val audioData = readPcmFile(filePath)
        runOnUiThread {
            if (audioData != null) {
                waveformView.setAudioData(audioData)
            }
            // You might enable the play button here if you want to ensure waveform is ready
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
                    // Enable the play button here if you want to wait until the media is prepared
                    playButton.isEnabled = true
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
                // Keep reading until end of file
            }
            byteBuffer.flip()
            byteBuffer.asShortBuffer().get(tempAudioData)
            inputStream.close()

            // Scale and trim silence
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
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        Log.d(TAG, "MediaPlayer resources released")
        mediaPlayer = null
    }
}






