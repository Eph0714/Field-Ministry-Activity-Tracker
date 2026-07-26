package com.fieldministry.app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.GenericViewModelFactory
import com.fieldministry.app.ui.theme.BannerBlue

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
                ServiceLocator.searchingRepository,
                ServiceLocator.bibleStudyRepository,
                ServiceLocator.returnVisitRepository,
                ServiceLocator.householderRepository,
            )
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.session) {
        if (state.session == null) onLoggedOut()
    }

    // DashboardViewModel survives navigating away and back (it's scoped to the nav back-stack
    // entry), so re-check for an update every time this composable re-enters composition - i.e.
    // every time the user returns to the Dashboard, not just on the very first app launch.
    LaunchedEffect(Unit) {
        viewModel.refreshUpdateCheck()
    }

    val role = state.session?.role

    state.availableUpdate?.let { update ->
        AlertDialog(
            onDismissRequest = viewModel::dismissUpdate,
            title = { Text("Update Available") },
            text = { Text("Version ${update.version} is ready. Do you want to update now?") },
            confirmButton = {
                TextButton(onClick = { viewModel.installUpdate(context) }, enabled = !state.isDownloadingUpdate) {
                    Text(if (state.isDownloadingUpdate) "Downloading..." else "Update")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissUpdate) { Text("Not Now") }
            },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Field Ministry Tracker",
                actions = { TextButton(onClick = viewModel::logout) { Text("Logout", color = MaterialTheme.colorScheme.onPrimary) } },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Text("Welcome, ${state.session?.name ?: ""}", style = MaterialTheme.typography.headlineSmall)
                Text("Role: ${role ?: ""}", style = MaterialTheme.typography.bodyMedium)
            }

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (state.isOnline) "Online" else "Offline — changes will sync when reconnected",
                        color = if (state.isOnline) BannerBlue else MaterialTheme.colorScheme.error,
                    )
                    if (state.pendingCount > 0) {
                        Text("${state.pendingCount} change(s) waiting to sync")
                    }
                    state.syncError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    OutlinedButton(onClick = viewModel::sync, enabled = !state.isSyncing, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text(if (state.isSyncing) "Syncing..." else "Sync")
                    }
                }
            }

            Text("Overview", style = MaterialTheme.typography.titleMedium)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(180.dp),
            ) {
                items(
                    listOf(
                        Triple("Searching", state.searchingCount, Icons.Filled.Search),
                        Triple("Bible Studies", state.bibleStudyCount, Icons.Filled.MenuBook),
                        Triple("Return Visits", state.returnVisitCount, Icons.Filled.Undo),
                        Triple("Householders", state.householderCount, Icons.Filled.Groups),
                    )
                ) { (label, count, icon) ->
                    StatCard(label = label, count = count, icon = icon)
                }
            }

            Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
            val actions = buildList {
                add(Triple("Log Searching", Icons.Filled.Search, onLogSearching))
                add(Triple("Log Bible Study", Icons.Filled.MenuBook, onLogBibleStudy))
                add(Triple("Log Return Visit", Icons.Filled.Undo, onLogReturnVisit))
                add(Triple("Householders", Icons.Filled.Groups, onOpenHouseholders))
                if (role == "overseer" || role == "admin") {
                    add(Triple("Reports", Icons.Filled.Assessment, onOpenReports))
                }
                if (role == "admin") {
                    add(Triple("Admin", Icons.Filled.AdminPanelSettings, onOpenAdmin))
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height((((actions.size + 1) / 2) * 110).dp),
            ) {
                items(actions) { (label, icon, onClick) ->
                    ActionCard(
                        label = label,
                        icon = icon,
                        badgeCount = if (label == "Admin") state.pendingApprovalCount else 0,
                        onClick = onClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, count: Int, icon: ImageVector) {
    Card(modifier = Modifier.aspectRatio(1.6f)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(icon, contentDescription = null, tint = BannerBlue)
            Column {
                Text(count.toString(), style = MaterialTheme.typography.headlineMedium)
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ActionCard(label: String, icon: ImageVector, badgeCount: Int, onClick: () -> Unit) {
    Card(modifier = Modifier
        .aspectRatio(1.6f)
        .clickable(onClick = onClick)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(icon, contentDescription = null, tint = BannerBlue)
                if (badgeCount > 0) {
                    Text(badgeCount.toString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(label, style = MaterialTheme.typography.titleSmall)
        }
    }
}
