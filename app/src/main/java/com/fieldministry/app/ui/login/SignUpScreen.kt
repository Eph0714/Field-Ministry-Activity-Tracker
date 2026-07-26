package com.fieldministry.app.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fieldministry.app.di.ServiceLocator
import com.fieldministry.app.ui.common.AppTopBar
import com.fieldministry.app.ui.common.GenericViewModelFactory

@Composable
fun SignUpScreen(onBack: () -> Unit, onSubmitted: () -> Unit) {
    val viewModel: SignUpViewModel = viewModel(
        factory = GenericViewModelFactory { SignUpViewModel(ServiceLocator.authRepository, ServiceLocator.phAddressRepository) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.submitted) {
        if (state.submitted) onSubmitted()
    }

    if (state.submitted) {
        Scaffold(topBar = { AppTopBar(title = "Sign Up", onBack = onBack) }) { padding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)) {
                Text(
                    "Your account has been successfully created and is awaiting administrator approval. " +
                        "You will be able to sign in once your account has been approved.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        return
    }

    Scaffold(topBar = { AppTopBar(title = "Create New Account", onBack = onBack) }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())) {

            Text("Your account will need admin approval before you can sign in.", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.padding(top = 16.dp))

            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.padding(top = 12.dp))
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Username (Email)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.padding(top = 12.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.padding(top = 12.dp))
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.padding(top = 12.dp))
            OutlinedTextField(
                value = state.contactNumber,
                onValueChange = viewModel::onContactNumberChange,
                label = { Text("Contact Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.padding(top = 16.dp))
            Text("Address", style = MaterialTheme.typography.titleMedium)

            var regionMenuExpanded by remember { mutableStateOf(false) }
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

            Spacer(modifier = Modifier.padding(top = 8.dp))
            var provinceMenuExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { if (state.provinces.isNotEmpty()) provinceMenuExpanded = true },
                    enabled = state.selectedRegion != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        state.selectedProvince?.name
                            ?: if (state.isLoadingProvinces) "Loading..." else "Select Province / Highly Urbanized City"
                    )
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
            state.provincesEmptyMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))
            var municipalityMenuExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { if (state.municipalities.isNotEmpty()) municipalityMenuExpanded = true },
                    enabled = state.selectedProvince != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        state.selectedMunicipality?.name
                            ?: if (state.isLoadingMunicipalities) "Loading..." else "Select City / Municipality"
                    )
                }
                DropdownMenu(expanded = municipalityMenuExpanded, onDismissRequest = { municipalityMenuExpanded = false }) {
                    state.municipalities.forEach { municipality ->
                        DropdownMenuItem(text = { Text(municipality.name) }, onClick = {
                            viewModel.onMunicipalitySelected(municipality)
                            municipalityMenuExpanded = false
                        })
                    }
                }
            }
            state.municipalitiesEmptyMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))
            var barangayMenuExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { if (state.barangays.isNotEmpty()) barangayMenuExpanded = true },
                    enabled = state.selectedMunicipality != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        state.selectedBarangay?.name
                            ?: if (state.isLoadingBarangays) "Loading..." else "Select Barangay"
                    )
                }
                DropdownMenu(expanded = barangayMenuExpanded, onDismissRequest = { barangayMenuExpanded = false }) {
                    state.barangays.forEach { barangay ->
                        DropdownMenuItem(text = { Text(barangay.name) }, onClick = {
                            viewModel.onBarangaySelected(barangay)
                            barangayMenuExpanded = false
                        })
                    }
                }
            }
            state.barangaysEmptyMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
            }

            Button(
                onClick = viewModel::submit,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
            ) {
                Text(if (state.isLoading) "Submitting..." else "Sign Up")
            }
        }
    }
}
