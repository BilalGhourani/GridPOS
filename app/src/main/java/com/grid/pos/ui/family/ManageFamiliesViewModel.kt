package com.grid.pos.ui.family

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.family.Family
import com.grid.pos.data.family.FamilyRepository
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.model.Event
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private var _familyState = MutableStateFlow(Family())
    var familyState = _familyState.asStateFlow()

    var currentFamily: Family = Family()
    var oldImage: String? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun resetState() {
        currentFamily = Family()
        updateFamily(Family())
        manageFamiliesState.value = manageFamiliesState.value.copy(
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun updateFamily(family: Family) {
        _familyState.value = family
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

    fun save() {
        if (familyState.value.familyName.isNullOrEmpty()) {
            manageFamiliesState.value = manageFamiliesState.value.copy(
                warning = Event("Please fill family name."),
                isLoading = false
            )
            return
        }
        manageFamiliesState.value = manageFamiliesState.value.copy(
            isLoading = true
        )
        val isInserting = familyState.value.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                familyState.value.prepareForInsert()
                val dataModel = familyRepository.insert(familyState.value)
                if (dataModel.succeed) {
                    val addedModel = dataModel.data as Family
                    val families = manageFamiliesState.value.families
                    if (families.isNotEmpty()) {
                        families.add(addedModel)
                    }
                    withContext(Dispatchers.Main) {
                        manageFamiliesState.value = manageFamiliesState.value.copy(
                            families = families,
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
                val dataModel = familyRepository.update(familyState.value)
                if (dataModel.succeed) {
                    val index =
                        manageFamiliesState.value.families.indexOfFirst { it.familyId == familyState.value.familyId }
                    if (index >= 0) {
                        manageFamiliesState.value.families.removeAt(index)
                        manageFamiliesState.value.families.add(
                            index,
                            familyState.value
                        )
                    }
                    withContext(Dispatchers.Main) {
                        manageFamiliesState.value = manageFamiliesState.value.copy(
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

    fun delete() {
        if (familyState.value.familyId.isEmpty()) {
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
            if (hasRelations(familyState.value.familyId)) {
                withContext(Dispatchers.Main) {
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        warning = Event("You can't delete this Family ,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            val dataModel = familyRepository.delete(familyState.value)
            if (dataModel.succeed) {
                val families = manageFamiliesState.value.families
                families.remove(familyState.value)
                withContext(Dispatchers.Main) {
                    manageFamiliesState.value = manageFamiliesState.value.copy(
                        families = families,
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

    fun copyToInternalStorage(context: Context, uri: Uri, callback: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val internalPath =
                FileUtils.saveToExternalStorage(
                    context = context,
                    parent = "family",
                    uri,
                    (familyState.value.familyName ?: "family").trim()
                        .replace(
                            " ",
                            "_"
                        )
                )
            withContext(Dispatchers.Main) {
                callback.invoke(internalPath)
            }
        }
    }
}