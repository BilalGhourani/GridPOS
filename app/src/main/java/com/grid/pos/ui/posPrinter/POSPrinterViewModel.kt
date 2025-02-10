package com.grid.pos.ui.posPrinter

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.posPrinter.PosPrinter
import com.grid.pos.data.posPrinter.PosPrinterRepository
import com.grid.pos.model.Event
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
        private val itemRepository: ItemRepository
) : BaseViewModel() {

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
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun updateState(newState: POSPrinterState) {
        state.value = newState
    }

    fun isAnyChangeDone():Boolean{
        return state.value.printer.didChanged(currentPrinter)
    }

    fun fetchPrinters() {
        state.value = state.value.copy(
            warning = null,
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfPrinters = posPrinterRepository.getAllPosPrinters()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    printers = listOfPrinters,
                    isLoading = false
                )
            }
        }
    }

    fun showWarning(
            warning: String,
            action: String? = null
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            state.value = state.value.copy(
                warning = Event(warning),
                actionLabel = action,
                isLoading = false
            )
        }
    }

    fun save() {
        val printer = state.value.printer
        if (printer.posPrinterName.isNullOrEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please fill Printer name, host and port"),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
            isLoading = true
        )
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
                            printers = printers,
                            isLoading = false,
                            warning = Event("Printer saved successfully."),
                            clear = true,
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false
                        )
                    }
                }
            } else {
                val dataModel = posPrinterRepository.update(printer)
                if (dataModel.succeed) {
                    val index = state.value.printers.indexOfFirst { it.posPrinterId == printer.posPrinterId }
                    if (index >= 0) {
                        state.value.printers.removeAt(index)
                        state.value.printers.add(
                            index,
                            printer
                        )
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false,
                            warning = Event("Printer saved successfully."),
                            clear = true,
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun delete() {
        val printer = state.value.printer
        if (printer.posPrinterId.isEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please select a Printer to delete"),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(printer.posPrinterId)) {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        warning = Event("You can't delete this Printer ,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            val dataModel = posPrinterRepository.delete(printer)
            if (dataModel.succeed) {
                val printers = state.value.printers
                printers.remove(printer)
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        printers = printers,
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        clear = true
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun hasRelations(printerId: String): Boolean {
        return itemRepository.getOneItemByPrinter(printerId) != null
    }
}