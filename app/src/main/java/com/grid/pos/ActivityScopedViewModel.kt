package com.grid.pos

import android.content.Context
import androidx.lifecycle.ViewModel
import com.grid.pos.ui.pos.POSState
import com.grid.pos.utils.Utils

class ActivityScopedViewModel : ViewModel() {
    var posState: POSState? = null


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