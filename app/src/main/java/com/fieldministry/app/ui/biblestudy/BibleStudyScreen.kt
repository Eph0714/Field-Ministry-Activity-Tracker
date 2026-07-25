package com.fieldministry.app.ui.biblestudy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.ElapsedTimer
import com.fieldministry.app.ui.common.GenericViewModelFactory
import com.fieldministry.app.ui.common.HouseholderPickerDialog
import com.fieldministry.app.ui.common.TimerControlRow

@Composable
fun BibleStudyScreen(onBack: () -> Unit, onSaved: () -> Unit) {
    val viewModel: BibleStudyViewModel = viewModel(
        factory = GenericViewModelFactory { BibleStudyViewModel(ServiceLocator.bibleStudyRepository) }
    )
    val form by viewModel.form.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val timer = remember { ElapsedTimer() }
    val elapsed by timer.elapsedSeconds.collectAsStateWithLifecycle()
    val isRunning by timer.isRunning.collectAsStateWithLifecycle()
    var showPicker by remember { mutableStateOf(false) }

    LaunchedEffect(form.saved) {
        if (form.saved) onSaved()
    }

    if (showPicker) {
        HouseholderPickerDialog(
            repository = ServiceLocator.householderRepository,
            onDismiss = { showPicker = false },
            onSelected = {
                viewModel.onHouseholderSelected(it)
                showPicker = false
            },
        )
    }

    Scaffold(topBar = { AppTopBar(title = "Log Bible Study (BS)", onBack = onBack) }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(form.householder?.name ?: "Select Householder")
            }

            Spacer(Modifier.padding(top = 16.dp))
            Text("Timer", style = MaterialTheme.typography.titleMedium)
            TimerControlRow(
                elapsedSeconds = elapsed,
                isRunning = isRunning,
                onStart = { timer.start(scope) },
                onStop = { timer.stop() },
            )

            OutlinedTextField(
                value = form.publication,
                onValueChange = viewModel::onPublicationChange,
                label = { Text("Book / Publication Used") },
                modifier = Modifier.fillMaxWidth(),
            )

            form.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = {
                    timer.stop()
                    viewModel.save(timer.startTimeIso, timer.endTimeIso, elapsed)
                },
                enabled = !form.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text(if (form.isSaving) "Saving..." else "Save")
            }
        }
    }
}
