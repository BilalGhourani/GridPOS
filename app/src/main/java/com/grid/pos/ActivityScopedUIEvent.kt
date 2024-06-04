package com.grid.pos

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import com.grid.pos.interfaces.OnGalleryResult

sealed class ActivityScopedUIEvent {
    data object Finish : ActivityScopedUIEvent()
    data object OpenAppSettings : ActivityScopedUIEvent()
    class LaunchGalleryPicker(
        var mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType,
        var delegate: OnGalleryResult,
        var onPermissionDenied: () -> Unit
    ) : ActivityScopedUIEvent()

    class LaunchFilePicker(
        var delegate: OnGalleryResult,
        var onPermissionDenied: () -> Unit
    ) : ActivityScopedUIEvent()

    class StartChooserActivity(
        var intent: Intent
    ) : ActivityScopedUIEvent()
}