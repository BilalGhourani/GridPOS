package com.grid.pos.ui.family

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.family.Family
import com.grid.pos.data.family.FamilyRepository
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.model.Event
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
    var currentFamily: Family = Family()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun resetState() {
        manageFamiliesState.value = manageFamiliesState.value.copy(
            warning = null,
            isLoading = false,
            clear = false
        )
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
            action: String? = null
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
                val dataModel = familyRepository.insert(family)
                if (dataModel.succeed) {
                    val addedModel = dataModel.data as Family
                    val families = manageFamiliesState.value.families
                    if (families.isNotEmpty()) {
                        families.add(addedModel)
                    }
                    withContext(Dispatchers.Main) {
                        manageFamiliesState.value = manageFamiliesState.value.copy(
                            families = families,
                            selectedFamily = Family(),
                            isLoading = false,
                            warning = Event("Family saved successfully."),
                            clear = true,
                        )
                    }
                } else if (dataModel.message != null) {
                    withContext(Dispatchers.Main) {
                        manageFamiliesState.value = manageFamiliesState.value.copy(
                            isLoading = false,
                        )
                    }
                }
            } else {
                val dataModel = familyRepository.update(family)
                if (dataModel.succeed) {
                    val index = manageFamiliesState.value.families.indexOfFirst { it.familyId == family.familyId }
                    if (index >= 0) {
                        manageFamiliesState.value.families.removeAt(index)
                        manageFamiliesState.value.families.add(
                            index,
                            family
                        )
                    }
                    withContext(Dispatchers.Main) {
                        manageFamiliesState.value = manageFamiliesState.value.copy(
                            selectedFamily = Family(),
                            isLoading = false,
                            warning = Event("Family saved successfully."),
                            clear = true,
                        )
                    }
                } else if (dataModel.message != null) {
                    withContext(Dispatchers.Main) {
                        manageFamiliesState.value = manageFamiliesState.value.copy(
                            isLoading = false,
                        )
                    }
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
            val dataModel = familyRepository.delete(family)
            if (dataModel.succeed) {
                val families = manageFamiliesState.value.families
                families.remove(family)
                withContext(Dispatchers.Main) {
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        families = families,
                        selectedFamily = Family(),
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        clear = true
                    )
                }
            } else if (dataModel.message != null) {
                withContext(Dispatchers.Main) {
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        isLoading = false,
                    )
                }
            }
        }
    }

    private suspend fun hasRelations(familyId: String): Boolean {
        return itemRepository.getOneItemByFamily(familyId) != null
    }
}