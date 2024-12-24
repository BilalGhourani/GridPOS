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

    private val _posPrinterState = MutableStateFlow(POSPrinterState())
    val posPrinterState: MutableStateFlow<POSPrinterState> = _posPrinterState
    var currentPrinter: PosPrinter = PosPrinter()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun resetState() {
        posPrinterState.value = posPrinterState.value.copy(
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun fetchPrinters() {
        posPrinterState.value = posPrinterState.value.copy(
            warning = null,
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val dataModel = posPrinterRepository.getAllPosPrinters()
            if (dataModel.succeed) {
                val listOfPrinters = convertToMutableList(
                    dataModel.data,
                    PosPrinter::class.java
                )
                withContext(Dispatchers.Main) {
                    posPrinterState.value = posPrinterState.value.copy(
                        printers = listOfPrinters,
                        isLoading = false
                    )
                }
            } else if (dataModel.message != null) {
                showWarning(dataModel.message)
            }
        }
    }

    fun showWarning(
            warning: String,
            action: String? = null
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
                val dataModel = posPrinterRepository.insert(printer)
                if (dataModel.succeed) {
                    val addedModel = dataModel.data as PosPrinter
                    val printers = posPrinterState.value.printers
                    if (printers.isNotEmpty()) {
                        printers.add(addedModel)
                    }
                    withContext(Dispatchers.Main) {
                        posPrinterState.value = posPrinterState.value.copy(
                            printers = printers,
                            selectedPrinter = PosPrinter(),
                            isLoading = false,
                            warning = Event("Printer saved successfully."),
                            clear = true,
                        )
                    }
                } else if (dataModel.message != null) {
                    showWarning(dataModel.message)
                }
            } else {
                val dataModel = posPrinterRepository.update(printer)
                if (dataModel.succeed) {
                    val index = posPrinterState.value.printers.indexOfFirst { it.posPrinterId == printer.posPrinterId }
                    if (index >= 0) {
                        posPrinterState.value.printers.removeAt(index)
                        posPrinterState.value.printers.add(
                            index,
                            printer
                        )
                    }
                    withContext(Dispatchers.Main) {
                        posPrinterState.value = posPrinterState.value.copy(
                            selectedPrinter = PosPrinter(),
                            isLoading = false,
                            warning = Event("Printer saved successfully."),
                            clear = true,
                        )
                    }
                } else if (dataModel.message != null) {
                    showWarning(dataModel.message)
                }
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
            val dataModel = posPrinterRepository.delete(printer)
            if (dataModel.succeed) {
                val printers = posPrinterState.value.printers
                printers.remove(printer)
                withContext(Dispatchers.Main) {
                    posPrinterState.value = posPrinterState.value.copy(
                        printers = printers,
                        selectedPrinter = PosPrinter(),
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        clear = true
                    )
                }
            } else if (dataModel.message != null) {
                showWarning(dataModel.message)
            }
        }
    }

    private suspend fun hasRelations(printerId: String): Boolean {
        val itemDataModel = itemRepository.getOneItemByPrinter(printerId)
        return itemDataModel.succeed && itemDataModel.data != null
    }
}