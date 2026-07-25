package com.fieldministry.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.GenericViewModelFactory

@Composable
fun DashboardScreen(
    onLoggedOut: () -> Unit,
    onOpenHouseholders: () -> Unit,
    onLogSearching: () -> Unit,
    onLogBibleStudy: () -> Unit,
    onLogReturnVisit: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenReports: () -> Unit,
) {
    val viewModel: DashboardViewModel = viewModel(
        factory = GenericViewModelFactory {
            DashboardViewModel(
                ServiceLocator.sessionManager,
                ServiceLocator.syncManager,
                ServiceLocator.networkMonitor,
                ServiceLocator.userRepository,
            )
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.session) {
        if (state.session == null) onLoggedOut()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Field Ministry Tracker",
                actions = { TextButton(onClick = viewModel::logout) { Text("Logout") } },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Welcome, ${state.session?.name ?: ""}", style = MaterialTheme.typography.headlineSmall)
            Text("Role: ${state.session?.role ?: ""}", style = MaterialTheme.typography.bodyMedium)

            Text(
                text = if (state.isOnline) "Online" else "Offline — changes will sync when reconnected",
                color = if (state.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
            if (state.pendingCount > 0) {
                Text("${state.pendingCount} change(s) waiting to sync")
            }
            state.syncError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            OutlinedButton(onClick = viewModel::sync, enabled = !state.isSyncing, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isSyncing) "Syncing..." else "Sync")
            }

            Button(onClick = onLogSearching, modifier = Modifier.fillMaxWidth()) {
                Text("Log Searching (SRC)")
            }

            Button(onClick = onLogBibleStudy, modifier = Modifier.fillMaxWidth()) {
                Text("Log Bible Study (BS)")
            }

            Button(onClick = onLogReturnVisit, modifier = Modifier.fillMaxWidth()) {
                Text("Log Return Visit (RV)")
            }

            Button(onClick = onOpenHouseholders, modifier = Modifier.fillMaxWidth()) {
                Text("Householders")
            }

            val role = state.session?.role
            if (role == "overseer" || role == "admin") {
                OutlinedButton(onClick = onOpenReports, modifier = Modifier.fillMaxWidth()) {
                    Text("Reports")
                }
            }

            if (role == "admin") {
                OutlinedButton(onClick = onOpenAdmin, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (state.pendingApprovalCount > 0) {
                            "Admin (${state.pendingApprovalCount} pending)"
                        } else {
                            "Admin"
                        }
                    )
                }
            }
        }
    }
}
