package com.grid.pos.interfaces

import android.content.Intent
import android.net.Uri

interface OnActivityResult {

    // ------------------------------------------------------------------------
    // HOOKS INTO ACTIVITY
    // ------------------------------------------------------------------------
    fun onActivityResult(resultCode: Int, data: Intent?)
}

interface OnGalleryResult {

    // ------------------------------------------------------------------------
    // HOOKS INTO ACTIVITY
    // ------------------------------------------------------------------------
    fun onGalleryResult(uris: List<Uri>)
}