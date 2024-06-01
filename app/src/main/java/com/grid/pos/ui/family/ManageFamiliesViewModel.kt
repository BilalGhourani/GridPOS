package com.grid.pos.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Family.FamilyRepository
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
        val listOfFamilies = familyRepository.getAllFamilies()
        viewModelScope.launch(Dispatchers.Main) {
            manageFamiliesState.value = manageFamiliesState.value.copy(
                families = listOfFamilies
            )
        }
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

    fun saveFamily(family: Family) {
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
        val isInserting = family.isNew()
        manageFamiliesState.value.selectedFamily.let {
            CoroutineScope(Dispatchers.IO).launch {
                if (isInserting) {
                    it.prepareForInsert()
                    val addedModel = familyRepository.insert(it)
                    val families = manageFamiliesState.value.families
                    families.add(addedModel)
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        families = families,
                        selectedFamily = Family(),
                        isLoading = false,
                        clear = true,
                    )
                } else {
                    familyRepository.update(it)
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        selectedFamily = Family(),
                        isLoading = false,
                        clear = true,
                    )
                }
            }
        }
    }

    fun deleteSelectedFamily(family: Family) {
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
            familyRepository.delete(family)
            val families = manageFamiliesState.value.families
            families.remove(family)
            viewModelScope.launch(Dispatchers.Main) {
                manageFamiliesState.value = manageFamiliesState.value.copy(
                    families = families,
                    selectedFamily = Family(),
                    isLoading = false,
                    clear = true
                )
            }
        }
    }
}