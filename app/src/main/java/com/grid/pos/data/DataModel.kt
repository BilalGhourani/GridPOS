package com.grid.pos.data

import com.google.firebase.firestore.Exclude
import java.io.Serializable

abstract class DataModel : Serializable {
    @Exclude
    open fun getId(): String {
        return ""
    }

    @Exclude
    open fun getName(): String {
        return ""
    }

    open fun isNew(): Boolean {
        return true
    }

    @Exclude
    open fun prepareForInsert() {
    }
}