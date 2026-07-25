package com.fieldministry.app.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GenericViewModelFactory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <U : ViewModel> create(modelClass: Class<U>): U = creator() as U
}
