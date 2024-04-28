package com.grid.pos.data.Company

import androidx.lifecycle.asLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.interfaces.OnResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class CompanyRepositoryImpl(
    private val companyDao: CompanyDao
) : CompanyRepository {
    override suspend fun insert(company: Company, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("company")
            .add(company)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    companyDao.insert(company)
                    callback?.onSuccess(company)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun delete(company: Company, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("company")
            .document(company.companyDocumentId!!)
            .delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    companyDao.delete(company)
                    callback?.onSuccess(company)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun update(company: Company, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("company")
            .document(company.companyDocumentId!!)
            .update(company.getMap())
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    companyDao.update(company)
                    callback?.onSuccess(company)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun getCompanyById(id: String): Company {
        return companyDao.getCompanyById(id)
    }

    override suspend fun getAllCompanies(callback: OnResult?) {
        val localCompanies = companyDao.getAllCompanies().asLiveData().value
        if (!localCompanies.isNullOrEmpty()) {
            callback?.onSuccess(localCompanies)
        }
        FirebaseFirestore.getInstance().collection("company").get()
            .addOnSuccessListener { result ->
                CoroutineScope(Dispatchers.IO).launch {
                    val companies = mutableListOf<Company>()
                    companyDao.deleteAll()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(Company::class.java)
                            if (!obj.companyId.isNullOrEmpty()) {
                                obj.companyDocumentId = document.id
                                companies.add(obj)
                            }
                        }
                        companyDao.insertAll(companies.toList())
                    }
                    callback?.onSuccess(companies)
                }
            }.addOnFailureListener { exception ->
                callback?.onFailure(
                    exception.message ?: "Network error! Can't get companies from remote."
                )
            }
    }
}