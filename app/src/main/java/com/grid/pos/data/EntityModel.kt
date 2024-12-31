package com.grid.pos.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.io.Serializable

abstract class EntityModel : Serializable {
    @Exclude
    open fun getId(): String {
        return ""
    }

    @Exclude
    open fun getName(): String {
        return ""
    }

    @Exclude
    open fun search(key: String): Boolean {
        return false
    }

    open fun isNew(): Boolean {
        return true
    }

    @Exclude
    open fun prepareForInsert() {
    }

    @Exclude
    open fun getDocumentId(): String? {
        return null
    }

    @Exclude
    open fun setDocumentId(documentId: String) {

    }

    @Exclude
    open fun getMap(): Map<String, Any?> {
        return mutableMapOf()
    }
}