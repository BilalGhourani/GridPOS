package com.grid.pos.data

import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.grid.pos.SharedViewModel
import com.grid.pos.data.invoice.Invoice
import com.grid.pos.model.DataModel
import kotlinx.coroutines.tasks.await

object FirebaseWrapper {

    private lateinit var sharedViewModel: SharedViewModel

    fun initialize(model: SharedViewModel) {
        this.sharedViewModel = model
    }

    suspend fun insert(
            collection: String,
            model: EntityModel
    ): DataModel {
        return try {
            val docRef = FirebaseFirestore.getInstance().collection(collection).add(model).await()
            model.setDocumentId(docRef.id)
            DataModel(
                model,
                true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (::sharedViewModel.isInitialized) {
                e.message?.let {
                    sharedViewModel.showWarning(it)
                }
            }
            DataModel(
                model,
                false
            )
        }
    }

    suspend fun update(
            collection: String,
            model: EntityModel
    ): DataModel {
        try {
            model.getDocumentId()?.let {
                FirebaseFirestore.getInstance().collection(collection).document(it)
                    .update(model.getMap()).await()
                return DataModel(
                    model,
                    true
                )
            }
            return DataModel(
                model,
                false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (::sharedViewModel.isInitialized) {
                e.message?.let {
                    sharedViewModel.showWarning(it)
                }
            }
            return DataModel(
                model,
                false
            )
        }
    }

    suspend fun update(
            collection: String,
            models: List<EntityModel>
    ): DataModel {
        try {
            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()
            for (model in models) {
                val modelRef = db.collection(collection)
                    .document(model.getDocumentId()!!) // Assuming `id` is the document ID
                batch.update(
                    modelRef,
                    model.getMap()
                )
            }
            batch.commit().await()
            return DataModel(
                models,
                true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (::sharedViewModel.isInitialized) {
                e.message?.let {
                    sharedViewModel.showWarning(it)
                }
            }
            return DataModel(
                models,
                false
            )
        }
    }

    suspend fun delete(
            collection: String,
            model: EntityModel
    ): DataModel {
        try {
            model.getDocumentId()?.let {
                FirebaseFirestore.getInstance().collection(collection).document(it).delete().await()
                return DataModel(
                    model,
                    true
                )
            }
            return DataModel(
                model,
                false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (::sharedViewModel.isInitialized) {
                e.message?.let {
                    sharedViewModel.showWarning(it)
                }
            }
            return DataModel(
                model,
                false
            )
        }
    }

    suspend fun getQuerySnapshot(
            collection: String,
            limit: Long? = null,
            filters: MutableList<Filter> = mutableListOf(),
            orderBy: MutableList<Pair<String, Query.Direction>> = mutableListOf(),
    ): QuerySnapshot? {
        try {
            val collectionRef = FirebaseFirestore.getInstance().collection(collection)
            var query: Query = collectionRef
            // Apply filtering to the query
            for (i in 0 until filters.size) {
                query = query.where(filters[i])
            }
            // Apply ordering to the query
            for ((field, direction) in orderBy) {
                query = query.orderBy(
                    field,
                    direction
                )
            }
            // Apply limit if provided
            if (limit != null) {
                query = query.limit(limit)
            }
            return query.get().await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (::sharedViewModel.isInitialized) {
                e.message?.let {
                    sharedViewModel.showWarning(it)
                }
            }
            return null
        }
    }

}