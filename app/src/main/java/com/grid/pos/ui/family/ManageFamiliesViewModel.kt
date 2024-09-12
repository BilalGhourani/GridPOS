package com.grid.pos.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Family.FamilyRepository
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManageFamiliesViewModel @Inject constructor(
        private val familyRepository: FamilyRepository,
        private val itemRepository: ItemRepository
) : BaseViewModel() {

    private val _manageFamiliesState = MutableStateFlow(ManageFamiliesState())
    val manageFamiliesState: MutableStateFlow<ManageFamiliesState> = _manageFamiliesState
    var currentFamily: Family? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
           openConnectionIfNeeded()
        }
    }

    fun fillCachedFamilies(families: MutableList<Family> = mutableListOf()) {
        if (manageFamiliesState.value.families.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                manageFamiliesState.value = manageFamiliesState.value.copy(
                    families = families.toMutableList()
                )
            }
        }
    }

    fun fetchFamilies() {
        manageFamiliesState.value = manageFamiliesState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfFamilies = familyRepository.getAllFamilies()
            withContext(Dispatchers.Main) {
                manageFamiliesState.value = manageFamiliesState.value.copy(
                    families = listOfFamilies,
                    isLoading = false
                )
            }
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
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                family.prepareForInsert()
                val addedModel = familyRepository.insert(family)
                val families = manageFamiliesState.value.families
                if(families.isNotEmpty()) {
                    families.add(addedModel)
                }
                currentFamily = null
                withContext(Dispatchers.Main) {
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        families = families,
                        selectedFamily = Family(),
                        isLoading = false,
                        warning = Event("Family saved successfully."),
                        clear = true,
                    )
                }
            } else {
                familyRepository.update(family)
                currentFamily = null
                withContext(Dispatchers.Main) {
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        selectedFamily = Family(),
                        isLoading = false,
                        warning = Event("Family saved successfully."),
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
            if (hasRelations(family.familyId)) {
                withContext(Dispatchers.Main) {
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        warning = Event("You can't delete this Family ,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            familyRepository.delete(family)
            val families = manageFamiliesState.value.families
            families.remove(family)
            currentFamily = null
            withContext(Dispatchers.Main) {
                manageFamiliesState.value = manageFamiliesState.value.copy(
                    families = families,
                    selectedFamily = Family(),
                    isLoading = false,
                    warning = Event("successfully deleted."),
                    clear = true
                )
            }
        }
    }

    private suspend fun hasRelations(familyId: String): Boolean {
        if (itemRepository.getOneItemByFamily(familyId) != null) return true

        return false
    }
}