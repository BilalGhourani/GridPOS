package com.grid.pos.ui.settings

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.company.Company
import com.grid.pos.data.company.CompanyRepository
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
        private val companyRepository: CompanyRepository
) : BaseViewModel() {
    private var localCompanies: MutableList<Company> = mutableListOf()

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


}