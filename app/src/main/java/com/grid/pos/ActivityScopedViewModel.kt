package com.grid.pos

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Currency.CurrencyRepository
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
        private val userRepository: UserRepository
) : ViewModel() {
    var posState: POSState = POSState()

    private val _activityState = MutableStateFlow(ActivityState())
    val activityState: MutableStateFlow<ActivityState> = _activityState

    suspend fun initiateValues() {
        fetchCurrentCurrency()
        fetchCurrentUser()
        fetchCurrentCompany()
    }

    private suspend fun fetchCurrentCurrency() {
        if (SettingsModel.currentCurrency == null) {
            viewModelScope.launch(Dispatchers.IO) {
                currencyRepository.getAllCurrencies(object : OnResult {
                    override fun onSuccess(result: Any) {
                        result as List<*>
                        SettingsModel.currentCurrency = if (result.size > 0) result[0] as Currency else Currency()
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

    private suspend fun fetchCurrentCompany() {
        if (SettingsModel.currentCurrency == null) {
            viewModelScope.launch(Dispatchers.IO) {
                companyRepository.getCompanyById(SettingsModel.companyID!!,
                    object : OnResult {
                        override fun onSuccess(result: Any) {
                            SettingsModel.currentCompany = result as Company
                            if (SettingsModel.currentCompany?.companySS == true) {
                                viewModelScope.launch(Dispatchers.IO) {
                                    SettingsModel.currentUserId = null
                                    SettingsModel.currentUser = null
                                    DataStoreManager.removeKey(
                                        DataStoreManager.DataStoreKeys.CURRENT_USER_ID.key
                                    )
                                    withContext(Dispatchers.Main){
                                        activityState.value = activityState.value.copy(
                                            isLoggedIn = false,
                                            warning = SettingsModel.companyAccessWarning
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
    }

    private suspend fun fetchCurrentUser() {
        if (SettingsModel.currentUser == null && !SettingsModel.currentUserId.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                userRepository.getUserById(SettingsModel.currentUserId!!,
                    object : OnResult {
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

    fun getHtmlContent(
            context: Context,
            content: String = Utils.readFileFromAssets(
                "receipt.html",
                context
            )
    ): String {//"file:///android_asset/receipt.html"
        var result = content
        if (posState != null) {
            val trs = StringBuilder("")
            var total = 0.0
            posState!!.invoices.forEach { item ->
                total += item.getAmount()
                trs.append(
                    "<tr> <td>${item.getName()}</td>  <td>${
                        String.format(
                            "%.2f",
                            item.getQuantity()
                        )
                    }</td> <td>$${
                        String.format(
                            "%.2f",
                            item.getPrice()
                        )
                    }</td>  </tr>"
                )
            }
            result = result.replace(
                "{rows_content}",
                trs.toString()
            )
            result = result.replace(
                "{total}",
                String.format(
                    "%.2f",
                    total
                )
            )
        }
        return result
    }
}