package com.grid.pos.ui.company

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Currency.CurrencyRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageCompaniesViewModel @Inject constructor(
    private val companyRepository: CompanyRepository,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _manageCompaniesState = MutableStateFlow(ManageCompaniesState())
    val manageCompaniesState: MutableStateFlow<ManageCompaniesState> = _manageCompaniesState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchCompanies()
            fetchCurrencies()
        }
    }

    private suspend fun fetchCompanies() {
        companyRepository.getAllCompanies(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfCompanies = mutableListOf<Company>()
                (result as List<Company>).forEach {
                    listOfCompanies.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageCompaniesState.value = manageCompaniesState.value.copy(
                        companies = listOfCompanies
                    )
                }
            }

            override fun onFailure(message: String, errorCode: Int) {

            }

        })
    }

    private suspend fun fetchCurrencies() {
        currencyRepository.getAllCurrencies(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfCurrencies = mutableListOf<Currency>()
                (result as List<Currency>).forEach {
                    listOfCurrencies.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageCompaniesState.value = manageCompaniesState.value.copy(
                        currencies = listOfCurrencies
                    )
                }
            }

            override fun onFailure(message: String, errorCode: Int) {

            }

        })
    }

    fun saveCompany(company: Company) {
        if (company.companyName.isNullOrEmpty() || company.companyAddress.isNullOrEmpty()) {
            manageCompaniesState.value = manageCompaniesState.value.copy(
                warning = "Please fill all inputs",
                isLoading = false
            )
            return
        }
        manageCompaniesState.value = manageCompaniesState.value.copy(
            isLoading = true
        )
        val isInserting = company.companyDocumentId.isNullOrEmpty()
        val callback = object : OnResult {
            override fun onSuccess(result: Any) {
                viewModelScope.launch(Dispatchers.Main) {
                    val addedModel = result as Company
                    val companies = manageCompaniesState.value.companies
                    if (isInserting) companies.add(addedModel)
                    manageCompaniesState.value = manageCompaniesState.value.copy(
                        companies = companies,
                        selectedCompany = Company(),
                        isLoading = false,
                        clear = true
                    )
                }
            }

            override fun onFailure(message: String, errorCode: Int) {
                viewModelScope.launch(Dispatchers.Main) {
                    manageCompaniesState.value = manageCompaniesState.value.copy(
                        isLoading = false
                    )
                }
            }

        }
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                company.companyId = Utils.generateRandomUuidString()
                companyRepository.insert(company, callback)
            } else {
                companyRepository.update(company, callback)
            }
        }
    }

    fun deleteSelectedCompany(company: Company) {
        if (company.companyId.isEmpty()) {
            manageCompaniesState.value = manageCompaniesState.value.copy(
                warning = "Please select an company to delete",
                isLoading = false
            )
            return
        }
        manageCompaniesState.value = manageCompaniesState.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            companyRepository.delete(company, object : OnResult {
                override fun onSuccess(result: Any) {
                    val companies = manageCompaniesState.value.companies
                    val position =
                        companies.indexOfFirst {
                            company.companyId.equals(
                                it.companyId,
                                ignoreCase = true
                            )
                        }
                    if (position >= 0) {
                        companies.removeAt(position)
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        manageCompaniesState.value = manageCompaniesState.value.copy(
                            companies = companies,
                            selectedCompany = Company(),
                            isLoading = false,
                            clear = true
                        )
                    }
                }

                override fun onFailure(message: String, errorCode: Int) {
                    viewModelScope.launch(Dispatchers.Main) {
                        manageCompaniesState.value = manageCompaniesState.value.copy(
                            isLoading = false
                        )
                    }
                }

            })
        }
    }

}