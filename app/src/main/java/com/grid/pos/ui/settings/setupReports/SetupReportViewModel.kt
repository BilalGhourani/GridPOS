package com.grid.pos.ui.settings.setupReports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
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

    fun showError(message: String) {
        state.value = state.value.copy(
            warning = Event(message),
            isLoading = false
        )
    }
}
