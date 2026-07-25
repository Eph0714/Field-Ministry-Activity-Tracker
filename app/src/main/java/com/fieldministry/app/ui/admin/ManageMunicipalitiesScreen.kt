package com.fieldministry.app.ui.admin

import androidx.compose.foundation.clickable
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
import com.fieldministry.app.data.local.entity.MunicipalityEntity
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.EmptyState
import com.fieldministry.app.ui.common.GenericViewModelFactory

@Composable
fun ManageMunicipalitiesScreen(onBack: () -> Unit) {
    val viewModel: ManageMunicipalitiesViewModel = viewModel(
        factory = GenericViewModelFactory { ManageMunicipalitiesViewModel(ServiceLocator.referenceDataRepository) }
    )
    val municipalities by viewModel.municipalities.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<MunicipalityEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<MunicipalityEntity?>(null) }

    if (showAddDialog) {
        NameDialog(
            title = "Add Municipality",
            initialValue = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { viewModel.add(it); showAddDialog = false },
        )
    }
    editTarget?.let { target ->
        NameDialog(
            title = "Edit Municipality",
            initialValue = target.name,
            onDismiss = { editTarget = null },
            onConfirm = { viewModel.update(target.id, it); editTarget = null },
        )
    }
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete ${target.name}?") },
            text = { Text("This also removes its barangays.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(target.id); deleteTarget = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } },
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "Manage Municipalities", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Municipality")
            }
        },
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            if (municipalities.isEmpty()) {
                EmptyState("No municipalities yet")
            } else {
                LazyColumn {
                    items(municipalities, key = { it.id }) { item ->
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

@Composable
fun NameDialog(title: String, initialValue: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var value by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        },
        confirmButton = { TextButton(onClick = { onConfirm(value) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
