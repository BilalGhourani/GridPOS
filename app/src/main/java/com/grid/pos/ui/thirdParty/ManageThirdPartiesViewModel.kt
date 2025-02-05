package com.grid.pos.ui.thirdParty

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.thirdParty.ThirdPartyRepository
import com.grid.pos.model.Event
import com.grid.pos.model.ThirdPartyType
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.Utils
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

    private val _state = MutableStateFlow(ManageThirdPartiesState())
    val state: MutableStateFlow<ManageThirdPartiesState> = _state

    var currentThirdParty: ThirdParty = ThirdParty()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            val isDefaultEnabled = thirdPartyRepository.getDefaultThirdParty() == null
            withContext(Dispatchers.Main) {
                fillTypes()
                state.value = state.value.copy(
                    enableIsDefault = isDefaultEnabled
                )
            }
        }
    }

    fun resetState() {
        currentThirdParty = ThirdParty().copy(thirdPartyType = ThirdPartyType.RECEIVALBE.type)
        updateThirdParty(currentThirdParty.copy())
        state.value = state.value.copy(
            thirdParty = ThirdParty(),
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun updateThirdParty(thirdParty: ThirdParty) {
        state.value = state.value.copy(
            thirdParty = thirdParty
        )
    }

    fun isAnyChangeDone(): Boolean {
        return state.value.thirdParty.didChanged(currentThirdParty)
    }

    private fun fillTypes() {
        updateThirdParty(
            state.value.thirdParty.copy(
                thirdPartyType = ThirdPartyType.RECEIVALBE.type
            )
        )
        state.value = state.value.copy(
            thirdPartyTypes = Utils.getThirdPartyTypeModels(),
        )
    }

    fun fetchThirdParties() {
        state.value = state.value.copy(
            warning = null,
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfThirdParties = thirdPartyRepository.getAllThirdParties()
            val isDefaultEnabled = listOfThirdParties.none { it.thirdPartyDefault }
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    thirdParties = listOfThirdParties,
                    enableIsDefault = isDefaultEnabled,
                    isLoading = false
                )
            }
        }
    }

    fun save() {
        val thirdParty = state.value.thirdParty
        if (thirdParty.thirdPartyName.isNullOrEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please fill ThirdParty name."),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
            isLoading = true
        )
        if (thirdParty.thirdPartyType.isNullOrEmpty()) {
            thirdParty.thirdPartyType = ThirdPartyType.RECEIVALBE.type
        }
        val isInserting = thirdParty.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                thirdParty.prepareForInsert()
                val dataModel = thirdPartyRepository.insert(thirdParty)
                if (dataModel.succeed) {
                    val addedModel = dataModel.data as ThirdParty
                    val thirdParties = state.value.thirdParties
                    if (thirdParties.isNotEmpty()) {
                        thirdParties.add(addedModel)
                    }
                    val isDefaultEnabled = thirdParties.none { it.thirdPartyDefault }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            thirdParties = thirdParties,
                            enableIsDefault = isDefaultEnabled,
                            isLoading = false,
                            warning = Event("ThirdParty saved successfully."),
                            clear = true
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false,
                            warning = null

                        )
                    }
                }
            } else {
                val dataModel = thirdPartyRepository.update(
                    currentThirdParty.thirdPartyId,
                    thirdParty
                )
                if (dataModel.succeed) {
                    val index =
                        state.value.thirdParties.indexOfFirst { it.thirdPartyId == thirdParty.thirdPartyId }
                    if (index >= 0) {
                        state.value.thirdParties.removeAt(index)
                        state.value.thirdParties.add(
                            index,
                            thirdParty
                        )
                    }
                    val isDefaultEnabled =
                        state.value.thirdParties.none { it.thirdPartyDefault }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            enableIsDefault = isDefaultEnabled,
                            isLoading = false,
                            warning = Event("ThirdParty saved successfully."),
                            clear = true
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false,
                            warning = null

                        )
                    }
                }
            }
        }
    }

    fun delete() {
        val thirdParty = state.value.thirdParty
        if (thirdParty.thirdPartyId.isEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please select an ThirdParty to delete"),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(thirdParty.thirdPartyId)) {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        warning = Event("You can't delete this ThirdParty,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            val dataModel = thirdPartyRepository.delete(thirdParty)
            if (dataModel.succeed) {
                val thirdParties = state.value.thirdParties
                thirdParties.remove(thirdParty)
                val isDefaultEnabled = thirdParties.none { it.thirdPartyDefault }
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        thirdParties = thirdParties,
                        enableIsDefault = isDefaultEnabled,
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        clear = true
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        isLoading = false,
                        warning = null
                    )
                }
            }
        }
    }

    private suspend fun hasRelations(clientID: String): Boolean {
        return invoiceHeaderRepository.getOneInvoiceByClientID(clientID) != null
    }

}