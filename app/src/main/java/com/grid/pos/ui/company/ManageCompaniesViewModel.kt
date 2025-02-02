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
import kotlinx.coroutines.flow.asStateFlow
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

    private var _companyState = MutableStateFlow(Company())
    var companyState = _companyState.asStateFlow()

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
        updateCompany(Company())
        manageCompaniesState.value = manageCompaniesState.value.copy(
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun updateCompany(company: Company) {
        _companyState.value = company
    }

    fun isAnyChangeDone():Boolean{
        return companyState.value.didChanged(currentCompany)
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
            val listOfCompanies = companyRepository.getAllCompanies()
            withContext(Dispatchers.Main) {
                manageCompaniesState.value = manageCompaniesState.value.copy(
                    companies = listOfCompanies,
                    isLoading = false
                )
            }
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

    fun save(
        isRegistering: Boolean
    ) {
        if (companyState.value.companyName.isNullOrEmpty()) {
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
        val isInserting = companyState.value.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                companyState.value.prepareForInsert()
                val dataModel = companyRepository.insert(companyState.value)
                if (dataModel.succeed) {
                    val addedCompany = dataModel.data as Company
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
                                isLoading = false,
                                warning = Event("Company saved successfully."),
                                clear = true
                            )
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        manageCompaniesState.value = manageCompaniesState.value.copy(
                            isLoading = false,
                            warning = null,
                            actionLabel = null
                        )
                    }
                }
            } else {
                val dataModel = companyRepository.update(companyState.value)
                if (dataModel.succeed) {
                    if (companyState.value.companyId.equals(
                            SettingsModel.currentCompany?.companyId,
                            ignoreCase = true
                        )
                    ) {
                        SettingsModel.currentCompany = companyState.value
                    }
                    val index =
                        manageCompaniesState.value.companies.indexOfFirst { it.companyId == companyState.value.companyId }
                    if (index >= 0) {
                        manageCompaniesState.value.companies.removeAt(index)
                        manageCompaniesState.value.companies.add(
                            index,
                            companyState.value
                        )
                    }
                    withContext(Dispatchers.Main) {
                        manageCompaniesState.value = manageCompaniesState.value.copy(
                            isLoading = false,
                            warning = Event("Company saved successfully."),
                            clear = true
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        manageCompaniesState.value = manageCompaniesState.value.copy(
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
        if (companyState.value.companyId.isEmpty()) {
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
            if (hasRelations(companyState.value.companyId)) {
                withContext(Dispatchers.Main) {
                    manageCompaniesState.value = manageCompaniesState.value.copy(
                        warning = Event("You can't delete this Company,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            val dataModel = companyRepository.delete(companyState.value)
            if (dataModel.succeed) {
                val companies = manageCompaniesState.value.companies
                companies.remove(companyState.value)
                withContext(Dispatchers.Main) {
                    manageCompaniesState.value = manageCompaniesState.value.copy(
                        companies = companies,
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        clear = true
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    manageCompaniesState.value = manageCompaniesState.value.copy(
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