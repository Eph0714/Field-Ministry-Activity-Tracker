package com.fieldministry.app.ui.searching

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.ElapsedTimer
import com.fieldministry.app.ui.common.TimerControlRow
import com.fieldministry.app.util.LocationHelper
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SearchingScreen(onBack: () -> Unit, onSaved: () -> Unit) {
    val viewModel: SearchingViewModel = viewModel(
        factory = com.fieldministry.app.ui.common.GenericViewModelFactory {
            SearchingViewModel(ServiceLocator.householderRepository, ServiceLocator.searchingRepository, ServiceLocator.referenceDataRepository)
        }
    )
    val form by viewModel.form.collectAsStateWithLifecycle()
    val municipalities by viewModel.municipalities.collectAsStateWithLifecycle()
    val barangays by viewModel.barangays.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val timer = remember { ElapsedTimer() }
    val elapsed by timer.elapsedSeconds.collectAsStateWithLifecycle()
    val isRunning by timer.isRunning.collectAsStateWithLifecycle()

    LaunchedEffect(form.saved) {
        if (form.saved) onSaved()
    }

    // GPS ----------------------------------------------------------------
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            scope.launch {
                LocationHelper.getCurrentLocation(context)?.let { viewModel.onLocationCaptured(it.latitude, it.longitude) }
            }
        }
    }
    fun captureLocation() {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            scope.launch {
                LocationHelper.getCurrentLocation(context)?.let { viewModel.onLocationCaptured(it.latitude, it.longitude) }
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Camera ---------------------------------------------------------------
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingPhotoFile?.let { viewModel.onPhotoSelected(it.absolutePath) }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
            pendingPhotoFile = file
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            takePictureLauncher.launch(uri)
        }
    }
    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            val file = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
            pendingPhotoFile = file
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var municipalityMenuExpanded by remember { mutableStateOf(false) }
    var barangayMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(topBar = { AppTopBar(title = "Log Searching (SRC)", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Timer", style = MaterialTheme.typography.titleMedium)
            TimerControlRow(
                elapsedSeconds = elapsed,
                isRunning = isRunning,
                onStart = { timer.start(scope) },
                onStop = { timer.stop() },
            )

            OutlinedTextField(
                value = form.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Householder Name *") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.padding(top = 8.dp))
            OutlinedTextField(
                value = form.address,
                onValueChange = viewModel::onAddressChange,
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.padding(top = 8.dp))
            Box {
                OutlinedButton(onClick = { municipalityMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(municipalities.firstOrNull { it.id == form.municipalityId }?.name ?: "Select Municipality")
                }
                DropdownMenu(expanded = municipalityMenuExpanded, onDismissRequest = { municipalityMenuExpanded = false }) {
                    municipalities.forEach { m ->
                        DropdownMenuItem(text = { Text(m.name) }, onClick = {
                            viewModel.onMunicipalityChange(m.id)
                            municipalityMenuExpanded = false
                        })
                    }
                }
            }

            Spacer(Modifier.padding(top = 8.dp))
            Box {
                OutlinedButton(onClick = { barangayMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(barangays.firstOrNull { it.id == form.barangayId }?.name ?: "Select Barangay")
                }
                DropdownMenu(expanded = barangayMenuExpanded, onDismissRequest = { barangayMenuExpanded = false }) {
                    barangays.forEach { b ->
                        DropdownMenuItem(text = { Text(b.name) }, onClick = {
                            viewModel.onBarangayChange(b.id)
                            barangayMenuExpanded = false
                        })
                    }
                }
            }

            Spacer(Modifier.padding(top = 8.dp))
            OutlinedTextField(
                value = form.languageSpoken,
                onValueChange = viewModel::onLanguageSpokenChange,
                label = { Text("Language Spoken") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.padding(top = 8.dp))
            OutlinedTextField(
                value = form.preferredLanguage,
                onValueChange = viewModel::onPreferredLanguageChange,
                label = { Text("Preferred Language to Use") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.padding(top = 8.dp))
            OutlinedTextField(
                value = form.maritalStatus,
                onValueChange = viewModel::onMaritalStatusChange,
                label = { Text("Marital Status") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.padding(top = 8.dp))
            OutlinedTextField(
                value = form.age,
                onValueChange = viewModel::onAgeChange,
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.padding(top = 8.dp))
            OutlinedTextField(
                value = form.contactNumber,
                onValueChange = viewModel::onContactNumberChange,
                label = { Text("Contact (CP) Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.padding(top = 8.dp))
            OutlinedTextField(
                value = form.remarks,
                onValueChange = viewModel::onRemarksChange,
                label = { Text("Remarks") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.padding(top = 16.dp))
            Row {
                OutlinedButton(onClick = { captureLocation() }) {
                    Text(if (form.latitude != null) "GPS Captured ✓" else "Capture GPS Location")
                }
                Spacer(Modifier.padding(start = 8.dp))
                OutlinedButton(onClick = { launchCamera() }) {
                    Text(if (form.localPhotoPath != null) "Photo Taken ✓" else "Take Location Photo")
                }
            }

            form.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = {
                    timer.stop()
                    viewModel.save(timer.startTimeIso, timer.endTimeIso, elapsed)
                },
                enabled = !form.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp),
            ) {
                Text(if (form.isSaving) "Saving..." else "Save")
            }
        }
    }
}
