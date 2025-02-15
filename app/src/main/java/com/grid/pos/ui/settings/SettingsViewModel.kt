package com.grid.pos.ui.settings

import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.company.Company
import com.grid.pos.data.company.CompanyRepository
import com.grid.pos.model.ReportCountry
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val companyRepository: CompanyRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {
    private var localCompanies: MutableList<Company> = mutableListOf()
    val isLoggedId = sharedViewModel.isLoggedIn

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun fetchLocalCompanies(onResult: (MutableList<Company>) -> Unit) {
        if (localCompanies.isEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                localCompanies = companyRepository.getLocalCompanies()
                withContext(Dispatchers.IO) {
                    onResult.invoke(localCompanies)
                }
            }
        } else {
            onResult.invoke(localCompanies)
        }
    }

    fun clearReportCountries() {
        sharedViewModel.reportCountries.clear()
    }

    fun fetchCountries(onResult: (MutableList<ReportCountry>) -> Unit) {
        sharedViewModel.fetchCountries(onResult)
    }

}