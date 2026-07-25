package com.fieldministry.app.ui.admin

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fieldministry.app.data.local.entity.BarangayEntity
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.EmptyState
import com.fieldministry.app.ui.common.GenericViewModelFactory

@Composable
fun ManageBarangaysScreen(onBack: () -> Unit) {
    val viewModel: ManageBarangaysViewModel = viewModel(
        factory = GenericViewModelFactory { ManageBarangaysViewModel(ServiceLocator.referenceDataRepository) }
    )
    val municipalities by viewModel.municipalities.collectAsStateWithLifecycle()
    val barangays by viewModel.barangays.collectAsStateWithLifecycle()

    var municipalityMenuExpanded by remember { mutableStateOf(false) }
    var selectedMunicipalityId by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<BarangayEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<BarangayEntity?>(null) }

    val visibleBarangays = barangays.filter { selectedMunicipalityId == null || it.municipalityId == selectedMunicipalityId }

    if (showAddDialog && selectedMunicipalityId != null) {
        NameDialog(
            title = "Add Barangay",
            initialValue = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { viewModel.add(selectedMunicipalityId!!, it); showAddDialog = false },
        )
    }
    editTarget?.let { target ->
        NameDialog(
            title = "Edit Barangay",
            initialValue = target.name,
            onDismiss = { editTarget = null },
            onConfirm = { viewModel.update(target.id, it); editTarget = null },
        )
    }
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete ${target.name}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(target.id); deleteTarget = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } },
            text = {},
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "Manage Barangays", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { if (selectedMunicipalityId != null) showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Barangay")
            }
        },
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            Box {
                OutlinedButton(onClick = { municipalityMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(municipalities.firstOrNull { it.id == selectedMunicipalityId }?.name ?: "Select Municipality")
                }
                DropdownMenu(expanded = municipalityMenuExpanded, onDismissRequest = { municipalityMenuExpanded = false }) {
                    municipalities.forEach { m ->
                        DropdownMenuItem(text = { Text(m.name) }, onClick = {
                            selectedMunicipalityId = m.id
                            municipalityMenuExpanded = false
                        })
                    }
                }
            }

            if (selectedMunicipalityId == null) {
                EmptyState("Select a municipality to manage its barangays")
            } else if (visibleBarangays.isEmpty()) {
                EmptyState("No barangays yet")
            } else {
                LazyColumn {
                    items(visibleBarangays, key = { it.id }) { item ->
                        ListItem(
                            headlineContent = { Text(item.name) },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { editTarget = item }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { deleteTarget = item }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
