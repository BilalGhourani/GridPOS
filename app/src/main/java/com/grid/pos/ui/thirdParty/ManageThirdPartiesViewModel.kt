package com.grid.pos.ui.thirdParty

import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.thirdParty.ThirdPartyRepository
import com.grid.pos.model.PopupModel
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
    private val invoiceHeaderRepository: InvoiceHeaderRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

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
        state.value = state.value.copy(
            thirdParty = currentThirdParty.copy()
        )
    }

    fun updateThirdParty(thirdParty: ThirdParty) {
        state.value = state.value.copy(
            thirdParty = thirdParty
        )
    }

    fun checkChanges(callback: () -> Unit) {
        if (isLoading()) {
            return
        }
        if (state.value.thirdParty.didChanged(currentThirdParty)) {
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
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val listOfThirdParties = thirdPartyRepository.getAllThirdParties()
            val isDefaultEnabled = listOfThirdParties.none { it.thirdPartyDefault }
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    thirdParties = listOfThirdParties,
                    enableIsDefault = isDefaultEnabled
                )
                showLoading(false)
            }
        }
    }

    fun save(callback: () -> Unit = {}) {
        val thirdParty = state.value.thirdParty
        if (thirdParty.thirdPartyName.isNullOrEmpty()) {
            showWarning("Please fill ThirdParty name.")
            return
        }
        showLoading(true)
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
                    if (sharedViewModel.needAddedData) {
                        sharedViewModel.needAddedData = false
                        sharedViewModel.fetchThirdPartiesAgain = true
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            thirdParties = thirdParties,
                            enableIsDefault = isDefaultEnabled
                        )
                        resetState()
                        showLoading(false)
                        showWarning("ThirdParty saved successfully.")
                        callback.invoke()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            } else {
                val dataModel = thirdPartyRepository.update(
                    currentThirdParty.thirdPartyId,
                    thirdParty
                )
                if (dataModel.succeed) {
                    val thirdParties = state.value.thirdParties.toMutableList()
                    val index =
                        thirdParties.indexOfFirst { it.thirdPartyId == thirdParty.thirdPartyId }
                    if (index >= 0) {
                        thirdParties.removeAt(index)
                        thirdParties.add(index, thirdParty)
                    }
                    val isDefaultEnabled = thirdParties.none { it.thirdPartyDefault }
                    if (sharedViewModel.needAddedData) {
                        sharedViewModel.needAddedData = false
                        sharedViewModel.fetchThirdPartiesAgain = true
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            thirdParties = thirdParties,
                            enableIsDefault = isDefaultEnabled,
                        )
                        resetState()
                        showLoading(false)
                        showWarning("ThirdParty saved successfully.")
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
        val thirdParty = state.value.thirdParty
        if (thirdParty.thirdPartyId.isEmpty()) {
            showWarning("Please select an ThirdParty to delete")
            return
        }
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(thirdParty.thirdPartyId)) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showWarning("You can't delete this ThirdParty,because it has related data!")
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
                        enableIsDefault = isDefaultEnabled
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

    private suspend fun hasRelations(clientID: String): Boolean {
        return invoiceHeaderRepository.getOneInvoiceByClientID(clientID) != null
    }

}