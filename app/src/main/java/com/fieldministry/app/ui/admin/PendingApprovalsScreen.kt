package com.fieldministry.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.fieldministry.app.ui.common.EmptyState
import com.fieldministry.app.ui.common.FullScreenLoading
import com.fieldministry.app.ui.common.GenericViewModelFactory

@Composable
fun PendingApprovalsScreen(onBack: () -> Unit) {
    val viewModel: PendingApprovalsViewModel = viewModel(
        factory = GenericViewModelFactory { PendingApprovalsViewModel(ServiceLocator.userRepository) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { AppTopBar(title = "Pending User Approvals", onBack = onBack) }) { padding ->
        if (state.isLoading) {
            FullScreenLoading()
            return@Scaffold
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }

            if (state.pending.isEmpty()) {
                EmptyState("No pending signups")
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(state.pending, key = { it.id }) { user ->
                        Card(modifier = Modifier.padding(bottom = 12.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(user.name, style = MaterialTheme.typography.titleMedium)
                                Text(user.email, style = MaterialTheme.typography.bodyMedium)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 8.dp),
                                ) {
                                    Button(onClick = { viewModel.approve(user.id) }) { Text("Approve") }
                                    OutlinedButton(onClick = { viewModel.reject(user.id) }) { Text("Decline") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
