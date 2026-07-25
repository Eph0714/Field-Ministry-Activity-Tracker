package com.fieldministry.app.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fieldministry.app.data.local.entity.HouseholderEntity
import com.fieldministry.app.data.repository.HouseholderRepository

/**
 * Search-and-select existing householder, shared by Bible Study and Return Visit entry
 * (both require picking from the existing householder database rather than re-entering one).
 */
@Composable
fun HouseholderPickerDialog(
    repository: HouseholderRepository,
    onDismiss: () -> Unit,
    onSelected: (HouseholderEntity) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val allHouseholders by repository.observeAll().collectAsStateWithLifecycle(initialValue = emptyList())
    val filtered = remember(query, allHouseholders) {
        if (query.isBlank()) {
            allHouseholders
        } else {
            allHouseholders.filter {
                it.name.contains(query, ignoreCase = true) || (it.address?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Householder")
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search by name or address") },
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.uuid }) { item ->
                        ListItem(
                            headlineContent = { Text(item.name) },
                            supportingContent = {
                                val place = listOfNotNull(item.barangayName, item.municipalityName).joinToString(", ")
                                Text("${item.status}${if (place.isNotBlank()) " · $place" else ""}")
                            },
                            modifier = Modifier.clickable { onSelected(item) },
                        )
                    }
                }
            }
        }
    }
}
