package com.fieldministry.app.ui.reports

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.FullScreenLoading
import com.fieldministry.app.ui.common.GenericViewModelFactory
import com.fieldministry.app.ui.common.formatElapsed
import com.fieldministry.app.util.ExportUtils

@Composable
fun ReportsScreen(onBack: () -> Unit) {
    val viewModel: ReportsViewModel = viewModel(
        factory = GenericViewModelFactory { ReportsViewModel(ServiceLocator.reportRepository, ServiceLocator.referenceDataRepository) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val municipalities by viewModel.municipalities.collectAsStateWithLifecycle()
    val barangays by viewModel.barangays.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isAdmin = ServiceLocator.sessionManager.isAdmin()

    var municipalityMenuExpanded by remember { mutableStateOf(false) }
    var barangayMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(topBar = { AppTopBar(title = "Reports", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { municipalityMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(municipalities.firstOrNull { it.id == state.municipalityId }?.name ?: "All Municipalities")
                    }
                    DropdownMenu(expanded = municipalityMenuExpanded, onDismissRequest = { municipalityMenuExpanded = false }) {
                        DropdownMenuItem(text = { Text("All Municipalities") }, onClick = {
                            viewModel.onMunicipalityChange(null)
                            municipalityMenuExpanded = false
                        })
                        municipalities.forEach { m ->
                            DropdownMenuItem(text = { Text(m.name) }, onClick = {
                                viewModel.onMunicipalityChange(m.id)
                                municipalityMenuExpanded = false
                            })
                        }
                    }
                }
                Spacer(Modifier.padding(start = 8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { barangayMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(barangays.firstOrNull { it.id == state.barangayId }?.name ?: "All Barangays")
                    }
                    DropdownMenu(expanded = barangayMenuExpanded, onDismissRequest = { barangayMenuExpanded = false }) {
                        DropdownMenuItem(text = { Text("All Barangays") }, onClick = {
                            viewModel.onBarangayChange(null)
                            barangayMenuExpanded = false
                        })
                        barangays.forEach { b ->
                            DropdownMenuItem(text = { Text(b.name) }, onClick = {
                                viewModel.onBarangayChange(b.id)
                                barangayMenuExpanded = false
                            })
                        }
                    }
                }
            }

            if (state.isLoading) {
                FullScreenLoading()
                return@Scaffold
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.padding(top = 16.dp))
            Text("Summary", style = MaterialTheme.typography.titleMedium)
            state.summary?.let { summary ->
                Text("Searching: ${summary.searching.sessionCount} sessions · ${formatElapsed(summary.searching.totalSeconds.toIntOrNull() ?: 0)}")
                Text("Bible Studies: ${summary.bibleStudies.studyCount} · ${formatElapsed(summary.bibleStudies.totalSeconds.toIntOrNull() ?: 0)}")
                Text("Return Visits: ${summary.returnVisits.visitCount}")
            }

            Spacer(Modifier.padding(top = 16.dp))
            Text("Searching — by Publisher", style = MaterialTheme.typography.titleMedium)
            if (state.searchingRows.isEmpty()) {
                Text("No data for this filter")
            } else {
                state.searchingRows.forEach { row ->
                    Text("${row.publisherName}: ${row.sessionCount} sessions · ${formatElapsed(row.totalSeconds.toIntOrNull() ?: 0)}")
                }
            }

            Spacer(Modifier.padding(top = 16.dp))
            Text("Bible Study — by Publisher", style = MaterialTheme.typography.titleMedium)
            if (state.bibleStudyRows.isEmpty()) {
                Text("No data for this filter")
            } else {
                state.bibleStudyRows.forEach { row ->
                    Text("${row.publisherName}: ${row.studyCount} studies · ${formatElapsed(row.totalSeconds.toIntOrNull() ?: 0)}")
                }
            }

            Spacer(Modifier.padding(top = 16.dp))
            Text("Return Visits — by Publisher", style = MaterialTheme.typography.titleMedium)
            if (state.returnVisitRows.isEmpty()) {
                Text("No data for this filter")
            } else {
                state.returnVisitRows.forEach { row ->
                    Text("${row.publisherName}: ${row.visitCount} visits")
                }
            }

            Spacer(Modifier.padding(top = 16.dp))
            Text("Potential Return Visits", style = MaterialTheme.typography.titleMedium)
            if (state.potentialRv.isEmpty()) {
                Text("None flagged")
            } else {
                state.potentialRv.forEach { h ->
                    val place = listOfNotNull(h.barangayName, h.municipalityName).joinToString(", ")
                    Text("${h.name}${if (place.isNotBlank()) " · $place" else ""}")
                }
            }

            if (isAdmin) {
                Spacer(Modifier.padding(top = 24.dp))
                Row {
                    Button(onClick = {
                        val header = listOf("Publisher", "SRC Sessions", "SRC Hours", "BS Count", "BS Hours", "RV Count")
                        val names = (state.searchingRows.map { it.publisherName } +
                            state.bibleStudyRows.map { it.publisherName } +
                            state.returnVisitRows.map { it.publisherName }).distinct()
                        val rows = names.map { name ->
                            val src = state.searchingRows.firstOrNull { it.publisherName == name }
                            val bs = state.bibleStudyRows.firstOrNull { it.publisherName == name }
                            val rv = state.returnVisitRows.firstOrNull { it.publisherName == name }
                            listOf(
                                name,
                                src?.sessionCount ?: "0",
                                formatElapsed(src?.totalSeconds?.toIntOrNull() ?: 0),
                                bs?.studyCount ?: "0",
                                formatElapsed(bs?.totalSeconds?.toIntOrNull() ?: 0),
                                rv?.visitCount ?: "0",
                            )
                        }
                        ExportUtils.exportCsv(context, "field_ministry_report.csv", header, rows, "Field Ministry Activity Report")
                    }) {
                        Text("Export CSV")
                    }
                    Spacer(Modifier.padding(start = 8.dp))
                    OutlinedButton(onClick = {
                        val header = listOf("Publisher", "SRC", "BS", "RV")
                        val names = (state.searchingRows.map { it.publisherName } +
                            state.bibleStudyRows.map { it.publisherName } +
                            state.returnVisitRows.map { it.publisherName }).distinct()
                        val rows = names.map { name ->
                            val src = state.searchingRows.firstOrNull { it.publisherName == name }
                            val bs = state.bibleStudyRows.firstOrNull { it.publisherName == name }
                            val rv = state.returnVisitRows.firstOrNull { it.publisherName == name }
                            listOf(name, src?.sessionCount ?: "0", bs?.studyCount ?: "0", rv?.visitCount ?: "0")
                        }
                        ExportUtils.exportPdf(context, "field_ministry_report.pdf", "Field Ministry Activity Report", header, rows)
                    }) {
                        Text("Export PDF")
                    }
                }
            }
        }
    }
}
