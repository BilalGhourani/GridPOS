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

    private val _state = MutableStateFlow(ManageFamiliesState())
    val state: MutableStateFlow<ManageFamiliesState> = _state

    var currentFamily: Family = Family()
    var oldImage: String? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun resetState() {
        currentFamily = Family()
        state.value = state.value.copy(
            family = Family(),
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun updateFamily(family: Family) {
        _state.value = state.value.copy(
            family = family
        )
    }

    fun isAnyChangeDone(): Boolean {
        return state.value.family.didChanged(currentFamily)
    }

    fun fetchFamilies() {
        state.value = state.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfFamilies = familyRepository.getAllFamilies()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
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
            state.value = state.value.copy(
                warning = Event(warning),
                actionLabel = action,
                isLoading = false
            )
        }
    }

    fun save() {
        val family = state.value.family
        if (family.familyName.isNullOrEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please fill family name."),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
            isLoading = true
        )
        val isInserting = family.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                family.prepareForInsert()
                val dataModel = familyRepository.insert(family)
                if (dataModel.succeed) {
                    val addedModel = dataModel.data as Family
                    val families = state.value.families
                    if (families.isNotEmpty()) {
                        families.add(addedModel)
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            families = families,
                            isLoading = false,
                            warning = Event("Family saved successfully."),
                            clear = true,
                        )
                    }
                } else if (dataModel.message != null) {
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false,
                        )
                    }
                }
            } else {
                val dataModel = familyRepository.update(family)
                if (dataModel.succeed) {
                    val index =
                        state.value.families.indexOfFirst { it.familyId == family.familyId }
                    if (index >= 0) {
                        state.value.families.removeAt(index)
                        state.value.families.add(
                            index,
                            family
                        )
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false,
                            warning = Event("Family saved successfully."),
                            clear = true,
                        )
                    }
                } else if (dataModel.message != null) {
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    fun delete() {
        val family = state.value.family
        if (family.familyId.isEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please select an family to delete"),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(family.familyId)) {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        warning = Event("You can't delete this Family ,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            val dataModel = familyRepository.delete(family)
            if (dataModel.succeed) {
                val families = state.value.families
                families.remove(family)
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        families = families,
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        clear = true
                    )
                }
            } else if (dataModel.message != null) {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
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