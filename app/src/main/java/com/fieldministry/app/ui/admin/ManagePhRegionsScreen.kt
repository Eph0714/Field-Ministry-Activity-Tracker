package com.fieldministry.app.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fieldministry.app.data.remote.dto.PhRegionDto
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.EmptyState
import com.fieldministry.app.ui.common.FullScreenLoading
import com.fieldministry.app.ui.common.GenericViewModelFactory

@Composable
fun ManagePhRegionsScreen(onBack: () -> Unit) {
    val viewModel: ManagePhRegionsViewModel = viewModel(
        factory = GenericViewModelFactory { ManagePhRegionsViewModel(ServiceLocator.phAddressRepository) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<PhRegionDto?>(null) }
    var deleteTarget by remember { mutableStateOf<PhRegionDto?>(null) }

    if (showAddDialog) {
        RegionDialog(title = "Add Region", initialPsgc = "", initialName = "", initialCode = "", showPsgc = true, onDismiss = { showAddDialog = false }) { psgc, name, code ->
            viewModel.add(psgc, name, code)
            showAddDialog = false
        }
    }
    editTarget?.let { target ->
        RegionDialog(title = "Edit Region", initialPsgc = target.psgcCode, initialName = target.name, initialCode = target.code ?: "", showPsgc = false, onDismiss = { editTarget = null }) { _, name, code ->
            viewModel.update(target.id, name, code)
            editTarget = null
        }
    }
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete ${target.name}?") },
            text = { Text("This also removes all its provinces, municipalities, and barangays.") },
            confirmButton = { TextButton(onClick = { viewModel.delete(target.id); deleteTarget = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } },
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "Manage Regions", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Add Region") }
        },
    ) { padding ->
        if (state.isLoading) {
            FullScreenLoading()
            return@Scaffold
        }
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            if (state.regions.isEmpty()) {
                EmptyState("No regions yet")
            } else {
                LazyColumn {
                    items(state.regions, key = { it.id }) { item ->
                        ListItem(
                            headlineContent = { Text(item.name) },
                            supportingContent = { Text(item.psgcCode) },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { editTarget = item }) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                                    IconButton(onClick = { deleteTarget = item }) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegionDialog(
    title: String,
    initialPsgc: String,
    initialName: String,
    initialCode: String,
    showPsgc: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
) {
    var psgc by remember { mutableStateOf(initialPsgc) }
    var name by remember { mutableStateOf(initialName) }
    var code by remember { mutableStateOf(initialCode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if (showPsgc) {
                    OutlinedTextField(value = psgc, onValueChange = { psgc = it }, label = { Text("PSGC Code") }, modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Region Code") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(psgc, name, code) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
