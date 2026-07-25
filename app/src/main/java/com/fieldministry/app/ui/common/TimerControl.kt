package com.fieldministry.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Reusable start/stop elapsed-duration tracker, shared by Searching (SRC) and Bible Study (BS)
 * screens per the spec's "timer component reusable across SRC and BS" requirement.
 */
class ElapsedTimer {
    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    var startTimeIso: String? = null
        private set
    var endTimeIso: String? = null
        private set

    private var tickJob: Job? = null

    fun start(scope: CoroutineScope) {
        if (_isRunning.value) return
        startTimeIso = Instant.now().toString()
        endTimeIso = null
        _isRunning.value = true
        tickJob = scope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
    }

    fun stop() {
        if (!_isRunning.value) return
        tickJob?.cancel()
        tickJob = null
        _isRunning.value = false
        endTimeIso = Instant.now().toString()
    }

    fun reset() {
        tickJob?.cancel()
        tickJob = null
        _isRunning.value = false
        _elapsedSeconds.value = 0
        startTimeIso = null
        endTimeIso = null
    }
}

fun formatElapsed(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

@Composable
fun TimerControlRow(
    elapsedSeconds: Int,
    isRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = formatElapsed(elapsedSeconds), style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        if (isRunning) {
            Button(onClick = onStop) { Text("Stop") }
        } else {
            Button(onClick = onStart) { Text("Start") }
        }
    }
}
