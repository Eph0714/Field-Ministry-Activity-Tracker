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
import com.fieldministry.app.data.remote.dto.PhMunicipalityDto
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.EmptyState
import com.fieldministry.app.ui.common.GenericViewModelFactory

@Composable
fun ManagePhMunicipalitiesScreen(onBack: () -> Unit) {
    val viewModel: ManagePhMunicipalitiesViewModel = viewModel(
        factory = GenericViewModelFactory { ManagePhMunicipalitiesViewModel(ServiceLocator.phAddressRepository) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    var regionMenuExpanded by remember { mutableStateOf(false) }
    var provinceMenuExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<PhMunicipalityDto?>(null) }
    var deleteTarget by remember { mutableStateOf<PhMunicipalityDto?>(null) }

    if (showAddDialog) {
        MunicipalityDialog(title = "Add Municipality/City", showPsgc = true, onDismiss = { showAddDialog = false }) { psgc, name, type ->
            viewModel.add(psgc, name, type)
            showAddDialog = false
        }
    }
    editTarget?.let { target ->
        MunicipalityDialog(title = "Edit Municipality/City", showPsgc = false, initialName = target.name, initialType = target.type, onDismiss = { editTarget = null }) { _, name, type ->
            viewModel.update(target.id, name, type)
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
        topBar = { AppTopBar(title = "Manage Municipalities/Cities", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { if (state.selectedProvince != null) showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Municipality")
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
                        DropdownMenuItem(text = { Text(region.name) }, onClick = {
                            viewModel.onRegionSelected(region)
                            regionMenuExpanded = false
                        })
                    }
                }
            }

            Spacer(Modifier.padding(top = 8.dp))
            Box {
                OutlinedButton(
                    onClick = { if (state.provinces.isNotEmpty()) provinceMenuExpanded = true },
                    enabled = state.selectedRegion != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(state.selectedProvince?.name ?: "Select Province")
                }
                DropdownMenu(expanded = provinceMenuExpanded, onDismissRequest = { provinceMenuExpanded = false }) {
                    state.provinces.forEach { province ->
                        DropdownMenuItem(text = { Text(province.name) }, onClick = {
                            viewModel.onProvinceSelected(province)
                            provinceMenuExpanded = false
                        })
                    }
                }
            }

            if (state.selectedProvince == null) {
                EmptyState("Select a province to manage its municipalities/cities")
            } else if (!state.isLoading && state.municipalities.isEmpty()) {
                EmptyState("No municipalities/cities yet for this province")
            } else {
                LazyColumn {
                    items(state.municipalities, key = { it.id }) { item ->
                        ListItem(
                            headlineContent = { Text(item.name) },
                            supportingContent = { Text("${item.type} · ${item.psgcCode}") },
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
private fun MunicipalityDialog(
    title: String,
    showPsgc: Boolean,
    initialName: String = "",
    initialType: String = "Municipality",
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
) {
    var psgc by remember { mutableStateOf("") }
    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialType) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if (showPsgc) {
                    OutlinedTextField(value = psgc, onValueChange = { psgc = it }, label = { Text("PSGC Code") }, modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                Box {
                    OutlinedButton(onClick = { typeMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) { Text("Type: $type") }
                    DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                        listOf("City", "Municipality").forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeMenuExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(psgc, name, type) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
