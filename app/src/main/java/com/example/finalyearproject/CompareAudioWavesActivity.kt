package com.example.finalyearproject
import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.protobuf.ByteString
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Math.abs
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CompareAudioWavesActivity : AppCompatActivity() {

    private lateinit var recordButton: ImageButton
    private lateinit var playButton: Button
    private lateinit var selectedText: TextView
    private lateinit var waveformView: WaveformView
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var textToSpeechService: TextToSpeechService
    private var audioFilePath: String? = null
    private lateinit var horizontalScrollView: HorizontalScrollView
    private val handler = Handler(Looper.getMainLooper())
    private var updateWaveformRunnable: Runnable? = null

    private lateinit var usersWaveformView: WaveformView
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null
    private var bufferSize = 0
    private val sampleRate = 44100 // Example sample rate
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare_audio_waves)

        horizontalScrollView = findViewById(R.id.horizontalScrollViewCorrectWave)
        textToSpeechService = TextToSpeechService(this)
        playButton = findViewById(R.id.speakButton)
        waveformView = findViewById(R.id.waveformIdCorrectWave)
        selectedText = findViewById(R.id.textView4)

        usersWaveformView = findViewById(R.id.waveformId1)

        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        recordButton = findViewById(R.id.recordButton)

        val textToConvert = intent.getStringExtra("word")
        selectedText.text = textToConvert
        Log.d("SelectedTongueTwisterActivity", "Received content: $textToConvert")

        playButton.setOnClickListener {
            if (textToConvert != null) {
                convertTextToSpeechAndPlay(textToConvert)
            }
        }

        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
                Log.d("AudioRecord", "Audio recording stopped")
            } else {
                startRecording()
                Log.d("AudioRecord", "Audio recording started")
            }
        }
    }

    private fun convertTextToSpeechAndPlay(text: String) {
        val languageCode = "en-US"
        val audioContent = textToSpeechService.convertTextToSpeech(text, languageCode)
        Log.d("AudioWavesActivity", "Converting text to speech...")
        audioContent?.let {

            val audioFilePath = saveByteStringToFile(it)
            Log.d("AudioWavesActivity", "Audio content ready. File path: $audioFilePath")
            val pcmData = extractPCMDataFromWavFile(audioFilePath)
            pcmData?.let { data ->
                Log.d("AudioWavesActivity", "PCM data extracted, sample count: ${it.size()}")
                waveformView.setAudioData(data)
                adjustWaveformWidthAndSetData(data)
                playAudio(audioFilePath)
                initWaveformUpdater()
            }
        }
    }

    private fun adjustWaveformWidthAndSetData(pcmData: ShortArray) {

        val sampleRate = 44100
        val audioDurationSeconds = pcmData.size / sampleRate.toDouble()
        val pixelsPerSecond = 100
        val waveformWidth = Math.max((audioDurationSeconds * pixelsPerSecond).toInt(), resources.displayMetrics.widthPixels)

        Log.d("AudioWavesActivity", "Calculated waveform width: $waveformWidth, Setting dynamic width...")

        waveformView.setDynamicWidth(waveformWidth)

        waveformView.setAudioData(pcmData)

    }
    private fun initWaveformUpdater() {
        updateWaveformRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        val currentPosition = mp.currentPosition.toLong()
                        waveformView.setPlaybackPosition(currentPosition)
                        scrollToCurrentPlaybackPosition()
                        handler.postDelayed(this, 100)
                    }
                }
            }
        }
        mediaPlayer?.setOnPreparedListener {
            it.start()
            handler.post(updateWaveformRunnable!!) // Start updating when playback starts
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

    private fun saveByteStringToFile(byteString: ByteString): String {
        val filePath = "${externalCacheDir?.absolutePath}/tts_output.wav"
        try {
            FileOutputStream(filePath).use { fos ->
                fos.write(byteString.toByteArray())
            }
        } catch (e: IOException) {
            Log.e("CompareAudioWavesActivity", "Error saving audio file: ", e)
        }
        return filePath
    }
    private fun extractPCMDataFromWavFile(filePath: String): ShortArray? {
        try {
            FileInputStream(filePath).use { fis ->
                // Skip WAV header (first 44 bytes)
                val skippedBytes = fis.skip(44)
                if (skippedBytes != 44L) {
                    Log.e("extractPCMDataFromWav", "Failed to skip WAV header")
                    return null
                }

                // Calculate the number of 16-bit samples
                val availableBytes = fis.available()
                val numberOfSamples = availableBytes / 2
                val pcmData = ShortArray(numberOfSamples)

                // Read the 16-bit samples
                val buffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
                for (i in 0 until numberOfSamples) {
                    if (fis.read(buffer.array()) != 2) {
                        Log.e("extractPCMDataFromWav", "Failed to read sample at position $i")
                        return null
                    }
                    pcmData[i] = buffer.getShort(0)
                    buffer.clear()
                }

                pcmData.forEachIndexed { index, value ->
                    if (index % 100 == 0) {
                        Log.d("TTSAmplitude", "Sample $index: $value")
                    }
                }

// Or log the maximum amplitude
                val maxAmplitude = pcmData.maxOrNull()
                Log.d("TTSAmplitude", "Maximum amplitude: $maxAmplitude")

                return pcmData
            }
        } catch (e: IOException) {
            Log.e("extractPCMDataFromWav", "Error reading WAV file", e)
        }
        return null
    }

    private fun playAudio(filePath: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepareAsync()
            setOnPreparedListener {
                start()
                initWaveformUpdater() // Initialize and start updating when playback starts
            }
        }
    }

    private fun scrollToCurrentPlaybackPosition() {
        mediaPlayer?.let { mp ->
            val totalWidth = waveformView.width.toFloat()
            val scrollPosition = ((mp.currentPosition.toFloat() / mp.duration) * totalWidth).toInt()
            horizontalScrollView.scrollTo(scrollPosition, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop waveform updating
        updateWaveformRunnable?.let { handler.removeCallbacks(it) }
        mediaPlayer?.release()
        mediaPlayer = null
        textToSpeechService.close()
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
        } else {
            Log.d("AudioRecord", "Buffer size: $bufferSize")
            if (bufferSize > 0) {
                audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)
                audioRecord?.startRecording()
                Log.d("AudioRecord", "Starting recording")
                isRecording = true
                // Indicate recording state on UI, e.g., change button color or icon
                usersWaveformView.isDynamic = true
                recordButton.setImageResource(R.drawable.ic_close) // Example
                recordingThread = Thread(Runnable { writeAudioDataToWaveform() }, "AudioRecorder Thread")
                recordingThread?.start()
            } else {
                // Handle invalid buffer size
                Log.e("AudioRecord", "Invalid buffer size: $bufferSize")
            }
        }
    }

    private fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingThread?.join()
        recordingThread = null
        // Revert UI changes to indicate recording has stopped
        recordButton.setImageResource(R.drawable.speech_img) // Example
    }

    private fun writeAudioDataToWaveform() {
        val audioData = ShortArray(bufferSize)
        while (isRecording) {
            Log.d("AudioData", "Sample: ${audioData.take(10).joinToString()}")
            val readSize = audioRecord?.read(audioData, 0, bufferSize) ?: 0
            Log.d("AudioRecord", "Read size: $readSize")
            if (readSize > 0) {
                // Update waveform with audio data
                // Normalize the audio data here before updating the waveform view
                val normalizedAudioData = normalizeAudioData(audioData.copyOfRange(0, readSize))

                runOnUiThread {
                    Log.d("Waveform", "Updating waveform with audio data")
                    Log.d("WaveformUpdate", "Data size: ${audioData.size}, Samples: ${audioData.take(10).joinToString()}")
                    usersWaveformView.updateLiveData(normalizedAudioData.copyOfRange(0, readSize))
                }
            }
        }
    }

    private fun normalizeAudioData(data: ShortArray, noiseThreshold: Short = 100): ShortArray {
        val maxAmplitude = data.maxOfOrNull { abs(it.toInt()) }?.toFloat() ?: 1f
        if (maxAmplitude <= noiseThreshold) {
            // If the max amplitude is lower than the noise threshold, treat it as silence
            return ShortArray(data.size)
        }

        return data.map { sample ->
            val normalizedSample = sample / maxAmplitude
            (normalizedSample * Short.MAX_VALUE).toInt().toShort()
        }.toShortArray()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("Permissions", "RECORD_AUDIO permission granted")
                startRecording()
            } else {
                // Permission was denied or request was cancelled
                Log.d("Permissions", "RECORD_AUDIO permission denied")
            }
        }
    }

}