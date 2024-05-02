package com.grid.pos.data.Company

import androidx.lifecycle.asLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.User.User
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.SettingsModel


class CompanyRepositoryImpl(
    private val companyDao: CompanyDao
) : CompanyRepository {
    override suspend fun insert(company: Company, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("company")
                .add(company)
                .addOnSuccessListener {
                    company.companyDocumentId = it.id
                    callback?.onSuccess(company)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            companyDao.insert(company)
            callback?.onSuccess(company)
        }

    }

    override suspend fun delete(company: Company, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("company")
                .document(company.companyDocumentId!!)
                .delete()
                .addOnSuccessListener {
                    callback?.onSuccess(company)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            companyDao.delete(company)
            callback?.onSuccess(company)
        }
    }

    override suspend fun update(company: Company, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("company")
                .document(company.companyDocumentId!!)
                .update(company.getMap())
                .addOnSuccessListener {
                    callback?.onSuccess(company)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            companyDao.update(company)
            callback?.onSuccess(company)
        }
    }

    override suspend fun getCompanyById(id: String): Company {
        return companyDao.getCompanyById(id)
    }

    override suspend fun getAllCompanies(callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("company").get()
                .addOnSuccessListener { result ->
                    val companies = mutableListOf<Company>()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(Company::class.java)
                            if (!obj.companyId.isNullOrEmpty()) {
                                obj.companyDocumentId = document.id
                                companies.add(obj)
                            }
                        }
                    }
                    callback?.onSuccess(companies)
                }.addOnFailureListener { exception ->
                    callback?.onFailure(
                        exception.message ?: "Network error! Can't get companies from remote."
                    )
                }
        } else {
            companyDao.getAllCompanies().collect {
                callback?.onSuccess(it)
            }
        }
    }
}