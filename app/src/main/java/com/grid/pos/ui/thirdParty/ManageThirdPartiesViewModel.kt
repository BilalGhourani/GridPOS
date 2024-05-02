package com.grid.pos.ui.thirdParty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.Family.Family
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.data.ThirdParty.ThirdPartyRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageThirdPartiesViewModel @Inject constructor(
    private val thirdPartyRepository: ThirdPartyRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    private val _manageThirdPartiesState = MutableStateFlow(ManageThirdPartiesState())
    val manageThirdPartiesState: MutableStateFlow<ManageThirdPartiesState> =
        _manageThirdPartiesState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchThirdParties()
            fetchCompanies()
        }
    }

    private suspend fun fetchThirdParties() {
        thirdPartyRepository.getAllThirdParties(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfThirdParties = mutableListOf<ThirdParty>()
                (result as List<ThirdParty>).forEach {
                    listOfThirdParties.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                        thirdParties = listOfThirdParties
                    )
                }
            }

            override fun onFailure(message: String, errorCode: Int) {

            }

        })
    }

    private suspend fun fetchCompanies() {
        companyRepository.getAllCompanies(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfCompanies = mutableListOf<Company>()
                (result as List<Company>).forEach {
                    listOfCompanies.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                        companies = listOfCompanies
                    )
                }
            }

            override fun onFailure(message: String, errorCode: Int) {

            }

        })
    }

    fun saveThirdParty() {
        val thirdParty = manageThirdPartiesState.value.selectedThirdParty
        if (thirdParty.thirdPartyName.isNullOrEmpty()) {
            manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                warning = "Please fill all inputs",
                isLoading = false
            )
            return
        }
        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
            isLoading = true
        )
        val isInserting = thirdParty.thirdPartyDocumentId.isNullOrEmpty()
        val callback = object : OnResult {
            override fun onSuccess(result: Any) {
                viewModelScope.launch(Dispatchers.Main) {
                    val addedModel = result as ThirdParty
                    val thirdParties = manageThirdPartiesState.value.thirdParties
                    if (isInserting) thirdParties.add(addedModel)
                    manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                        thirdParties = thirdParties,
                        selectedThirdParty = addedModel,
                        isLoading = false,
                        clear = true
                    )
                }
            }

            override fun onFailure(message: String, errorCode: Int) {
                viewModelScope.launch(Dispatchers.Main) {
                    manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                        isLoading = false
                    )
                }
            }

        }
        manageThirdPartiesState.value.selectedThirdParty.let {
            CoroutineScope(Dispatchers.IO).launch {
                if (isInserting) {
                    it.thirdPartyId = Utils.generateRandomUuidString()
                    thirdPartyRepository.insert(it, callback)
                } else {
                    thirdPartyRepository.update(it, callback)
                }
            }
        }
    }

    fun deleteSelectedThirdParty() {
        val thirdParty = manageThirdPartiesState.value.selectedThirdParty
        if (thirdParty.thirdPartyId.isEmpty()) {
            manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                warning = "Please select an third party to delete",
                isLoading = false
            )
            return
        }
        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            thirdPartyRepository.delete(thirdParty, object : OnResult {
                override fun onSuccess(result: Any) {
                    val thirdParties = manageThirdPartiesState.value.thirdParties
                    val position =
                        thirdParties.indexOfFirst {
                            thirdParty.thirdPartyId.equals(
                                it.thirdPartyId,
                                ignoreCase = true
                            )
                        }
                    if (position >= 0) {
                        thirdParties.removeAt(position)
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                            thirdParties = thirdParties,
                            selectedThirdParty = ThirdParty(),
                            isLoading = false,
                            clear = true
                        )
                    }
                }

                override fun onFailure(message: String, errorCode: Int) {
                    viewModelScope.launch(Dispatchers.Main) {
                        manageThirdPartiesState.value = manageThirdPartiesState.value.copy(
                            isLoading = false
                        )
                    }
                }

            })
        }
    }

}