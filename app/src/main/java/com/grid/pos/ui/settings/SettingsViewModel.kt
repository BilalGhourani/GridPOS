package com.grid.pos.ui.settings

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.Settings.SettingsRepository
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
    private val settingsRepository: SettingsRepository
) : BaseViewModel() {
    private var localCompanies: MutableList<Company> = mutableListOf()
    private var reportCountries: MutableList<ReportCountry> = mutableListOf()

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

    fun fetchCountries(onResult: (MutableList<ReportCountry>) -> Unit){
        if (localCompanies.isEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                reportCountries =   settingsRepository.getCountries()
                withContext(Dispatchers.IO) {
                    onResult.invoke(reportCountries)
                }
            }
        } else {
            onResult.invoke(reportCountries)
        }
    }


}