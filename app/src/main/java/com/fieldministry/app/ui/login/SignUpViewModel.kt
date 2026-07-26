package com.fieldministry.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldministry.app.data.remote.dto.PhBarangayDto
import com.fieldministry.app.data.remote.dto.PhMunicipalityDto
import com.fieldministry.app.data.remote.dto.PhProvinceDto
import com.fieldministry.app.data.remote.dto.PhRegionDto
import com.fieldministry.app.data.repository.AuthRepository
import com.fieldministry.app.data.repository.PhAddressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignUpState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val contactNumber: String = "",
    val regions: List<PhRegionDto> = emptyList(),
    val provinces: List<PhProvinceDto> = emptyList(),
    val municipalities: List<PhMunicipalityDto> = emptyList(),
    val barangays: List<PhBarangayDto> = emptyList(),
    val selectedRegion: PhRegionDto? = null,
    val selectedProvince: PhProvinceDto? = null,
    val selectedMunicipality: PhMunicipalityDto? = null,
    val selectedBarangay: PhBarangayDto? = null,
    val isLoadingProvinces: Boolean = false,
    val isLoadingMunicipalities: Boolean = false,
    val isLoadingBarangays: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val submitted: Boolean = false,
) {
    val provincesEmptyMessage: String?
        get() = if (selectedRegion != null && !isLoadingProvinces && provinces.isEmpty()) {
            "No provinces are currently available for the selected region."
        } else null

    val municipalitiesEmptyMessage: String?
        get() = if (selectedProvince != null && !isLoadingMunicipalities && municipalities.isEmpty()) {
            "No municipalities are currently available for the selected province."
        } else null

    val barangaysEmptyMessage: String?
        get() = if (selectedMunicipality != null && !isLoadingBarangays && barangays.isEmpty()) {
            "No barangays are currently available for the selected municipality."
        } else null
}

class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val phAddressRepository: PhAddressRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state

    init {
        viewModelScope.launch {
            val regions = runCatching { phAddressRepository.getRegions() }.getOrDefault(emptyList())
            _state.update { it.copy(regions = regions) }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value, error = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }
    fun onConfirmPasswordChange(value: String) = _state.update { it.copy(confirmPassword = value, error = null) }
    fun onContactNumberChange(value: String) = _state.update { it.copy(contactNumber = value) }

    fun onRegionSelected(region: PhRegionDto) {
        _state.update {
            it.copy(
                selectedRegion = region,
                selectedProvince = null, provinces = emptyList(),
                selectedMunicipality = null, municipalities = emptyList(),
                selectedBarangay = null, barangays = emptyList(),
                isLoadingProvinces = true,
                error = null,
            )
        }
        viewModelScope.launch {
            val provinces = runCatching { phAddressRepository.getProvinces(region.id) }.getOrDefault(emptyList())
            _state.update { it.copy(provinces = provinces, isLoadingProvinces = false) }
        }
    }

    fun onProvinceSelected(province: PhProvinceDto) {
        _state.update {
            it.copy(
                selectedProvince = province,
                selectedMunicipality = null, municipalities = emptyList(),
                selectedBarangay = null, barangays = emptyList(),
                isLoadingMunicipalities = true,
                error = null,
            )
        }
        viewModelScope.launch {
            val municipalities = runCatching { phAddressRepository.getMunicipalities(province.id) }.getOrDefault(emptyList())
            _state.update { it.copy(municipalities = municipalities, isLoadingMunicipalities = false) }
        }
    }

    fun onMunicipalitySelected(municipality: PhMunicipalityDto) {
        _state.update {
            it.copy(
                selectedMunicipality = municipality,
                selectedBarangay = null, barangays = emptyList(),
                isLoadingBarangays = true,
                error = null,
            )
        }
        viewModelScope.launch {
            val barangays = runCatching { phAddressRepository.getBarangays(municipality.id) }.getOrDefault(emptyList())
            _state.update { it.copy(barangays = barangays, isLoadingBarangays = false) }
        }
    }

    fun onBarangaySelected(barangay: PhBarangayDto) {
        _state.update { it.copy(selectedBarangay = barangay, error = null) }
    }

    fun submit() {
        val current = _state.value
        if (current.name.isBlank() || current.email.isBlank() || current.password.isBlank() || current.contactNumber.isBlank()) {
            _state.update { it.copy(error = "All fields are required") }
            return
        }
        if (current.password != current.confirmPassword) {
            _state.update { it.copy(error = "Passwords do not match") }
            return
        }
        if (current.selectedRegion == null || current.selectedProvince == null ||
            current.selectedMunicipality == null || current.selectedBarangay == null
        ) {
            _state.update { it.copy(error = "Province, City/Municipality, and Barangay are required") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                authRepository.signup(
                    name = current.name,
                    email = current.email,
                    password = current.password,
                    contactNumber = current.contactNumber,
                    phRegionId = current.selectedRegion.id,
                    phProvinceId = current.selectedProvince.id,
                    phMunicipalityId = current.selectedMunicipality.id,
                    phBarangayId = current.selectedBarangay.id,
                )
                _state.update { it.copy(isLoading = false, submitted = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Sign up failed. That email may already be registered.") }
            }
        }
    }
}
