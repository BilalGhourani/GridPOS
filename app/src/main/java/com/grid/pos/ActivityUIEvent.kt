package com.grid.pos

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import com.grid.pos.data.item.Item
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.PopupModel
import com.grid.pos.model.ToastModel
import java.util.ArrayList

sealed class ActivityUIEvent {
    data object Finish : ActivityUIEvent()
    class ShowLoading(
            var show: Boolean,
            val timeout: Long
    ) : ActivityUIEvent()

    class ShowToastMessage(
            var toastModel: ToastModel? = null,
    ) : ActivityUIEvent()

    class ShowPopup(
            var show: Boolean,
            var popupModel: PopupModel?
    ) : ActivityUIEvent()

    class NavigateTo(
            var destination: String
    ) : ActivityUIEvent()

    data object OpenAppSettings : ActivityUIEvent()
    class LaunchGalleryPicker(
            var mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType,
            var delegate: OnGalleryResult,
            var onPermissionDenied: () -> Unit
    ) : ActivityUIEvent()

    class LaunchFilePicker(
            var intentType: String,
            var delegate: OnGalleryResult,
            var onPermissionDenied: () -> Unit
    ) : ActivityUIEvent()

    class StartChooserActivity(
            var intent: Intent
    ) : ActivityUIEvent()

    class LaunchBarcodeScanner(
            var scanToAdd: Boolean,
            var items: ArrayList<Item>?,
            var delegate: OnBarcodeResult,
            var onPermissionDenied: () -> Unit
    ) : ActivityUIEvent()

    class ChangeAppOrientation(
            var orientationType: String
    ) : ActivityUIEvent()
}