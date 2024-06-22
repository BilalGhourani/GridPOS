package com.grid.pos.ui.license

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.model.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LicenseViewModel @Inject constructor(
        private val invoiceHeaderRepository: InvoiceHeaderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LicenseState())
    val state: MutableStateFlow<LicenseState> = _state

    fun showWarning(message: String,action:String) {
        state.value = state.value.copy(
            warning = Event(message),
            action = action,
            isLoading = false
        )
    }

}