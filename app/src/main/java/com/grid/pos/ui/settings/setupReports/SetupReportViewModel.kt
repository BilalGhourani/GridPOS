package com.grid.pos.ui.settings.setupReports

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
import com.grid.pos.SharedViewModel
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.Event
import com.grid.pos.model.FileModel
import com.grid.pos.model.ReportTypeModel
import com.grid.pos.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SetupReportViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SetupReportState())
    val state: MutableStateFlow<SetupReportState> = _state


    fun updateState(newState: SetupReportState) {
        state.value = newState
    }

    fun onGalleryResult(
        context: Context,
        reportType: String,
        uris: List<Uri>,
        callback: () -> Unit
    ) {
        if (uris.isNotEmpty()) {
            state.value = state.value.copy(
                isLoading = true
            )
            viewModelScope.launch(Dispatchers.IO) {
                FileUtils.saveToInternalStorage(
                    context,
                    "Reports/${state.value.country}/${state.value.language.value}",
                    uris[0],
                    "$reportType.html"
                )
                withContext(Dispatchers.Main) {
                    updateState(
                        state.value.copy(
                            warning = Event("$reportType.html has been added successfully"),
                            actionLabel = null,
                            isLoading = false
                        )
                    )
                    callback.invoke()
                }
            }
        } else {
            updateState(
                state.value.copy(
                    warning = Event("Failed to add $reportType"),
                    actionLabel = null,
                    isLoading = false
                )
            )
        }
    }

    fun onPermissionDenied() {
        updateState(
            state.value.copy(
                warning = Event("Permission Denied"),
                actionLabel = "Settings"
            )
        )
    }
}
