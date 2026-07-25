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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fieldministry.app.data.remote.dto.UserDto
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.EmptyState
import com.fieldministry.app.ui.common.FullScreenLoading
import com.fieldministry.app.ui.common.GenericViewModelFactory

private val ROLES = listOf("publisher", "overseer", "admin")

@Composable
fun ManageUsersScreen(onBack: () -> Unit) {
    val viewModel: ManageUsersViewModel = viewModel(
        factory = GenericViewModelFactory { ManageUsersViewModel(ServiceLocator.userRepository) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<UserDto?>(null) }
    var deleteTarget by remember { mutableStateOf<UserDto?>(null) }

    if (showAddDialog) {
        AddUserDialog(onDismiss = { showAddDialog = false }, onConfirm = { name, email, password, role ->
            viewModel.createUser(name, email, password, role)
            showAddDialog = false
        })
    }
    editTarget?.let { target ->
        EditUserDialog(
            user = target,
            onDismiss = { editTarget = null },
            onConfirm = { name, role, isActive -> viewModel.updateUser(target.id, name, role, isActive); editTarget = null },
            onResetPassword = { newPassword -> viewModel.resetPassword(target.id, newPassword) },
        )
    }
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete ${target.name}?") },
            text = {},
            confirmButton = { TextButton(onClick = { viewModel.deleteUser(target.id); deleteTarget = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } },
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "Manage Users", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        },
    ) { padding ->
        if (state.isLoading) {
            FullScreenLoading()
            return@Scaffold
        }
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            if (state.users.isEmpty()) {
                EmptyState("No users yet")
            } else {
                LazyColumn {
                    items(state.users, key = { it.id }) { user ->
                        ListItem(
                            headlineContent = { Text(user.name) },
                            supportingContent = { Text("${user.email} · ${user.role}${if (!user.isActive) " · inactive" else ""}") },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { editTarget = user }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { deleteTarget = user }) {
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
private fun AddUserDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("publisher") }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add User") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Temporary Password") }, modifier = Modifier.fillMaxWidth())
                RoleDropdown(role, roleMenuExpanded, { roleMenuExpanded = it }, { role = it })
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name, email, password, role) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EditUserDialog(
    user: UserDto,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean) -> Unit,
    onResetPassword: (String) -> Unit,
) {
    var name by remember { mutableStateOf(user.name) }
    var role by remember { mutableStateOf(user.role) }
    var isActive by remember { mutableStateOf(user.isActive) }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                RoleDropdown(role, roleMenuExpanded, { roleMenuExpanded = it }, { role = it })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Active")
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Reset Password (leave blank to keep)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (newPassword.isNotBlank()) {
                    TextButton(onClick = { onResetPassword(newPassword); newPassword = "" }) {
                        Text("Apply Password Reset")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name, role, isActive) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun RoleDropdown(role: String, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, onRoleChange: (String) -> Unit) {
    androidx.compose.foundation.layout.Box {
        OutlinedButton(onClick = { onExpandedChange(true) }, modifier = Modifier.fillMaxWidth()) {
            Text("Role: $role")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            ROLES.forEach { r ->
                DropdownMenuItem(text = { Text(r) }, onClick = { onRoleChange(r); onExpandedChange(false) })
            }
        }
    }
}
