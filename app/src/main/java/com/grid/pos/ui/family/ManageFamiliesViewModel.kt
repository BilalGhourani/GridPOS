package com.grid.pos.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Family.FamilyRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageFamiliesViewModel @Inject constructor(
        private val familyRepository: FamilyRepository
) : ViewModel() {

    private val _manageFamiliesState = MutableStateFlow(ManageFamiliesState())
    val manageFamiliesState: MutableStateFlow<ManageFamiliesState> = _manageFamiliesState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchFamilies()
        }
    }

    fun fillCachedFamilies(families: MutableList<Family> = mutableListOf()) {
        if (manageFamiliesState.value.families.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                manageFamiliesState.value = manageFamiliesState.value.copy(
                    families = families
                )
            }
        }
    }

    private suspend fun fetchFamilies() {
        familyRepository.getAllFamilies(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfFamilies = mutableListOf<Family>()
                (result as List<*>).forEach {
                    listOfFamilies.add(it as Family)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        families = listOfFamilies
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
            manageFamiliesState.value = manageFamiliesState.value.copy(
                warning = Event(warning),
                actionLabel = action,
                isLoading = false
            )
        }
    }

    fun saveFamily() {
        val family = manageFamiliesState.value.selectedFamily
        if (family.familyName.isNullOrEmpty()) {
            manageFamiliesState.value = manageFamiliesState.value.copy(
                warning = Event("Please fill family name."),
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
                        selectedFamily = Family(),
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
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        isLoading = false
                    )
                }
            }

        }
        manageFamiliesState.value.selectedFamily.let {
            CoroutineScope(Dispatchers.IO).launch {
                if (isInserting) {
                    it.prepareForInsert()
                    familyRepository.insert(
                        it,
                        callback
                    )
                } else {
                    familyRepository.update(
                        it,
                        callback
                    )
                }
            }
        }
    }

    fun deleteSelectedFamily() {
        val family = manageFamiliesState.value.selectedFamily
        if (family.familyId.isEmpty()) {
            manageFamiliesState.value = manageFamiliesState.value.copy(
                warning = Event("Please select an family to delete"),
                isLoading = false
            )
            return
        }
        manageFamiliesState.value = manageFamiliesState.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            familyRepository.delete(family,
                object : OnResult {
                    override fun onSuccess(result: Any) {
                        val families = manageFamiliesState.value.families
                        val position = families.indexOfFirst {
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
                            manageFamiliesState.value = manageFamiliesState.value.copy(
                                isLoading = false
                            )
                        }
                    }

                })
        }
    }
}