package com.grid.pos.ui.thirdParty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.data.ThirdParty.ThirdPartyRepository
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
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
) : ViewModel() {

    private val _manageThirdPartiesState = MutableStateFlow(ManageThirdPartiesState())
    val manageThirdPartiesState: MutableStateFlow<ManageThirdPartiesState> = _manageThirdPartiesState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchThirdParties()
        }
    }

    fun fillCachedThirdParties(thirdParties: MutableList<ThirdParty> = mutableListOf()) {
        if (manageThirdPartiesState.value.thirdParties.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                    thirdParties = thirdParties
                )
            }
        }
    }

    private suspend fun fetchThirdParties() {
        val listOfThirdParties = thirdPartyRepository.getAllThirdParties()
        val isDefaultEnabled = listOfThirdParties.none { it.thirdPartyDefault }
        viewModelScope.launch(Dispatchers.Main) {
            manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                thirdParties = listOfThirdParties,
                enableIsDefault = isDefaultEnabled
            )
        }
    }

    fun saveThirdParty(thirdParty: ThirdParty) {
        if (thirdParty.thirdPartyName.isNullOrEmpty()) {
            manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                warning = Event("Please fill Third Party name."),
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
                val addedModel = thirdPartyRepository.insert(thirdParty)
                val thirdParties = manageThirdPartiesState.value.thirdParties
                thirdParties.add(addedModel)
                val isDefaultEnabled = thirdParties.none { it.thirdPartyDefault }
                viewModelScope.launch(Dispatchers.Main) {
                    manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                        thirdParties = thirdParties,
                        enableIsDefault = isDefaultEnabled,
                        selectedThirdParty = addedModel,
                        isLoading = false,
                        warning = Event("Third Party saved successfully."),
                        clear = true
                    )
                }
            } else {
                thirdPartyRepository.update(thirdParty)
                val isDefaultEnabled = manageThirdPartiesState.value.thirdParties.none { it.thirdPartyDefault }
                viewModelScope.launch(Dispatchers.Main) {
                    manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                        selectedThirdParty = thirdParty,
                        enableIsDefault = isDefaultEnabled,
                        isLoading = false,
                        warning = Event("Third Party saved successfully."),
                        clear = true
                    )
                }
            }
        }
    }

    fun deleteSelectedThirdParty() {
        val thirdParty = manageThirdPartiesState.value.selectedThirdParty
        if (thirdParty.thirdPartyId.isEmpty()) {
            manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                warning = Event("Please select an third party to delete"),
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
                        warning = Event("You can't delete this Third Party,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            thirdPartyRepository.delete(thirdParty)
            val thirdParties = manageThirdPartiesState.value.thirdParties
            thirdParties.remove(thirdParty)
            val isDefaultEnabled = thirdParties.none { it.thirdPartyDefault }
            viewModelScope.launch(Dispatchers.Main) {
                manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                    thirdParties = thirdParties,
                    selectedThirdParty = ThirdParty(),
                    enableIsDefault = isDefaultEnabled,
                    isLoading = false,
                    warning = Event("successfully deleted."),
                    clear = true
                )
            }
        }
    }

    private suspend fun hasRelations(clientID: String): Boolean {
        if (invoiceHeaderRepository.getOneInvoiceByClientID(clientID) != null)
            return true

        return false
    }

}