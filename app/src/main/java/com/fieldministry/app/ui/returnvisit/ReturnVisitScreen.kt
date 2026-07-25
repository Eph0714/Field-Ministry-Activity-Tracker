package com.fieldministry.app.ui.returnvisit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.GenericViewModelFactory
import com.fieldministry.app.ui.common.HouseholderPickerDialog

@Composable
fun ReturnVisitScreen(onBack: () -> Unit, onSaved: () -> Unit) {
    val viewModel: ReturnVisitViewModel = viewModel(
        factory = GenericViewModelFactory { ReturnVisitViewModel(ServiceLocator.returnVisitRepository) }
    )
    val form by viewModel.form.collectAsStateWithLifecycle()
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

    Scaffold(topBar = { AppTopBar(title = "Log Return Visit (RV)", onBack = onBack) }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(form.householder?.name ?: "Select Householder")
            }

            form.householder?.topic?.let {
                Spacer(Modifier.padding(top = 12.dp))
                Text("Recent Topic", style = MaterialTheme.typography.labelLarge)
                Text(it)
            }
            form.householder?.remarks?.let {
                Spacer(Modifier.padding(top = 8.dp))
                Text("Previous Remarks", style = MaterialTheme.typography.labelLarge)
                Text(it)
            }

            Spacer(Modifier.padding(top = 16.dp))
            OutlinedTextField(
                value = form.outcomeNotes,
                onValueChange = viewModel::onOutcomeNotesChange,
                label = { Text("Outcome / Notes for this visit") },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Text("Candidate for future Return Visit")
                Spacer(Modifier.padding(start = 8.dp))
                Switch(checked = form.isPotentialRv, onCheckedChange = viewModel::onPotentialRvChange)
            }

            form.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = viewModel::save,
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
