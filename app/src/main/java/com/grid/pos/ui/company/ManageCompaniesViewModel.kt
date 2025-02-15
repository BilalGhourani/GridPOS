package com.grid.pos.ui.company

import android.content.Context
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.company.Company
import com.grid.pos.data.company.CompanyRepository
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.family.FamilyRepository
import com.grid.pos.data.posPrinter.PosPrinterRepository
import com.grid.pos.data.thirdParty.ThirdPartyRepository
import com.grid.pos.data.user.UserRepository
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.DataStoreManager
import com.grid.pos.utils.FileUtils
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
    private val userRepository: UserRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

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
            companyTaxStr = "",
            companyTax1Str = "",
            companyTax2Str = ""
        )
    }

    fun updateState(newState: ManageCompaniesState) {
        _state.value = newState
    }

    fun isAnyChangeDone(): Boolean {
        return state.value.company.didChanged(currentCompany)
    }

    fun checkChanges(context: Context, callback: (Boolean, Boolean) -> Unit) {
        if (isLoading()) {
            return
        }
        if (isAnyChangeDone()) {
            sharedViewModel.showPopup(true,
                PopupModel().apply {
                    onDismissRequest = {
                        resetState()
                        callback.invoke(false, sharedViewModel.isRegistering)
                    }
                    onConfirmation = {
                        save(context) {
                            callback.invoke(true, it)
                        }
                    }
                    dialogTitle = null
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                    icon = null
                })
        } else {
            callback.invoke(false, sharedViewModel.isRegistering)
        }
    }

    fun fetchCompanies() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val listOfCompanies = companyRepository.getAllCompanies()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    companies = listOfCompanies
                )
                showLoading(false)
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

    fun save(context: Context, callback: (Boolean) -> Unit = {}) {
        val company = state.value.company
        if (company.companyName.isNullOrEmpty()) {
            showWarning("Please fill company name and Currency.")
            return
        }
        showLoading(true)
        val isInserting = company.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            oldImage?.let { old ->
                FileUtils.deleteFile(
                    context,
                    old
                )
            }
            val firstCurr = state.value.currencies.firstOrNull() ?: SettingsModel.currentCurrency
            company.companyCurCodeTax = firstCurr?.currencyId
            if (isInserting) {
                company.prepareForInsert()
                val dataModel = companyRepository.insert(company)
                if (dataModel.succeed) {
                    val addedCompany = dataModel.data as Company
                    val companies = state.value.companies
                    if (companies.isNotEmpty()) {
                        companies.add(addedCompany)
                    }
                    if (sharedViewModel.isRegistering) {
                        SettingsModel.currentCompany = addedCompany
                        SettingsModel.localCompanyID = addedCompany.companyId
                        DataStoreManager.putString(
                            DataStoreManager.DataStoreKeys.LOCAL_COMPANY_ID.key,
                            addedCompany.companyId
                        )
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            companies = companies
                        )
                        resetState()
                        showLoading(false)
                        if (sharedViewModel.isRegistering) {
                            callback.invoke(true)
                        } else {
                            showWarning("Company saved successfully.")
                            callback.invoke(false)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
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
                    val companies = state.value.companies.toMutableList()
                    val index = companies.indexOfFirst { it.companyId == company.companyId }
                    if (index >= 0) {
                        companies.removeAt(index)
                        companies.add(index, company)
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            companies = companies
                        )
                        resetState()
                        showLoading(false)
                        showWarning("Company saved successfully.")
                        callback.invoke(false)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            }
        }
    }

    fun delete() {
        val company = state.value.company
        if (company.companyId.isEmpty()) {
            showWarning(("Please select an company to delete"))
            return
        }
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(company.companyId)) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showWarning("You can't delete this Company,because it has related data!")
                }
                return@launch
            }
            val dataModel = companyRepository.delete(company)
            if (dataModel.succeed) {
                val companies = state.value.companies.toMutableList()
                companies.remove(company)
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        companies = companies
                    )
                    resetState()
                    showLoading(false)
                    showWarning("successfully deleted.")
                }
            } else {
                withContext(Dispatchers.Main) {
                    showLoading(false)
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

    fun launchGalleryPicker(context: Context) {
        sharedViewModel.launchGalleryPicker(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
            object : OnGalleryResult {
                override fun onGalleryResult(uris: List<Uri>) {
                    if (uris.isNotEmpty()) {
                        sharedViewModel.copyToInternalStorage(
                            context,
                            uris[0],
                            "company logo",
                            (state.value.company.companyName ?: "company").trim()
                                .replace(
                                    " ",
                                    "_"
                                )
                        ) { internalPath ->
                            if (internalPath != null) {
                                oldImage =
                                    state.value.company.companyLogo
                                updateState(
                                    state.value.copy(
                                        company = state.value.company.copy(
                                            companyLogo = internalPath
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            },
            onPermissionDenied = {
                showWarning(
                    "Permission Denied", "Settings"
                ) {
                    sharedViewModel.openAppStorageSettings()
                }
            })
    }

}