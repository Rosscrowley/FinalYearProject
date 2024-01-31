package com.example.finalyearproject

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread

class DafService(private val context: Context, private val permissionRequester: PermissionRequester) {
    var isRecording = false
    private val sampleRate = 44100
    private val delayMillis = 500
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2
    private val circularBuffer = CircularBuffer(bufferSize * delayMillis / 1000)
    private lateinit var audioRecord: AudioRecord
    private lateinit var audioTrack: AudioTrack

    init {
        initializeAudioComponents()
    }

    @SuppressLint("MissingPermission")
    private fun initializeAudioComponents() {
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
        audioTrack = AudioTrack.Builder()
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(bufferSize)
            .build()
    }

    fun startRecording() {
        if (checkPermission()) {
            if (audioRecord.state != AudioRecord.STATE_INITIALIZED || audioTrack.state != AudioTrack.STATE_INITIALIZED) {
                initializeAudioComponents()
            }

            isRecording = true
            thread {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
                audioRecord.startRecording()
                audioTrack.play()

                val tempBuffer = ShortArray(bufferSize)
                while (isRecording) {
                    val readSize = audioRecord.read(tempBuffer, 0, bufferSize)
                    if (readSize > 0) {
                        tempBuffer.forEach { circularBuffer.write(it) }

                        while (!circularBuffer.isEmpty) {
                            audioTrack.write(shortArrayOf(circularBuffer.read()), 0, 1)
                        }
                    }
                }

                audioRecord.stop()
                audioRecord.release()
                audioTrack.stop()
                audioTrack.release()
            }
        } else {
            permissionRequester.requestAudioPermissions()
        }
    }

    fun stopRecording() {
        isRecording = false
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
        return result == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}

