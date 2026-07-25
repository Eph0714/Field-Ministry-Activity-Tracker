package com.fieldministry.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fieldministry.app.ui.common.AppTopBar

@Composable
fun AdminHomeScreen(
    onBack: () -> Unit,
    onPendingApprovals: () -> Unit,
    onManageUsers: () -> Unit,
    onManageMunicipalities: () -> Unit,
    onManageBarangays: () -> Unit,
) {
    Scaffold(topBar = { AppTopBar(title = "Admin", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = onPendingApprovals, modifier = Modifier.fillMaxWidth()) {
                Text("Pending User Approvals")
            }
            Button(onClick = onManageUsers, modifier = Modifier.fillMaxWidth()) {
                Text("Manage Users")
            }
            Button(onClick = onManageMunicipalities, modifier = Modifier.fillMaxWidth()) {
                Text("Manage Municipalities")
            }
            Button(onClick = onManageBarangays, modifier = Modifier.fillMaxWidth()) {
                Text("Manage Barangays")
            }
        }
    }
}
