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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManageThirdPartiesViewModel @Inject constructor(
    private val thirdPartyRepository: ThirdPartyRepository,
    private val invoiceHeaderRepository: InvoiceHeaderRepository
) : BaseViewModel() {

    private val _manageThirdPartiesState = MutableStateFlow(ManageThirdPartiesState())
    val manageThirdPartiesState: MutableStateFlow<ManageThirdPartiesState> =
        _manageThirdPartiesState

    private var _thirdPartyState = MutableStateFlow(ThirdParty())
    var thirdPartyState = _thirdPartyState.asStateFlow()

    var currentThirdParty: ThirdParty = ThirdParty()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            val isDefaultEnabled = thirdPartyRepository.getDefaultThirdParty() == null
            withContext(Dispatchers.Main) {
                fillTypes()
                manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                    enableIsDefault = isDefaultEnabled
                )
            }
        }
    }

    fun resetState() {
        currentThirdParty = ThirdParty().copy(thirdPartyType = ThirdPartyType.RECEIVALBE.type)
        updateThirdParty(currentThirdParty.copy())
        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun updateThirdParty(thirdParty: ThirdParty) {
        _thirdPartyState.value = thirdParty
    }


    private fun fillTypes() {
        updateThirdParty(
            thirdPartyState.value.copy(
                thirdPartyType = ThirdPartyType.RECEIVALBE.type
            )
        )
        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
            thirdPartyTypes = Utils.getThirdPartyTypeModels(),
            isLoading = true
        )
    }

    fun fetchThirdParties() {
        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
            warning = null,
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfThirdParties = thirdPartyRepository.getAllThirdParties()
            val isDefaultEnabled = listOfThirdParties.none { it.thirdPartyDefault }
            withContext(Dispatchers.Main) {
                manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                    thirdParties = listOfThirdParties,
                    enableIsDefault = isDefaultEnabled,
                    isLoading = false
                )
            }
        }
    }

    fun save() {
        val thirdParty = thirdPartyState.value
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
                    val thirdParties = manageThirdPartiesState.value.thirdParties
                    if (thirdParties.isNotEmpty()) {
                        thirdParties.add(addedModel)
                    }
                    val isDefaultEnabled = thirdParties.none { it.thirdPartyDefault }
                    withContext(Dispatchers.Main) {
                        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                            thirdParties = thirdParties,
                            enableIsDefault = isDefaultEnabled,
                            isLoading = false,
                            warning = Event("ThirdParty saved successfully."),
                            clear = true
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
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
                        manageThirdPartiesState.value.thirdParties.indexOfFirst { it.thirdPartyId == thirdParty.thirdPartyId }
                    if (index >= 0) {
                        manageThirdPartiesState.value.thirdParties.removeAt(index)
                        manageThirdPartiesState.value.thirdParties.add(
                            index,
                            thirdParty
                        )
                    }
                    val isDefaultEnabled =
                        manageThirdPartiesState.value.thirdParties.none { it.thirdPartyDefault }
                    withContext(Dispatchers.Main) {
                        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                            enableIsDefault = isDefaultEnabled,
                            isLoading = false,
                            warning = Event("ThirdParty saved successfully."),
                            clear = true
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                            isLoading = false,
                            warning = null

                        )
                    }
                }
            }
        }
    }

    fun delete() {
        val thirdParty = thirdPartyState.value
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
                        enableIsDefault = isDefaultEnabled,
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        clear = true
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
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