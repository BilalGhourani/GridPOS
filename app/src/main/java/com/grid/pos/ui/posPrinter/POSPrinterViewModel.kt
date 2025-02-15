package com.grid.pos.ui.posPrinter

import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.posPrinter.PosPrinter
import com.grid.pos.data.posPrinter.PosPrinterRepository
import com.grid.pos.model.PopupModel
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class POSPrinterViewModel @Inject constructor(
    private val posPrinterRepository: PosPrinterRepository,
    private val itemRepository: ItemRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

    private val _state = MutableStateFlow(POSPrinterState())
    val state: MutableStateFlow<POSPrinterState> = _state

    var currentPrinter: PosPrinter = PosPrinter()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun resetState() {
        currentPrinter = PosPrinter()
        state.value = state.value.copy(
            printer = currentPrinter.copy(),
            posPrinterPortStr = ""
        )
    }

    fun updateState(newState: POSPrinterState) {
        state.value = newState
    }

    fun checkChanges(callback: () -> Unit) {
        if (isLoading()) {
            return
        }
        if (state.value.printer.didChanged(currentPrinter)) {
            sharedViewModel.showPopup(true,
                PopupModel().apply {
                    onDismissRequest = {
                        resetState()
                        callback.invoke()
                    }
                    onConfirmation = {
                        save {
                            checkChanges(callback)
                        }
                    }
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                })
        } else {
            callback.invoke()
        }
    }

    fun fetchPrinters() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val listOfPrinters = posPrinterRepository.getAllPosPrinters()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    printers = listOfPrinters
                )
                showLoading(false)
            }
        }
    }

    fun save(callback: () -> Unit = {}) {
        val printer = state.value.printer
        if (printer.posPrinterName.isNullOrEmpty()) {
            showWarning("Please fill Printer name, host and port")
            return
        }
        showLoading(true)
        val isInserting = printer.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                printer.prepareForInsert()
                val dataModel = posPrinterRepository.insert(printer)
                if (dataModel.succeed) {
                    val addedModel = dataModel.data as PosPrinter
                    val printers = state.value.printers
                    if (printers.isNotEmpty()) {
                        printers.add(addedModel)
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            printers = printers
                        )
                        resetState()
                        showLoading(false)
                        showWarning("Printer saved successfully.")
                        callback.invoke()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            } else {
                val dataModel = posPrinterRepository.update(printer)
                if (dataModel.succeed) {
                    val printers = state.value.printers.toMutableList()
                    val index = printers.indexOfFirst { it.posPrinterId == printer.posPrinterId }
                    if (index >= 0) {
                        printers.removeAt(index)
                        printers.add(index, printer)
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            printers = printers
                        )
                        resetState()
                        showLoading(false)
                        showWarning("Printer saved successfully.")
                        callback.invoke()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            }
        }
    }

    fun delete() {
        val printer = state.value.printer
        if (printer.posPrinterId.isEmpty()) {
            showWarning("Please select a Printer to delete")
            return
        }
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(printer.posPrinterId)) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showWarning("You can't delete this Printer ,because it has related data!")
                }
                return@launch
            }
            val dataModel = posPrinterRepository.delete(printer)
            if (dataModel.succeed) {
                val printers = state.value.printers
                printers.remove(printer)
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        printers = printers
                    )
                    resetState()
                    showLoading(false)
                    showWarning("successfully deleted.")
                }
            } else {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
    }

    private suspend fun hasRelations(printerId: String): Boolean {
        return itemRepository.getOneItemByPrinter(printerId) != null
    }
}