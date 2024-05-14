package com.grid.pos.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Family.FamilyRepository
import com.grid.pos.data.Item.Item
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.interfaces.OnResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageItemsViewModel @Inject constructor(
        private val itemRepository: ItemRepository,
        private val familyRepository: FamilyRepository
) : ViewModel() {

    private val _manageItemsState = MutableStateFlow(ManageItemsState())
    val manageItemsState: MutableStateFlow<ManageItemsState> = _manageItemsState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchItems()
            fetchFamilies()
        }
    }

    fun fillCachedItems(
            items: MutableList<Item> = mutableListOf(),
            families: MutableList<Family> = mutableListOf()
    ) {
        if (manageItemsState.value.items.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                manageItemsState.value = manageItemsState.value.copy(
                    items = items, families = families
                )
            }
        }
    }

    private suspend fun fetchItems() {
        itemRepository.getAllItems(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfItems = mutableListOf<Item>()
                (result as List<*>).forEach {
                    listOfItems.add(it as Item)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageItemsState.value = manageItemsState.value.copy(
                        items = listOfItems
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

    private suspend fun fetchFamilies() {
        familyRepository.getAllFamilies(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfFamilies = mutableListOf<Family>()
                (result as List<*>).forEach {
                    listOfFamilies.add(it as Family)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageItemsState.value = manageItemsState.value.copy(
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

    fun saveItem(item: Item) {
        if (item.itemName.isNullOrEmpty() || item.itemFaId.isNullOrEmpty()) {
            manageItemsState.value = manageItemsState.value.copy(
                warning = "Please fill item name and family", isLoading = false
            )
            return
        }
        manageItemsState.value = manageItemsState.value.copy(
            isLoading = true
        )
        val isInserting = item.itemDocumentId.isNullOrEmpty()
        val callback = object : OnResult {
            override fun onSuccess(result: Any) {
                viewModelScope.launch(Dispatchers.Main) {
                    val addedModel = result as Item
                    val items = manageItemsState.value.items
                    if (isInserting) items.add(addedModel)
                    manageItemsState.value = manageItemsState.value.copy(
                        items = items, selectedItem = addedModel, isLoading = false, clear = true
                    )
                }
            }

            override fun onFailure(
                    message: String,
                    errorCode: Int
            ) {
                viewModelScope.launch(Dispatchers.Main) {
                    manageItemsState.value = manageItemsState.value.copy(
                        isLoading = false
                    )
                }
            }

        }

        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                item.prepareForInsert()
                itemRepository.insert(item, callback)
            } else {
                itemRepository.update(item, callback)
            }
        }
    }

    fun deleteSelectedItem(item: Item) {
        if (item.itemId.isEmpty()) {
            manageItemsState.value = manageItemsState.value.copy(
                warning = "Please select an Item to delete", isLoading = false
            )
            return
        }
        manageItemsState.value = manageItemsState.value.copy(
            warning = null, isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            itemRepository.delete(item, object : OnResult {
                override fun onSuccess(result: Any) {
                    val items = manageItemsState.value.items
                    val position = items.indexOfFirst {
                        item.itemId.equals(
                            it.itemId, ignoreCase = true
                        )
                    }
                    if (position >= 0) {
                        items.removeAt(position)
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        manageItemsState.value = manageItemsState.value.copy(
                            items = items, selectedItem = Item(), isLoading = false, clear = true
                        )
                    }
                }

                override fun onFailure(
                        message: String,
                        errorCode: Int
                ) {
                    viewModelScope.launch(Dispatchers.Main) {
                        manageItemsState.value = manageItemsState.value.copy(
                            isLoading = false
                        )
                    }
                }

            })
        }
    }

}