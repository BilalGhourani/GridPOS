package com.grid.pos.data.Company

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.tasks.await

class CompanyRepositoryImpl(
        private val companyDao: CompanyDao
) : CompanyRepository {
    override suspend fun insert(
            company: Company
    ): Company {
        if (SettingsModel.isConnectedToFireStore()) {
            val docRef = FirebaseFirestore.getInstance().collection("company").add(company).await()
            company.companyDocumentId = docRef.id
        } else {
            companyDao.insert(company)
        }
        return company
    }

    override suspend fun delete(
            company: Company
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            company.companyDocumentId?.let {
                FirebaseFirestore.getInstance().collection("company").document(it).delete().await()
            }
        } else {
            companyDao.delete(company)
        }
    }

    override suspend fun update(
            company: Company
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            company.companyDocumentId?.let {
                FirebaseFirestore.getInstance().collection("company")
                    .document(it).update(company.getMap()).await()
            }

        } else {
            companyDao.update(company)
        }
    }

    override suspend fun getCompanyById(
            id: String
    ): Company? {
        var company: Company? = null
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("company").whereEqualTo(
                "cmp_id",
                id
            ).get().await()
            val document = querySnapshot.documents.firstOrNull()
            if (document != null) {
                company = document.toObject(Company::class.java)
            }

        } else {
            company = companyDao.getCompanyById(id)
        }
        return company
    }

    override suspend fun getAllCompanies(): MutableList<Company> {
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("company").get().await()
            val companies = mutableListOf<Company>()
            if (querySnapshot.size() > 0) {
                for (document in querySnapshot) {
                    val obj = document.toObject(Company::class.java)
                    if (obj.companyId.isNotEmpty()) {
                        obj.companyDocumentId = document.id
                        companies.add(obj)
                    }
                }
            }
            return companies
        } else {
            return companyDao.getAllCompanies()
        }
    }

    override suspend fun getLocalCompanies(): MutableList<Company> {
        return companyDao.getAllCompanies()
    }
}