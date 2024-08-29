package com.grid.pos

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import com.grid.pos.data.Item.Item
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.PopupModel
import java.util.ArrayList

sealed class ActivityScopedUIEvent {
    data object Finish : ActivityScopedUIEvent()
    class ShowLoading(var show: Boolean) : ActivityScopedUIEvent()
    class ShowPopup(
            var show: Boolean,
            var popupModel: PopupModel?
    ) : ActivityScopedUIEvent()

    data object OpenAppSettings : ActivityScopedUIEvent()
    class LaunchGalleryPicker(
            var mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType,
            var delegate: OnGalleryResult,
            var onPermissionDenied: () -> Unit
    ) : ActivityScopedUIEvent()

    class LaunchFilePicker(
            var intentType: String,
            var delegate: OnGalleryResult,
            var onPermissionDenied: () -> Unit
    ) : ActivityScopedUIEvent()

    class StartChooserActivity(
            var intent: Intent
    ) : ActivityScopedUIEvent()

    class LaunchBarcodeScanner(
            var scanToAdd: Boolean,
            var items: ArrayList<Item>?,
            var delegate: OnBarcodeResult,
            var onPermissionDenied: () -> Unit
    ) : ActivityScopedUIEvent()

    class ChangeAppOrientation(
            var orientationType: String
    ) : ActivityScopedUIEvent()
}