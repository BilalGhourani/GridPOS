package com.grid.pos.ui.company

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.company.Company
import com.grid.pos.data.company.CompanyRepository
import com.grid.pos.data.currency.Currency
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.family.FamilyRepository
import com.grid.pos.data.posPrinter.PosPrinterRepository
import com.grid.pos.data.thirdParty.ThirdPartyRepository
import com.grid.pos.data.user.UserRepository
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManageCompaniesViewModel @Inject constructor(
        private val companyRepository: CompanyRepository,
        private val currencyRepository: CurrencyRepository,
        private val posPrinterRepository: PosPrinterRepository,
        private val familyRepository: FamilyRepository,
        private val thirdPartyRepository: ThirdPartyRepository,
        private val userRepository: UserRepository
) : BaseViewModel() {

    private val _manageCompaniesState = MutableStateFlow(ManageCompaniesState())
    val manageCompaniesState: MutableStateFlow<ManageCompaniesState> = _manageCompaniesState
    var currentCompany: Company = Company()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            fetchCurrencies()
        }
    }

    fun resetState() {
        manageCompaniesState.value = manageCompaniesState.value.copy(
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun showWarning(
            warning: String?,
            action: String? = null
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            manageCompaniesState.value = manageCompaniesState.value.copy(
                warning = if (warning != null) Event(warning) else null,
                actionLabel = action,
                isLoading = false
            )
        }
    }

    fun fetchCompanies() {
        manageCompaniesState.value = manageCompaniesState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val dataModel = companyRepository.getAllCompanies()
            if (dataModel.succeed) {
                val listOfCompanies = convertToMutableList(
                    dataModel.data,
                    Company::class.java
                )
                withContext(Dispatchers.Main) {
                    manageCompaniesState.value = manageCompaniesState.value.copy(
                        companies = listOfCompanies,
                        isLoading = false
                    )
                }
            } else {
                showWarning(dataModel.message ?: "an error has occurred!")
            }
        }
    }

    private suspend fun fetchCurrencies() {
        val dataModel = currencyRepository.getAllCurrencies()
        if (dataModel.succeed) {
            val currencies = convertToMutableList(
                dataModel.data,
                Currency::class.java
            )
            viewModelScope.launch(Dispatchers.Main) {
                manageCompaniesState.value = manageCompaniesState.value.copy(
                    currencies = currencies
                )
            }
        } else {
            showWarning(dataModel.message)
        }
    }

    fun saveCompany(
            company: Company,
            isRegistering: Boolean
    ) {
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
                if (companies.isNotEmpty()) {
                    companies.add(addedCompany)
                }
                if (isRegistering) {
                    SettingsModel.currentCompany = addedCompany
                    SettingsModel.localCompanyID = addedCompany.companyId
                    DataStoreManager.putString(
                        DataStoreManager.DataStoreKeys.LOCAL_COMPANY_ID.key,
                        addedCompany.companyId
                    )
                    withContext(Dispatchers.Main) {
                        manageCompaniesState.value = manageCompaniesState.value.copy(
                            companies = companies,
                            selectedCompany = Company(),
                            isLoading = false,
                            warning = Event("Company saved successfully, do you want to continue?"),
                            actionLabel = "next",
                            clear = true
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        manageCompaniesState.value = manageCompaniesState.value.copy(
                            companies = companies,
                            selectedCompany = Company(),
                            isLoading = false,
                            warning = Event("Company saved successfully."),
                            clear = true
                        )
                    }
                }
            } else {
                companyRepository.update(company)
                if (company.companyId.equals(
                        SettingsModel.currentCompany?.companyId,
                        ignoreCase = true
                    )
                ) {
                    SettingsModel.currentCompany = company
                }
                val index = manageCompaniesState.value.companies.indexOfFirst { it.companyId == company.companyId }
                if (index >= 0) {
                    manageCompaniesState.value.companies.removeAt(index)
                    manageCompaniesState.value.companies.add(
                        index,
                        company
                    )
                }
                withContext(Dispatchers.Main) {
                    manageCompaniesState.value = manageCompaniesState.value.copy(
                        selectedCompany = company,
                        isLoading = false,
                        warning = Event("Company saved successfully."),
                        clear = true
                    )
                }
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
            if (hasRelations(company.companyId)) {
                withContext(Dispatchers.Main) {
                    manageCompaniesState.value = manageCompaniesState.value.copy(
                        warning = Event("You can't delete this Company,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            companyRepository.delete(company)
            val companies = manageCompaniesState.value.companies
            companies.remove(company)
            withContext(Dispatchers.Main) {
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

    private suspend fun hasRelations(companyID: String): Boolean {
        val familyDataModel = familyRepository.getOneFamily(companyID)
        if (familyDataModel.succeed && familyDataModel.data != null) return true
        if (userRepository.getOneUser(companyID) != null) {
            return true
        }
        if (thirdPartyRepository.getOneThirdPartyByCompanyID(companyID) != null) {
            return true
        }
        if (posPrinterRepository.getOnePosPrinter(companyID) != null) {
            return true
        }
        return false
    }

}