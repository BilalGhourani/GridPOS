package com.grid.pos.ui.license

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.model.Event
import com.grid.pos.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LicenseViewModel @Inject constructor(
        private val invoiceHeaderRepository: InvoiceHeaderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LicenseState())
    val state: MutableStateFlow<LicenseState> = _state

    fun showWarning(
            message: String,
            action: String? = null
    ) {
        state.value = state.value.copy(
            warning = Event(message),
            action = action,
            isLoading = false
        )
    }

    fun copyLicenseFile(
            context: Context,
            uri: Uri
    ) {
        if (uri.toString().isEmpty()) {
            showWarning("no file selected!")
            return
        }
        state.value = state.value.copy(
            filePath = null,
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val path = FileUtils.saveToInternalStorage(
                context = context,
                parent = "licenses",
                uri,
                "license"
            )
            withContext(Dispatchers.Main) {
                delay(1000L)
                if (!path.isNullOrEmpty()) {
                    state.value = state.value.copy(
                        filePath = path,
                        isDone = true,
                        isLoading = false
                    )
                } else {
                    showWarning("failed to copy license file!")
                }
            }

        }

    }

}