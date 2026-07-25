package com.fieldministry.app.ui.householder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.FullScreenLoading
import com.fieldministry.app.ui.common.GenericViewModelFactory
import com.fieldministry.app.ui.common.formatElapsed
import com.fieldministry.app.util.openInMaps

@Composable
fun HouseholderProfileScreen(uuid: String, onBack: () -> Unit) {
    val viewModel: HouseholderProfileViewModel = viewModel(
        factory = GenericViewModelFactory { HouseholderProfileViewModel(ServiceLocator.householderRepository, uuid) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(topBar = { AppTopBar(title = state.householder?.name ?: "Householder", onBack = onBack) }) { padding ->
        if (state.isLoading) {
            FullScreenLoading()
            return@Scaffold
        }

        val householder = state.householder
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            if (householder == null) {
                Text("Householder not found")
                return@Column
            }

            Text(householder.status, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            householder.address?.let { Text(it) }
            val place = listOfNotNull(householder.barangayName, householder.municipalityName).joinToString(", ")
            if (place.isNotBlank()) Text(place)
            householder.topic?.let { Text("Topic: $it") }
            householder.remarks?.let { Text("Remarks: $it") }

            if (householder.latitude != null && householder.longitude != null) {
                Button(
                    onClick = { openInMaps(context, householder.latitude, householder.longitude, householder.name) },
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text("View on Map")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Searching History", style = MaterialTheme.typography.titleMedium)

            if (state.searchingHistory.isEmpty()) {
                Text("No searching sessions recorded yet.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.searchingHistory.forEach { session ->
                        Text("${session.publisherName ?: "Unknown"} · ${formatElapsed(session.durationSeconds)} · ${session.startTime ?: ""}")
                    }
                }
            }
        }
    }
}
