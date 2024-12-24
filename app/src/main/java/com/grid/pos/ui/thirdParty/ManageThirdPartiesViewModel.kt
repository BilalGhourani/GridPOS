package com.grid.pos.ui.thirdParty

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.thirdParty.ThirdPartyRepository
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
class ManageThirdPartiesViewModel @Inject constructor(
        private val thirdPartyRepository: ThirdPartyRepository,
        private val invoiceHeaderRepository: InvoiceHeaderRepository
) : BaseViewModel() {

    private val _manageThirdPartiesState = MutableStateFlow(ManageThirdPartiesState())
    val manageThirdPartiesState: MutableStateFlow<ManageThirdPartiesState> = _manageThirdPartiesState
    var currentThirdParty: ThirdParty = ThirdParty()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            val dataModel = thirdPartyRepository.getDefaultThirdParty()
            val isDefaultEnabled = dataModel.succeed && dataModel.data == null
            withContext(Dispatchers.Main) {
                manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                    enableIsDefault = isDefaultEnabled
                )
            }
        }
    }

    fun resetState() {
        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun fetchThirdParties() {
        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
            warning = null,
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val dataModel = thirdPartyRepository.getAllThirdParties()
            if (dataModel.succeed) {
                val listOfThirdParties = convertToMutableList(
                    dataModel.data,
                    ThirdParty::class.java
                )
                val isDefaultEnabled = listOfThirdParties.none { it.thirdPartyDefault }
                withContext(Dispatchers.Main) {
                    manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                        thirdParties = listOfThirdParties,
                        enableIsDefault = isDefaultEnabled,
                        isLoading = false
                    )
                }
            } else if (dataModel.message != null) {
                withContext(Dispatchers.Main) {
                    manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                        isLoading = false,
                        warning = Event(dataModel.message),

                        )
                }
            }
        }
    }

    fun saveThirdParty(thirdParty: ThirdParty) {
        if (thirdParty.thirdPartyName.isNullOrEmpty()) {
            manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                warning = Event("Please fill ThirdParty name."),
                isLoading = false
            )
            return
        }
        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
            isLoading = true
        )
        val isInserting = thirdParty.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                thirdParty.prepareForInsert()
                val dataModel = thirdPartyRepository.insert(thirdParty)
                if (dataModel.succeed) {
                    val addedModel = dataModel.data as ThirdParty
                    val thirdParties = manageThirdPartiesState.value.thirdParties
                    if (thirdParties.isNotEmpty()) {
                        thirdParties.add(addedModel)
                    }
                    val isDefaultEnabled = thirdParties.none { it.thirdPartyDefault }
                    withContext(Dispatchers.Main) {
                        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                            thirdParties = thirdParties,
                            enableIsDefault = isDefaultEnabled,
                            selectedThirdParty = addedModel,
                            isLoading = false,
                            warning = Event("ThirdParty saved successfully."),
                            clear = true
                        )
                    }
                } else if (dataModel.message != null) {
                    withContext(Dispatchers.Main) {
                        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                            isLoading = false,
                            warning = Event(dataModel.message),

                            )
                    }
                }
            } else {
                val dataModel = thirdPartyRepository.update(
                    currentThirdParty.thirdPartyId,
                    thirdParty
                )
                if (dataModel.succeed) {
                    val index = manageThirdPartiesState.value.thirdParties.indexOfFirst { it.thirdPartyId == thirdParty.thirdPartyId }
                    if (index >= 0) {
                        manageThirdPartiesState.value.thirdParties.removeAt(index)
                        manageThirdPartiesState.value.thirdParties.add(
                            index,
                            thirdParty
                        )
                    }
                    val isDefaultEnabled = manageThirdPartiesState.value.thirdParties.none { it.thirdPartyDefault }
                    withContext(Dispatchers.Main) {
                        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                            selectedThirdParty = thirdParty,
                            enableIsDefault = isDefaultEnabled,
                            isLoading = false,
                            warning = Event("ThirdParty saved successfully."),
                            clear = true
                        )
                    }
                } else if (dataModel.message != null) {
                    withContext(Dispatchers.Main) {
                        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                            isLoading = false,
                            warning = Event(dataModel.message),

                            )
                    }
                }
            }
        }
    }

    fun deleteSelectedThirdParty() {
        val thirdParty = manageThirdPartiesState.value.selectedThirdParty
        if (thirdParty.thirdPartyId.isEmpty()) {
            manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                warning = Event("Please select an ThirdParty to delete"),
                isLoading = false
            )
            return
        }
        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(thirdParty.thirdPartyId)) {
                withContext(Dispatchers.Main) {
                    manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                        warning = Event("You can't delete this ThirdParty,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            val dataModel = thirdPartyRepository.delete(thirdParty)
            if (dataModel.succeed) {
                val thirdParties = manageThirdPartiesState.value.thirdParties
                thirdParties.remove(thirdParty)
                val isDefaultEnabled = thirdParties.none { it.thirdPartyDefault }
                withContext(Dispatchers.Main) {
                    manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                        thirdParties = thirdParties,
                        selectedThirdParty = ThirdParty(),
                        enableIsDefault = isDefaultEnabled,
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        clear = true
                    )
                }
            } else if (dataModel.message != null) {
                withContext(Dispatchers.Main) {
                    manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                        isLoading = false,
                        warning = Event(dataModel.message),

                        )
                }
            }
        }
    }

    private suspend fun hasRelations(clientID: String): Boolean {
        return invoiceHeaderRepository.getOneInvoiceByClientID(clientID) != null
    }

}