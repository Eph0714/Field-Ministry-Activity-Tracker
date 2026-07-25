package com.fieldministry.app.ui.householder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.fieldministry.app.ui.common.GenericViewModelFactory

@Composable
fun HouseholderListScreen(
    onBack: () -> Unit,
    onOpenHouseholder: (String) -> Unit,
    onAddNew: () -> Unit,
) {
    val viewModel: HouseholderListViewModel = viewModel(
        factory = GenericViewModelFactory {
            HouseholderListViewModel(ServiceLocator.householderRepository, ServiceLocator.syncManager)
        }
    )
    val query by viewModel.query.collectAsStateWithLifecycle()
    val householders by viewModel.householders.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppTopBar(title = "Householders", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNew) {
                Icon(Icons.Default.Add, contentDescription = "Log Searching")
            }
        },
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Search by name, address, or barangay") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )

            if (householders.isEmpty()) {
                EmptyState("No householders found")
            } else {
                LazyColumn {
                    items(householders, key = { it.uuid }) { item ->
                        ListItem(
                            headlineContent = { Text(item.name) },
                            supportingContent = {
                                val place = listOfNotNull(item.barangayName, item.municipalityName).joinToString(", ")
                                Text("${item.status}${if (place.isNotBlank()) " · $place" else ""}")
                            },
                            trailingContent = if (item.isDirty) {
                                { Text("Unsynced", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            modifier = Modifier.clickable { onOpenHouseholder(item.uuid) },
                        )
                    }
                }
            }
        }
    }
}
