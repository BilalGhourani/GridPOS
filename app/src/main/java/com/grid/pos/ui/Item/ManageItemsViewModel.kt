package com.grid.pos.ui.Item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Currency.CurrencyRepository
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Family.FamilyRepository
import com.grid.pos.data.Item.Item
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.data.PosPrinter.PosPrinterRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageItemsViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val companyRepository: CompanyRepository,
    private val familyRepository: FamilyRepository,
    private val posPrinterRepository: PosPrinterRepository
) : ViewModel() {

    private val _manageItemsState = MutableStateFlow(ManageItemsState())
    val manageItemsState: MutableStateFlow<ManageItemsState> = _manageItemsState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchItems()
            fetchCompanies()
            fetchFamilies()
            fetchPrintes()
        }
    }

    private suspend fun fetchItems() {
        itemRepository.getAllItems(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfItems = mutableListOf<Item>()
                (result as List<Item>).forEach {
                    listOfItems.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageItemsState.value = manageItemsState.value.copy(
                        items = listOfItems
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
                    manageItemsState.value = manageItemsState.value.copy(
                        companies = listOfCompanies
                    )
                }
            }

            override fun onFailure(message: String) {

            }

        })
    }

    private suspend fun fetchPrintes() {
        posPrinterRepository.getAllPosPrinters(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfPrinters = mutableListOf<PosPrinter>()
                (result as List<PosPrinter>).forEach {
                    listOfPrinters.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageItemsState.value = manageItemsState.value.copy(
                        printers = listOfPrinters
                    )
                }
            }

            override fun onFailure(message: String) {

            }

        })
    }

    private fun fetchFamilies() {
        familyRepository.getAllFamilies(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfFamilies = mutableListOf<Family>()
                (result as List<Family>).forEach {
                    listOfFamilies.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageItemsState.value = manageItemsState.value.copy(
                        families = listOfFamilies
                    )
                }
            }

            override fun onFailure(message: String) {

            }

        })
    }

    fun saveItem(item: Item) {
        if (item.itemName.isNullOrEmpty() || item.itemBarcode.isNullOrEmpty()) {
            manageItemsState.value = manageItemsState.value.copy(
                warning = "Please fill all inputs",
                isLoading = false
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
                        items = items,
                        selectedItem = addedModel,
                        isLoading = false,
                        clear = true
                    )
                }
            }

            override fun onFailure(message: String) {
                viewModelScope.launch(Dispatchers.Main) {
                    manageItemsState.value = manageItemsState.value.copy(
                        isLoading = false
                    )
                }
            }

        }

        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                item.itemId = Utils.generateRandomUuidString()
                itemRepository.insert(item, callback)
            } else {
                itemRepository.update(item, callback)
            }
        }
    }

    fun deleteSelectedItem(item: Item) {
        if (item.itemDocumentId.isNullOrEmpty()) {
            manageItemsState.value = manageItemsState.value.copy(
                warning = "Please select an Item to delete",
                isLoading = false
            )
            return
        }
        manageItemsState.value = manageItemsState.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            itemRepository.delete(item, object : OnResult {
                override fun onSuccess(result: Any) {
                    val items = manageItemsState.value.companies
                    val position =
                        items.indexOfFirst {
                            item.itemId.equals(
                                it.companyId,
                                ignoreCase = true
                            )
                        }
                    if (position >= 0) {
                        items.removeAt(position)
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        manageItemsState.value = manageItemsState.value.copy(
                            selectedItem = result as Item,
                            isLoading = false
                        )
                    }
                }

                override fun onFailure(message: String) {
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