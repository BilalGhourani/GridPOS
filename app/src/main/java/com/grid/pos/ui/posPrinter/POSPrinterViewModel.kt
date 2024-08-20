package com.grid.pos.ui.posPrinter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.data.PosPrinter.PosPrinterRepository
import com.grid.pos.model.Event
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
) : ViewModel() {

    private val _posPrinterState = MutableStateFlow(POSPrinterState())
    val posPrinterState: MutableStateFlow<POSPrinterState> = _posPrinterState
    var currentPrinter: PosPrinter? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchPrinters()
        }
    }

    fun fillCachedPrinters(printers: MutableList<PosPrinter> = mutableListOf()) {
        if (posPrinterState.value.printers.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                posPrinterState.value = posPrinterState.value.copy(
                    printers = printers
                )
            }
        }
    }

    private suspend fun fetchPrinters() {
        val listOfPrinters = posPrinterRepository.getAllPosPrinters()
        posPrinterState.value = posPrinterState.value.copy(
            printers = listOfPrinters
        )
    }

    fun showWarning(
            warning: String,
            action: String
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            posPrinterState.value = posPrinterState.value.copy(
                warning = Event(warning),
                actionLabel = action,
                isLoading = false
            )
        }
    }

    fun savePrinter(printer: PosPrinter) {
        if (printer.posPrinterName.isNullOrEmpty()) {
            posPrinterState.value = posPrinterState.value.copy(
                warning = Event("Please fill Printer name, host and port"),
                isLoading = false
            )
            return
        }
        posPrinterState.value = posPrinterState.value.copy(
            isLoading = true
        )
        val isInserting = printer.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                printer.prepareForInsert()
                val addedModel = posPrinterRepository.insert(printer)
                val printers = posPrinterState.value.printers
                printers.add(addedModel)
                posPrinterState.value = posPrinterState.value.copy(
                    printers = printers,
                    selectedPrinter = PosPrinter(),
                    isLoading = false,
                    warning = Event("Printer saved successfully."),
                    clear = true,
                )
            } else {
                posPrinterRepository.update(printer)
                posPrinterState.value = posPrinterState.value.copy(
                    selectedPrinter = PosPrinter(),
                    isLoading = false,
                    warning = Event("Printer saved successfully."),
                    clear = true,
                )
            }
        }
    }

    fun deleteSelectedPrinter(printer: PosPrinter) {
        if (printer.posPrinterId.isEmpty()) {
            posPrinterState.value = posPrinterState.value.copy(
                warning = Event("Please select a Printer to delete"),
                isLoading = false
            )
            return
        }
        posPrinterState.value = posPrinterState.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(printer.posPrinterId)) {
                withContext(Dispatchers.Main) {
                    posPrinterState.value = posPrinterState.value.copy(
                        warning = Event("You can't delete this Printer ,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            posPrinterRepository.delete(printer)
            val printers = posPrinterState.value.printers
            printers.remove(printer)
            viewModelScope.launch(Dispatchers.Main) {
                posPrinterState.value = posPrinterState.value.copy(
                    printers = printers,
                    selectedPrinter = PosPrinter(),
                    isLoading = false,
                    warning = Event("successfully deleted."),
                    clear = true
                )
            }
        }
    }

    private suspend fun hasRelations(printerId: String): Boolean {
        if (itemRepository.getOneItemByPrinter(printerId) != null) return true

        return false
    }
}