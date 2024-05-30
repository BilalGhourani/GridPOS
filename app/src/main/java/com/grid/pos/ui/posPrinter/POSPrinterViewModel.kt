package com.grid.pos.ui.posPrinter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.data.PosPrinter.PosPrinterRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class POSPrinterViewModel @Inject constructor(
        private val posPrinterRepository: PosPrinterRepository
) : ViewModel() {

    private val _posPrinterState = MutableStateFlow(POSPrinterState())
    val posPrinterState: MutableStateFlow<POSPrinterState> = _posPrinterState

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
        posPrinterRepository.getAllPosPrinters(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfPrinters = mutableListOf<PosPrinter>()
                (result as List<*>).forEach {
                    listOfPrinters.add(it as PosPrinter)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    posPrinterState.value = posPrinterState.value.copy(
                        printers = listOfPrinters
                    )
                }
            }

            override fun onFailure(
                    message: String,
                    errorCode: Int
            ) {

            }

        })
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
        if (printer.posPrinterName.isNullOrEmpty() || printer.posPrinterHost.isEmpty() || printer.posPrinterPort == -1) {
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
        val callback = object : OnResult {
            override fun onSuccess(result: Any) {
                viewModelScope.launch(Dispatchers.Main) {
                    val addedModel = result as PosPrinter
                    val printers = posPrinterState.value.printers
                    if (isInserting) {
                        printers.add(addedModel)
                    }
                    posPrinterState.value = posPrinterState.value.copy(
                        printers = printers,
                        selectedPrinter = PosPrinter(),
                        isLoading = false,
                        clear = true,
                    )
                }
            }

            override fun onFailure(
                    message: String,
                    errorCode: Int
            ) {
                viewModelScope.launch(Dispatchers.Main) {
                    posPrinterState.value = posPrinterState.value.copy(
                        isLoading = false
                    )
                }
            }

        }
        posPrinterState.value.selectedPrinter.let {
            CoroutineScope(Dispatchers.IO).launch {
                if (isInserting) {
                    it.prepareForInsert()
                    posPrinterRepository.insert(
                        it,
                        callback
                    )
                } else {
                    posPrinterRepository.update(
                        it,
                        callback
                    )
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
            posPrinterRepository.delete(printer,
                object : OnResult {
                    override fun onSuccess(result: Any) {
                        val printers = posPrinterState.value.printers
                        val position = printers.indexOfFirst {
                            printer.posPrinterId.equals(
                                it.posPrinterId,
                                ignoreCase = true
                            )
                        }
                        if (position >= 0) {
                            printers.removeAt(position)
                        }
                        viewModelScope.launch(Dispatchers.Main) {
                            posPrinterState.value = posPrinterState.value.copy(
                                printers = printers,
                                selectedPrinter = PosPrinter(),
                                isLoading = false,
                                clear = true
                            )
                        }
                    }

                    override fun onFailure(
                            message: String,
                            errorCode: Int
                    ) {
                        viewModelScope.launch(Dispatchers.Main) {
                            posPrinterState.value = posPrinterState.value.copy(
                                isLoading = false
                            )
                        }
                    }

                })
        }
    }
}