package com.grid.pos.ui.pos

import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.grid.pos.data.family.Family
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.item.Item
import com.grid.pos.data.posPrinter.PosPrinter
import com.grid.pos.data.posReceipt.PosReceipt
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.user.User
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.PopupState

data class POSState(
    val invoiceHeader: InvoiceHeader = InvoiceHeader(),
    val invoiceItems: MutableList<InvoiceItemModel> = mutableListOf(),
    var posReceipt: PosReceipt = PosReceipt(),
    var families: MutableList<Family> = mutableListOf(),
    var items: MutableList<Item> = mutableListOf(),
    var thirdParties: MutableList<ThirdParty> = mutableListOf(),
    var invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf(),
    var users: MutableList<User> = mutableListOf(),
    var printers: MutableList<PosPrinter> = mutableListOf(),
    var selectedThirdParty: ThirdParty = ThirdParty(),

    val isEditBottomSheetVisible: Boolean = false,
    val isAddItemBottomSheetVisible: Boolean = false,
    val isPayBottomSheetVisible: Boolean = false,
    val isSavePopupVisible: Boolean = false,
    val popupState: PopupState = PopupState.BACK_PRESSED,

    val orientation: Int = Configuration.ORIENTATION_PORTRAIT,

    var isSaved: Boolean = false,
    var isDeleted: Boolean = false,
    var isLoading: Boolean = false,
    val warning: Event<String>? = null,
    val actionLabel: String? = null,
) {
    val isAnyPopupShown: Boolean
        get() {
            return isEditBottomSheetVisible || isAddItemBottomSheetVisible || isPayBottomSheetVisible
        }

    val isLandscape: Boolean
        get() {
            return orientation == Configuration.ORIENTATION_LANDSCAPE
        }
}