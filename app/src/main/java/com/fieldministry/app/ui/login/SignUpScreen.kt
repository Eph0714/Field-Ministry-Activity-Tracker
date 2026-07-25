package com.fieldministry.app.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
        factory = GenericViewModelFactory { SignUpViewModel(ServiceLocator.authRepository) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.submitted) {
        if (state.submitted) onSubmitted()
    }

    Scaffold(topBar = { AppTopBar(title = "Sign Up", onBack = onBack) }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp)) {

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
                label = { Text("Email") },
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

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = viewModel::submit,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text(if (state.isLoading) "Submitting..." else "Sign Up")
            }
        }
    }
}
