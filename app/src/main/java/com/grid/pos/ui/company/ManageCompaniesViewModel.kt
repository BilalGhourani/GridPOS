package com.grid.pos.ui.company

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Currency.CurrencyRepository
import com.grid.pos.data.PosPrinter.PosPrinterRepository
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageCompaniesViewModel @Inject constructor(
        private val companyRepository: CompanyRepository,
        private val currencyRepository: CurrencyRepository,
        private val posPrinterRepository: PosPrinterRepository
) : ViewModel() {

    private val _manageCompaniesState = MutableStateFlow(ManageCompaniesState())
    val manageCompaniesState: MutableStateFlow<ManageCompaniesState> = _manageCompaniesState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchCompanies()
            fetchCurrencies()
            //fetchPrinters()
        }
    }

    fun showWarning(
            warning: String,
            action: String
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            manageCompaniesState.value = manageCompaniesState.value.copy(
                warning = Event(warning),
                actionLabel = action,
                isLoading = false
            )
        }
    }

    fun fillCachedCompanies(
            companies: MutableList<Company> = mutableListOf(),
            currencies: MutableList<Currency> = mutableListOf()
    ) {
        if (manageCompaniesState.value.companies.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                manageCompaniesState.value = manageCompaniesState.value.copy(
                    companies = companies,
                    currencies = currencies
                )
            }
        }
    }

    private suspend fun fetchCompanies() {
        val companies = companyRepository.getAllCompanies()
        viewModelScope.launch(Dispatchers.Main) {
            manageCompaniesState.value = manageCompaniesState.value.copy(
                companies = companies
            )
        }
    }

    private suspend fun fetchCurrencies() {
        val currencies = currencyRepository.getAllCurrencies()
        viewModelScope.launch(Dispatchers.Main) {
            manageCompaniesState.value = manageCompaniesState.value.copy(
                currencies = currencies
            )
        }
    }

    private suspend fun fetchPrinters() {
        val listOfPrinters = posPrinterRepository.getAllPosPrinters()
        viewModelScope.launch(Dispatchers.Main) {
            manageCompaniesState.value = manageCompaniesState.value.copy(
                printers = listOfPrinters
            )
        }
    }

    fun saveCompany(company: Company) {
        if (company.companyName.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                manageCompaniesState.value = manageCompaniesState.value.copy(
                    warning = Event("Please fill company name and Currency."),
                    isLoading = false
                )
            }
            return
        }
        manageCompaniesState.value = manageCompaniesState.value.copy(
            isLoading = true
        )
        val isInserting = company.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                company.prepareForInsert()
                val addedCompany = companyRepository.insert(company)
                val companies = manageCompaniesState.value.companies
                companies.add(addedCompany)
                manageCompaniesState.value = manageCompaniesState.value.copy(
                    companies = companies,
                    selectedCompany = Company(),
                    isLoading = false,
                    warning = Event("Company saved successfully."),
                    clear = true
                )
            } else {
                companyRepository.update(company)
                manageCompaniesState.value = manageCompaniesState.value.copy(
                    selectedCompany = company,
                    isLoading = false,
                    warning = Event("Company saved successfully."),
                    clear = true
                )
            }
        }
    }

    fun deleteSelectedCompany(company: Company) {
        if (company.companyId.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                manageCompaniesState.value = manageCompaniesState.value.copy(
                    warning = Event("Please select an company to delete"),
                    isLoading = false
                )
            }
            return
        }
        manageCompaniesState.value = manageCompaniesState.value.copy(
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            companyRepository.delete(company)
            val companies = manageCompaniesState.value.companies
            companies.remove(company)
            viewModelScope.launch(Dispatchers.Main) {
                manageCompaniesState.value = manageCompaniesState.value.copy(
                    companies = companies,
                    selectedCompany = Company(),
                    isLoading = false,
                    warning = Event("successfully deleted."),
                    clear = true
                )
            }
        }
    }

}