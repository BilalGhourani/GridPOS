package com.grid.pos

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Currency.CurrencyRepository
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.pos.POSState
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityScopedViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {
    var posState: POSState = POSState()


    fun fetchCurrencies() {
        if (SettingsModel.currentCurrency == null) {
            viewModelScope.launch(Dispatchers.IO) {
                currencyRepository.getAllCurrencies(object : OnResult {
                    override fun onSuccess(result: Any) {
                        result as List<*>
                        SettingsModel.currentCurrency =
                            if (result.size > 0) result[0] as Currency else Currency()
                    }

                    override fun onFailure(message: String, errorCode: Int) {

                    }
                })
            }
        }
    }

    fun getHtmlContent(
        context: Context,
        content: String = Utils.readHtmlFromAssets("receipt.html", context)
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
                    }</td> <td>$${String.format("%.2f", item.getPrice())}</td>  </tr>"
                )
            }
            result = result.replace("{rows_content}", trs.toString())
            result = result.replace("{total}", String.format("%.2f", total))
        }
        return result
    }
}