package com.grid.pos

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Currency.CurrencyRepository
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Family.FamilyRepository
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.Item.Item
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.data.ThirdParty.ThirdPartyRepository
import com.grid.pos.data.User.User
import com.grid.pos.data.User.UserRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.pos.POSState
import com.grid.pos.utils.DataStoreManager
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ActivityScopedViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val companyRepository: CompanyRepository,
    private val userRepository: UserRepository,
    private val thirdPartyRepository: ThirdPartyRepository,
    private val familyRepository: FamilyRepository,
    private val itemRepository: ItemRepository,
) : ViewModel() {
    var posState: POSState = POSState()
    var isFromTable: Boolean = false
    var companies: MutableList<Company> = mutableListOf()
    var currencies: MutableList<Currency> = mutableListOf()
    var users: MutableList<User> = mutableListOf()
    var thirdParties: MutableList<ThirdParty> = mutableListOf()
    var families: MutableList<Family> = mutableListOf()
    var items: MutableList<Item> = mutableListOf()
    var invoices: MutableList<InvoiceHeader> = mutableListOf()

    private val _activityState = MutableStateFlow(ActivityState())
    val activityState: MutableStateFlow<ActivityState> = _activityState

    suspend fun initiateValues() {
        if (SettingsModel.currentUser != null) {
            fetchCurrencies()
            fetchCurrentUser()
            fetchCompanies()
            fetchThirdParties()
            fetchFamilies()
            fetchItems()
        }
    }

    private suspend fun fetchCurrencies() {
        if (SettingsModel.currentCurrency == null) {
            viewModelScope.launch(Dispatchers.IO) {
                currencyRepository.getAllCurrencies(object : OnResult {
                    override fun onSuccess(result: Any) {
                        result as List<*>
                        val listOfCurrencies = mutableListOf<Currency>()
                        result.forEach {
                            it as Currency
                            if (it.currencyId.equals(
                                    SettingsModel.currentCompany?.companyCurCodeTax,
                                    ignoreCase = true
                                )
                            ) {
                                SettingsModel.currentCurrency = it
                            }
                            listOfCurrencies.add(it)
                        }
                        currencies = listOfCurrencies
                        if (SettingsModel.currentCurrency == null) {
                            SettingsModel.currentCurrency = Currency()
                        }
                    }

                    override fun onFailure(
                        message: String,
                        errorCode: Int
                    ) {

                    }
                })
            }
        }
    }

    private suspend fun fetchCompanies() {
        viewModelScope.launch(Dispatchers.IO) {
            companyRepository.getAllCompanies(object : OnResult {
                override fun onSuccess(result: Any) {
                    val listOfCompanies = mutableListOf<Company>()
                    (result as List<*>).forEach {
                        it as Company
                        if (it.companyId.equals(SettingsModel.companyID, ignoreCase = true)) {
                            SettingsModel.currentCompany = it
                        }
                        listOfCompanies.add(it)
                    }

                    if (SettingsModel.currentCompany?.companySS == true) {
                        viewModelScope.launch(Dispatchers.IO) {
                            SettingsModel.currentUserId = null
                            SettingsModel.currentUser = null
                            DataStoreManager.removeKey(
                                DataStoreManager.DataStoreKeys.CURRENT_USER_ID.key
                            )
                            withContext(Dispatchers.Main) {
                                activityState.value = activityState.value.copy(
                                    isLoggedIn = false, warning = SettingsModel.companyAccessWarning
                                )
                            }
                        }
                    }
                }

                override fun onFailure(
                    message: String,
                    errorCode: Int
                ) {

                }
            })
        }
    }

    private suspend fun fetchCurrentUser() {
        if (SettingsModel.currentUser == null && !SettingsModel.currentUserId.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                userRepository.getUserById(SettingsModel.currentUserId!!, object : OnResult {
                    override fun onSuccess(result: Any) {
                        SettingsModel.currentUser = result as User
                    }

                    override fun onFailure(
                        message: String,
                        errorCode: Int
                    ) {

                    }
                })
            }
        }
    }

    private suspend fun fetchThirdParties() {
        thirdPartyRepository.getAllThirdParties(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfThirdParties = mutableListOf<ThirdParty>()
                (result as List<*>).forEach {
                    listOfThirdParties.add(it as ThirdParty)
                }
                thirdParties = listOfThirdParties
            }

            override fun onFailure(
                message: String,
                errorCode: Int
            ) {
            }
        })
    }

    private suspend fun fetchItems() {
        itemRepository.getAllItems(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfItems = mutableListOf<Item>()
                (result as List<*>).forEach {
                    listOfItems.add(it as Item)
                }
                items = listOfItems
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
                families = listOfFamilies
            }

            override fun onFailure(
                message: String,
                errorCode: Int
            ) {
            }
        })
    }

    fun getHtmlContent(
        context: Context,
        content: String = Utils.readFileFromAssets(
            "receipt.html", context
        )
    ): String {//"file:///android_asset/receipt.html"
        var result = content
        if (posState.invoices.isNotEmpty()) {
            val trs = StringBuilder("")
            var total = 0.0
            posState.invoices.forEach { item ->
                total += item.getAmount()
                trs.append(
                    "<tr> <td>${item.getName()}</td>  <td>${
                        String.format(
                            "%.2f", item.getQuantity()
                        )
                    }</td> <td>$${
                        String.format(
                            "%.2f", item.getPrice()
                        )
                    }</td>  </tr>"
                )
            }
            result = result.replace(
                "{rows_content}", trs.toString()
            )
            result = result.replace(
                "{total}", String.format(
                    "%.2f", total
                )
            )
        }
        return result
    }
}