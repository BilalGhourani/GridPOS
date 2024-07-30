package com.grid.pos.ui.thirdParty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.data.ThirdParty.ThirdPartyRepository
import com.grid.pos.model.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageThirdPartiesViewModel @Inject constructor(
        private val thirdPartyRepository: ThirdPartyRepository
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
        viewModelScope.launch(Dispatchers.Main) {
            manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                thirdParties = listOfThirdParties
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
                    viewModelScope.launch(Dispatchers.Main) {
                        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                            thirdParties = thirdParties,
                            selectedThirdParty = addedModel,
                            isLoading = false,
                            warning = Event("Third Party saved successfully."),
                            clear = true
                        )
                    }
                } else {
                    thirdPartyRepository.update(thirdParty)
                    viewModelScope.launch(Dispatchers.Main) {
                        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                            selectedThirdParty = thirdParty,
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
            thirdPartyRepository.delete(thirdParty)
            val thirdParties = manageThirdPartiesState.value.thirdParties
            thirdParties.remove(thirdParty)
            viewModelScope.launch(Dispatchers.Main) {
                manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                    thirdParties = thirdParties,
                    selectedThirdParty = ThirdParty(),
                    isLoading = false,
                    warning = Event("successfully deleted."),
                    clear = true
                )
            }
        }
    }

}