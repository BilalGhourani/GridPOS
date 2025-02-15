package com.grid.pos.ui.settings.setupReports

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SetupReportViewModel @Inject constructor(
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

    private val _state = MutableStateFlow(SetupReportState())
    val state: MutableStateFlow<SetupReportState> = _state


    fun updateState(newState: SetupReportState) {
        state.value = newState
    }

    fun fetchCountries() {
        sharedViewModel.fetchCountries { list ->
            updateState(
                state.value.copy(
                    countries = list
                )
            )
        }
    }

    fun getReportType(): String? {
        return sharedViewModel.selectedReportType
    }

    fun addReport(context: Context, callback: () -> Unit) {
        val reportType = sharedViewModel.selectedReportType
        sharedViewModel.launchFilePicker("text/html",
            object : OnGalleryResult {
                override fun onGalleryResult(uris: List<Uri>) {
                    if (uris.isNotEmpty()) {
                        showLoading(true)
                        viewModelScope.launch(Dispatchers.IO) {
                            FileUtils.saveToInternalStorage(
                                context,
                                "Reports/${state.value.country}/${state.value.language.value}",
                                uris[0],
                                "$reportType.html"
                            )
                            withContext(Dispatchers.Main) {
                                showLoading(false)
                                showWarning("$reportType.html has been added successfully")
                                callback.invoke()
                            }
                        }
                    } else {
                        showLoading(false)
                        showWarning("Failed to add $reportType")
                    }
                }
            },
            onPermissionDenied = {
                showWarning(
                    "Permission Denied", "Settings"
                ) {
                    sharedViewModel.openAppStorageSettings()
                }
            })
    }
}
