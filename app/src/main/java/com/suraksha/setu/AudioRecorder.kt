package com.suraksha.setu

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import java.io.IOException

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var fileName: String = ""

    fun startRecording() {
        fileName = "${context.getExternalFilesDir(null)?.absolutePath}/sos_audio_${System.currentTimeMillis()}.3gp"
        
        mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun stopRecording(): String {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        return fileName // Path to uploaded/stored file
    }
}
