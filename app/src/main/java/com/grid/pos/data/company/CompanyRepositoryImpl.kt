package com.grid.pos.data.company

import com.google.firebase.firestore.Filter
import com.grid.pos.data.FirebaseWrapper
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Extension.getBooleanValue
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getStringValue

class CompanyRepositoryImpl(
        private val companyDao: CompanyDao
) : CompanyRepository {
    override suspend fun insert(
            company: Company
    ): DataModel {
        return if (SettingsModel.isConnectedToFireStore()) {
            FirebaseWrapper.insert(
                "company",
                company
            )
        } else {
            companyDao.insert(company)
            DataModel(company)
        }
    }

    override suspend fun delete(
            company: Company
    ): DataModel {
        return if (SettingsModel.isConnectedToFireStore()) {
            FirebaseWrapper.delete(
                "company",
                company
            )
        } else {
            companyDao.delete(company)
            DataModel(company)
        }
    }

    override suspend fun update(
            company: Company
    ): DataModel {
        return if (SettingsModel.isConnectedToFireStore()) {
            FirebaseWrapper.update(
                "company",
                company
            )
        } else {
            companyDao.update(company)
            DataModel(company)
        }
    }

    override suspend fun getCompanyById(
            id: String
    ): Company? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "company",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "cmp_id",
                            id
                        )
                    )
                )
                val document = querySnapshot?.documents?.firstOrNull()
                val company = document?.toObject(Company::class.java)
                company?.companyDocumentId = document?.id
                return company
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return companyDao.getCompanyById(id)
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                var company: Company? = null
                try {
                    val where = "cmp_id='$id'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "company",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            company = Company().apply {
                                companyId = it.getStringValue("cmp_id")
                                companyName = it.getStringValue("cmp_name")
                                companyPhone = it.getStringValue("cmp_phone")
                                companyAddress = it.getStringValue("cmp_address")
                                companyTaxRegno = it.getStringValue("cmp_vatregno")
                                companyTax = it.getDoubleValue("cmp_vat")
                                companyCurCodeTax = it.getStringValue("cmp_cur_codetax")
                                companyEmail = it.getStringValue("cmp_email")
                                companyWeb = it.getStringValue("cmp_web")
                                companyLogo = it.getStringValue("cmp_logo")
                                companySS = it.getBooleanValue("cmp_ss")
                                companyCountry = it.getStringValue("cmp_country")
                                companyTax1 = it.getDoubleValue("cmp_tax1")
                                companyTax1Regno = it.getStringValue("cmp_tax1regno")
                                companyTax2 = it.getDoubleValue("cmp_tax2")
                                companyTax2Regno = it.getStringValue("cmp_tax2regno")
                                cmp_multibranchcode = it.getStringValue("cmp_multibranchcode")
                            }
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return company
            }
        }
    }

    override suspend fun getAllCompanies(): MutableList<Company> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "company"
                )
                val size = querySnapshot?.size() ?: 0
                val companies = mutableListOf<Company>()
                if (size > 0) {
                    for (document in querySnapshot!!) {
                        val obj = document.toObject(Company::class.java)
                        if (obj.companyId.isNotEmpty()) {
                            obj.companyDocumentId = document.id
                            companies.add(obj)
                        }
                    }
                }
                return companies
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return companyDao.getAllCompanies()
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                val companies: MutableList<Company> = mutableListOf()
                try {
                    val where = "cmp_id='${SettingsModel.getCompanyID()}'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "company",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            companies.add(Company().apply {
                                companyId = it.getStringValue("cmp_id")
                                companyName = it.getStringValue("cmp_name")
                                companyPhone = it.getStringValue("cmp_phone")
                                companyAddress = it.getStringValue("cmp_address")
                                companyTaxRegno = it.getStringValue("cmp_vatregno")
                                companyTax = it.getDoubleValue("cmp_vat")
                                companyCurCodeTax = it.getStringValue("cmp_cur_codetax")
                                companyUpWithTax = it.getBooleanValue("cmp_upwithtax")
                                companyEmail = it.getStringValue("cmp_email")
                                companyWeb = it.getStringValue("cmp_web")
                                companyLogo = it.getStringValue("cmp_logo")
                                companySS = it.getBooleanValue("cmp_ss")
                                companyCountry = it.getStringValue("cmp_country")
                                companyTax1 = it.getDoubleValue("cmp_tax1")
                                companyTax1Regno = it.getStringValue("cmp_tax1regno")
                                companyTax2 = it.getDoubleValue("cmp_tax2")
                                companyTax2Regno = it.getStringValue("cmp_tax2regno")
                                cmp_multibranchcode = it.getStringValue("cmp_multibranchcode")
                            })
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return companies
            }
        }
    }

    override suspend fun getLocalCompanies(): MutableList<Company> {
        return companyDao.getAllCompanies()
    }

    override suspend fun disableCompanies(disabled: Boolean) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "company"
                )
                val size = querySnapshot?.size() ?: 0
                if (size > 0) {
                    for (document in querySnapshot!!) {
                        val obj = document.toObject(Company::class.java)
                        if (obj.companyId.isNotEmpty()) {
                            obj.companyDocumentId = document.id
                            obj.companySS = disabled
                            update(obj)
                        }
                    }
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                val companies = companyDao.getAllCompanies()
                companies.forEach {
                    it.companySS = disabled
                }
                companyDao.updateAll(companies)
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                SQLServerWrapper.update(
                    "company",
                    listOf("cmp_ss"),
                    listOf(if (disabled) "1" else "0"),
                    "cmp_id = ${SettingsModel.getCompanyID()}"
                )
            }
        }
    }
}