package com.abubakr.taskstreak.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class AccessibilityActionHelper(
    private val context: Context,
    private val onCompleteMatchingHabit: (String) -> Unit,
    private val onStatusUpdate: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null

    fun startListeningForAction() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onStatusUpdate("Speech recognition not available")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onStatusUpdate("Listening for command...")
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    onStatusUpdate("Processing voice action...")
                }
                override fun onError(error: Int) {
                    onStatusUpdate("Voice recognition error: $error")
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val command = matches?.firstOrNull()?.lowercase(Locale.getDefault()) ?: ""
                    handleVoiceCommand(command)
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun handleVoiceCommand(spokenText: String) {
        // Voice commands: "complete [habit name]", "check [habit name]", "done [habit name]"
        val regex = Regex("^(?:complete|check|done|finish|mark done|أكمل|تم|انجز)\\s+(.+)$", RegexOption.IGNORE_CASE)
        val match = regex.find(spokenText)
        if (match != null) {
            val habitQuery = match.groupValues[1].trim()
            onCompleteMatchingHabit(habitQuery)
            onStatusUpdate("Completed: $habitQuery")
        } else {
            // Check if user just spoke the habit name
            onCompleteMatchingHabit(spokenText)
            onStatusUpdate("Processed command for: $spokenText")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e("AccessibilityHelper", "Error stopping recognizer", e)
        }
    }
}
