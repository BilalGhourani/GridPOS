package com.grid.pos.ui.company

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.company.Company
import com.grid.pos.data.company.CompanyRepository
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

    private val _state = MutableStateFlow(ManageCompaniesState())
    val state: MutableStateFlow<ManageCompaniesState> = _state

    var oldImage: String? = null
    var currentCompany: Company = Company()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            fetchCurrencies()
        }
    }

    fun resetState() {
        currentCompany = Company()
        state.value = state.value.copy(
            company = Company(),
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun updateState(newState: ManageCompaniesState) {
        _state.value = newState
    }

    fun isAnyChangeDone(): Boolean {
        return state.value.company.didChanged(currentCompany)
    }

    fun showWarning(
        warning: String?,
        action: String? = null
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            state.value = state.value.copy(
                warning = if (warning != null) Event(warning) else null,
                actionLabel = action,
                isLoading = false
            )
        }
    }

    fun fetchCompanies() {
        state.value = state.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfCompanies = companyRepository.getAllCompanies()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    companies = listOfCompanies,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun fetchCurrencies() {
        val currencies = currencyRepository.getAllCurrencies()
        viewModelScope.launch(Dispatchers.Main) {
            state.value = state.value.copy(
                currencies = currencies
            )
        }
    }

    fun save(
        isRegistering: Boolean
    ) {
        val company = state.value.company
        if (company.companyName.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                state.value = state.value.copy(
                    warning = Event("Please fill company name and Currency."),
                    isLoading = false
                )
            }
            return
        }
        state.value = state.value.copy(
            isLoading = true
        )
        val isInserting = company.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                company.prepareForInsert()
                val dataModel = companyRepository.insert(company)
                if (dataModel.succeed) {
                    val addedCompany = dataModel.data as Company
                    val companies = state.value.companies
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
                            state.value = state.value.copy(
                                companies = companies,
                                isLoading = false,
                                warning = Event("Company saved successfully, do you want to continue?"),
                                actionLabel = "next",
                                clear = true
                            )
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            state.value = state.value.copy(
                                companies = companies,
                                isLoading = false,
                                warning = Event("Company saved successfully."),
                                clear = true
                            )
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false,
                            warning = null,
                            actionLabel = null
                        )
                    }
                }
            } else {
                val dataModel = companyRepository.update(company)
                if (dataModel.succeed) {
                    if (company.companyId.equals(
                            SettingsModel.currentCompany?.companyId,
                            ignoreCase = true
                        )
                    ) {
                        SettingsModel.currentCompany = company
                    }
                    val index =
                        state.value.companies.indexOfFirst { it.companyId == company.companyId }
                    if (index >= 0) {
                        state.value.companies.removeAt(index)
                        state.value.companies.add(
                            index,
                            company
                        )
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false,
                            warning = Event("Company saved successfully."),
                            clear = true
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false,
                            warning = null,
                            actionLabel = null
                        )
                    }
                }
            }
        }
    }

    fun delete() {
        val company = state.value.company
        if (company.companyId.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                state.value = state.value.copy(
                    warning = Event("Please select an company to delete"),
                    isLoading = false
                )
            }
            return
        }
        state.value = state.value.copy(
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(company.companyId)) {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        warning = Event("You can't delete this Company,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            val dataModel = companyRepository.delete(company)
            if (dataModel.succeed) {
                val companies = state.value.companies
                companies.remove(company)
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        companies = companies,
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        clear = true
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        isLoading = false,
                        warning = null,
                        actionLabel = null
                    )
                }
            }
        }
    }

    private suspend fun hasRelations(companyID: String): Boolean {
        if (familyRepository.getOneFamily(companyID) != null) return true
        if (userRepository.getOneUser(companyID) != null) {
            return true
        }
        if (thirdPartyRepository.getOneThirdPartyByCompanyID(companyID) != null) {
            return true
        }
        return posPrinterRepository.getOnePosPrinter(companyID) != null
    }

}