package com.grid.pos.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Family.FamilyRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageFamiliesViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    private val _manageFamiliesState = MutableStateFlow(ManageFamiliesState())
    val manageFamiliesState: MutableStateFlow<ManageFamiliesState> = _manageFamiliesState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchFamilies()
            fetchCompanies()
        }
    }

    private fun fetchFamilies() {
        familyRepository.getAllFamilies(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfFamilies = mutableListOf<Family>()
                (result as List<Family>).forEach {
                    listOfFamilies.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        families = listOfFamilies
                    )
                }
            }

            override fun onFailure(message: String) {

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
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        companies = listOfCompanies
                    )
                }
            }

            override fun onFailure(message: String) {

            }

        })
    }

    fun saveFamily() {
        val family = manageFamiliesState.value.selectedFamily
        if (family.familyName.isNullOrEmpty() || family.familyCompanyId.isNullOrEmpty()) {
            manageFamiliesState.value = manageFamiliesState.value.copy(
                warning = "Please fill all inputs",
                isLoading = false
            )
            return
        }
        manageFamiliesState.value = manageFamiliesState.value.copy(
            isLoading = true
        )
        val isInserting = family.familyDocumentId.isNullOrEmpty()
        val callback = object : OnResult {
            override fun onSuccess(result: Any) {
                viewModelScope.launch(Dispatchers.Main) {
                    val addedModel = result as Family
                    val families = manageFamiliesState.value.families
                    if (isInserting) {
                        families.add(addedModel)
                    }
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        families = families,
                        selectedFamily = addedModel,
                        isLoading = false
                    )
                }
            }

            override fun onFailure(message: String) {
                viewModelScope.launch(Dispatchers.Main) {
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        isLoading = false
                    )
                }
            }

        }
        manageFamiliesState.value.selectedFamily.let {
            CoroutineScope(Dispatchers.IO).launch {
                if (isInserting) {
                    it.familyId = Utils.generateRandomUuidString()
                    familyRepository.insert(it, callback)
                } else {
                    familyRepository.update(it, callback)
                }
            }
        }
    }

    fun deleteSelectedFamily() {
        val family = manageFamiliesState.value.selectedFamily
        if (family.familyId.isEmpty()) {
            manageFamiliesState.value = manageFamiliesState.value.copy(
                warning = "Please select an user to delete",
                isLoading = false
            )
            return
        }
        manageFamiliesState.value = manageFamiliesState.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            familyRepository.delete(family, object : OnResult {
                override fun onSuccess(result: Any) {
                    val families = manageFamiliesState.value.families
                    val position =
                        families.indexOfFirst {
                            family.familyId.equals(
                                it.familyId,
                                ignoreCase = true
                            )
                        }
                    if (position >= 0) {
                        families.removeAt(position)
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        manageFamiliesState.value = manageFamiliesState.value.copy(
                            families = families,
                            selectedFamily = Family(),
                            isLoading = false
                        )
                    }
                }

                override fun onFailure(message: String) {
                    viewModelScope.launch(Dispatchers.Main) {
                        manageFamiliesState.value = manageFamiliesState.value.copy(
                            isLoading = false
                        )
                    }
                }

            })
        }
    }

}