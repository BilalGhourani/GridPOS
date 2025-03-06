package com.grid.pos.ui.family

import android.content.Context
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.family.Family
import com.grid.pos.data.family.FamilyRepository
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.PopupModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManageFamiliesViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val itemRepository: ItemRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

    val state = mutableStateOf(ManageFamiliesState())

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
            family = Family()
        )
    }

    fun updateFamily(family: Family) {
        state.value = state.value.copy(
            family = family
        )
    }

    fun checkChanges(context: Context, callback: () -> Unit) {
        if (state.value.family.didChanged(currentFamily)) {
            sharedViewModel.showPopup(true,
                PopupModel().apply {
                    onDismissRequest = {
                        resetState()
                        callback.invoke()
                    }
                    onConfirmation = {
                        save(context) {
                            checkChanges(context, callback)
                        }
                    }
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                    cancelable = false
                })
        } else {
            callback.invoke()
        }
    }

    fun fetchFamilies() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val listOfFamilies = familyRepository.getAllFamilies()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    families = listOfFamilies
                )
                showLoading(false)
            }
        }
    }

    fun save(context: Context, callback: () -> Unit = {}) {
        val family = state.value.family
        if (family.familyName.isNullOrEmpty()) {
            showWarning("Please fill family name.")
            return
        }
        showLoading(true)
        val isInserting = family.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            oldImage?.let { old ->
                FileUtils.deleteFile(
                    context,
                    old
                )
            }
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
                            families = families
                        )
                        resetState()
                        showLoading(false)
                        showWarning("Family saved successfully.")
                        callback.invoke()
                    }
                } else if (dataModel.message != null) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            } else {
                val dataModel = familyRepository.update(family)
                if (dataModel.succeed) {
                    val families = state.value.families.toMutableList()
                    val index = families.indexOfFirst { it.familyId == family.familyId }
                    if (index >= 0) {
                        families.removeAt(index)
                        families.add(index, family)
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            families = families
                        )
                        resetState()
                        showLoading(false)
                        showWarning("Family saved successfully.")
                        callback.invoke()
                    }
                } else if (dataModel.message != null) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            }
        }
    }

    fun delete() {
        val family = state.value.family
        if (family.familyId.isEmpty()) {
            showWarning("Please select an family to delete")
            return
        }
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(family.familyId)) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showWarning("You can't delete this Family ,because it has related data!")
                }
                return@launch
            }
            val dataModel = familyRepository.delete(family)
            if (dataModel.succeed) {
                val families = state.value.families
                families.remove(family)
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        families = families
                    )
                    resetState()
                    showLoading(false)
                    showWarning("successfully deleted.")
                }
            } else if (dataModel.message != null) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
    }

    private suspend fun hasRelations(familyId: String): Boolean {
        return itemRepository.getOneItemByFamily(familyId) != null
    }

    fun launchGalleryPicker(context: Context) {
        sharedViewModel.launchGalleryPicker(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
            object : OnGalleryResult {
                override fun onGalleryResult(uris: List<Uri>) {
                    if (uris.isNotEmpty()) {
                        sharedViewModel.copyToInternalStorage(
                            context,
                            uris[0],
                            parent = "family",
                            fileName = (state.value.family.familyName
                                ?: "family").trim().replace(" ", "_")
                        ) { internalPath ->
                            if (internalPath != null) {
                                oldImage =
                                    state.value.family.familyImage
                                updateFamily(
                                    state.value.family.copy(
                                        familyImage = internalPath
                                    )
                                )
                            }
                        }
                    }
                }

            },
            onPermissionDenied = {
                showWarning(
                    "Permission Denied",
                    "Settings"
                ) {
                    sharedViewModel.openAppStorageSettings()
                }
            })
    }
}