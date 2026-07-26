package com.fieldministry.app.ui.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fieldministry.app.data.remote.dto.PhBarangayDto
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.EmptyState
import com.fieldministry.app.ui.common.GenericViewModelFactory

@Composable
fun ManagePhBarangaysScreen(onBack: () -> Unit) {
    val viewModel: ManagePhBarangaysViewModel = viewModel(
        factory = GenericViewModelFactory { ManagePhBarangaysViewModel(ServiceLocator.phAddressRepository) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    var regionMenuExpanded by remember { mutableStateOf(false) }
    var provinceMenuExpanded by remember { mutableStateOf(false) }
    var municipalityMenuExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<PhBarangayDto?>(null) }
    var deleteTarget by remember { mutableStateOf<PhBarangayDto?>(null) }

    if (showAddDialog) {
        NameWithCodeDialog(title = "Add Barangay", showPsgc = true, onDismiss = { showAddDialog = false }) { psgc, name ->
            viewModel.add(psgc, name)
            showAddDialog = false
        }
    }
    editTarget?.let { target ->
        NameWithCodeDialog(title = "Edit Barangay", showPsgc = false, initialName = target.name, onDismiss = { editTarget = null }) { _, name ->
            viewModel.update(target.id, name)
            editTarget = null
        }
    }
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete ${target.name}?") },
            confirmButton = { TextButton(onClick = { viewModel.delete(target.id); deleteTarget = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } },
            text = {},
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "Manage Barangays", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { if (state.selectedMunicipality != null) showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Barangay")
            }
        },
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {
            Box {
                OutlinedButton(onClick = { regionMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(state.selectedRegion?.name ?: "Select Region")
                }
                DropdownMenu(expanded = regionMenuExpanded, onDismissRequest = { regionMenuExpanded = false }) {
                    state.regions.forEach { region ->
                        DropdownMenuItem(text = { Text(region.name) }, onClick = { viewModel.onRegionSelected(region); regionMenuExpanded = false })
                    }
                }
            }

            Spacer(Modifier.padding(top = 8.dp))
            Box {
                OutlinedButton(
                    onClick = { if (state.provinces.isNotEmpty()) provinceMenuExpanded = true },
                    enabled = state.selectedRegion != null,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(state.selectedProvince?.name ?: "Select Province") }
                DropdownMenu(expanded = provinceMenuExpanded, onDismissRequest = { provinceMenuExpanded = false }) {
                    state.provinces.forEach { province ->
                        DropdownMenuItem(text = { Text(province.name) }, onClick = { viewModel.onProvinceSelected(province); provinceMenuExpanded = false })
                    }
                }
            }

            Spacer(Modifier.padding(top = 8.dp))
            Box {
                OutlinedButton(
                    onClick = { if (state.municipalities.isNotEmpty()) municipalityMenuExpanded = true },
                    enabled = state.selectedProvince != null,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(state.selectedMunicipality?.name ?: "Select Municipality/City") }
                DropdownMenu(expanded = municipalityMenuExpanded, onDismissRequest = { municipalityMenuExpanded = false }) {
                    state.municipalities.forEach { municipality ->
                        DropdownMenuItem(text = { Text(municipality.name) }, onClick = { viewModel.onMunicipalitySelected(municipality); municipalityMenuExpanded = false })
                    }
                }
            }

            if (state.selectedMunicipality == null) {
                EmptyState("Select a municipality/city to manage its barangays")
            } else {
                Spacer(Modifier.padding(top = 8.dp))
                OutlinedTextField(
                    value = state.search,
                    onValueChange = viewModel::onSearchChange,
                    label = { Text("Search barangay") },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (!state.isLoading && state.barangays.isEmpty()) {
                    EmptyState("No barangays found")
                } else {
                    LazyColumn {
                        items(state.barangays, key = { it.id }) { item ->
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
}

@Composable
private fun NameWithCodeDialog(title: String, showPsgc: Boolean, initialName: String = "", onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var psgc by remember { mutableStateOf("") }
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if (showPsgc) {
                    OutlinedTextField(value = psgc, onValueChange = { psgc = it }, label = { Text("PSGC Code") }, modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(psgc, name) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
