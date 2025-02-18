package com.grid.pos.data.family

import com.google.firebase.firestore.Filter
import com.grid.pos.data.FirebaseWrapper
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Extension.getStringValue

class FamilyRepositoryImpl(
    private val familyDao: FamilyDao
) : FamilyRepository {
    override suspend fun insert(family: Family): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                FirebaseWrapper.insert(
                    "st_family",
                    family
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                familyDao.insert(family)
                DataModel(family)
            }

            else -> {
                return insertByProcedure(family)
            }
        }
    }

    override suspend fun delete(family: Family): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return FirebaseWrapper.delete(
                    "st_family",
                    family
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                familyDao.delete(family)
                return DataModel(family)
            }

            else -> {
                return deleteByProcedure(family)
            }
        }
    }

    override suspend fun update(family: Family): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return FirebaseWrapper.update(
                    "st_family",
                    family
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                familyDao.update(family)
                return DataModel(family)
            }

            else -> {
                return updateFamily(family)
            }
        }
    }

    override suspend fun getAllFamilies(): MutableList<Family> {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "st_family",
                    filters = mutableListOf(
                        Filter.equalTo(
                            "fa_cmp_id",
                            SettingsModel.getCompanyID()
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                val families = mutableListOf<Family>()
                if (size > 0) {
                    for (document in querySnapshot!!) {
                        val obj = document.toObject(Family::class.java)
                        if (obj.familyId.isNotEmpty()) {
                            obj.familyDocumentId = document.id
                            families.add(obj)
                        }
                    }
                }
                families
            }

            CONNECTION_TYPE.LOCAL.key -> {
                familyDao.getAllFamilies(SettingsModel.getCompanyID() ?: "")
            }

            else -> {
                val families: MutableList<Family> = mutableListOf()
                try {
                    val where =
                        if (SettingsModel.isSqlServerWebDb) "fa_cmp_id='${SettingsModel.getCompanyID()}'" else ""
                    val dbResult = SQLServerWrapper.getListOf(
                        "st_family",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            families.add(Family().apply {
                                familyId = it.getStringValue("fa_name")
                                familyName =
                                    if (SettingsModel.isSqlServerWebDb) it.getStringValue("fa_newname") else it.getStringValue(
                                        "fa_name"
                                    )
                                //familyImage = obj.optString("fa_name")
                                familyCompanyId =
                                    if (SettingsModel.isSqlServerWebDb) it.getStringValue("fa_cmp_id") else SettingsModel.getCompanyID()
                            })
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                families
            }
        }
    }

    override suspend fun getFamiliesForPOS(deviceID: String): MutableList<Family> {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "st_family",
                    filters = mutableListOf(
                        Filter.equalTo(
                            "fa_cmp_id",
                            SettingsModel.getCompanyID()
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                val families = mutableListOf<Family>()
                if (size > 0) {
                    for (document in querySnapshot!!) {
                        val obj = document.toObject(Family::class.java)
                        if (obj.familyId.isNotEmpty()) {
                            obj.familyDocumentId = document.id
                            families.add(obj)
                        }
                    }
                }
                families
            }

            CONNECTION_TYPE.LOCAL.key -> {
                familyDao.getAllFamilies(SettingsModel.getCompanyID() ?: "")
            }

            else -> {
                val families: MutableList<Family> = mutableListOf()
                try {
                    val companyId = SettingsModel.getCompanyID()
                    val query = if (SettingsModel.isSqlServerWebDb) {
                        val userGroupDesc = SettingsModel.currentUser?.userGrpDesc ?: ""
                        """
                        if (select count(*) from pos_users_groupbutton where pug_cmp_id='$companyId')>0
                            select distinct(gb_fa_name),gb_id,gb_name,gb_btncolor,gb_txtcolor,gb_txtfontsize,gb_txtfontstyle,gb_image,gb_cmp_id
                            from pos_groupbutton,pos_users_groupbutton where pug_gb_id=gb_id and pug_grp_desc='$userGroupDesc'
                        else
                            select distinct(gb_fa_name),gb_id,gb_name,gb_btncolor,gb_txtcolor,gb_txtfontsize,gb_txtfontstyle,gb_image,gb_cmp_id
                            from pos_groupbutton where gb_cmp_id='$companyId'
                        """
                    } else {
                        """
                        if ('$deviceID') in (SELECT sta_name from pos_station)
                            SELECT * from pos_station_groupbutton,pos_groupbutton,pos_station where psg_gb_id=gb_id and psg_sta_name=sta_name and sta_name='$deviceID' order by psg_order
                        else 
                            SELECT * from pos_station_groupbutton,pos_groupbutton,pos_station where psg_gb_id=gb_id and psg_sta_name=sta_name and sta_name='.' order by psg_order
                        """
                    }
                    val dbResult = SQLServerWrapper.getQueryResult(query)
                    dbResult?.let {
                        while (it.next()) {
                            families.add(Family().apply {
                                familyId =
                                    if (SettingsModel.isSqlServerWebDb) it.getStringValue("gb_id") else it.getStringValue(
                                        "gb_id"
                                    )
                                familyName =
                                    if (SettingsModel.isSqlServerWebDb) it.getStringValue("gb_name") else it.getStringValue(
                                        "gb_name"
                                    )
                                familyCompanyId =
                                    if (SettingsModel.isSqlServerWebDb) it.getStringValue("gb_cmp_id") else companyId
                            })
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                families
            }
        }
    }

    override suspend fun getOneFamily(companyId: String): Family? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "st_family",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "fa_cmp_id",
                            companyId
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                if (size > 0) {
                    for (document in querySnapshot!!) {
                        val obj = document.toObject(Family::class.java)
                        if (obj.familyId.isNotEmpty()) {
                            obj.familyDocumentId = document.id
                            return obj
                        }
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return familyDao.getOneFamily(companyId)
            }

            else -> {
                var family: Family? = null
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) "fa_cmp_id='$companyId'" else ""
                    val dbResult = SQLServerWrapper.getListOf(
                        "st_family",
                        "TOP 1",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        if (it.next()) {
                            family = Family().apply {
                                familyId = it.getStringValue("fa_name")
                                familyName =
                                    if (SettingsModel.isSqlServerWebDb) it.getStringValue("fa_newname") else it.getStringValue(
                                        "fa_name"
                                    )
                                //familyImage = obj.optString("fa_name")
                                familyCompanyId =
                                    if (SettingsModel.isSqlServerWebDb) it.getStringValue("fa_cmp_id") else SettingsModel.getCompanyID()
                            }
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return family
            }
        }
    }

    override suspend fun getFamilyById(familyId: String): Family? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "st_family",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "fa_id",
                            familyId
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                if (size > 0) {
                    for (document in querySnapshot!!) {
                        val obj = document.toObject(Family::class.java)
                        if (obj.familyId.isNotEmpty()) {
                            obj.familyDocumentId = document.id
                            return obj
                        }
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return familyDao.getFamilyById(familyId)
            }

            else -> {
                var family: Family? = null
                try {
                    val dbResult = SQLServerWrapper.getListOf(
                        "st_family",
                        "TOP 1",
                        mutableListOf("*"),
                        "fa_name='$familyId'"
                    )
                    dbResult?.let {
                        if (it.next()) {
                            family = Family().apply {
                                this.familyId = it.getStringValue("fa_name")
                                this.familyName =
                                    if (SettingsModel.isSqlServerWebDb) it.getStringValue("fa_newname") else it.getStringValue(
                                        "fa_name"
                                    )
                                //familyImage = obj.optString("fa_name")
                                this.familyCompanyId =
                                    if (SettingsModel.isSqlServerWebDb) it.getStringValue("fa_cmp_id") else SettingsModel.getCompanyID()
                            }
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return family
            }
        }
    }

    private fun insertByProcedure(family: Family): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                null,//@fa_name
                null,//@fa_parent
                null,//@fa_group
                null,//@fa_numberofyears
                null,//@fa_codelen
                null,//@fa_purchacc
                null,//@fa_depacc
                null,//@fa_depchargeacc
                null,//@fa_salesacc
                null,//@fa_costoffasoldacc
                null,//@fa_periodicityofprovision
                family.familyName,//@fa_newname
                SettingsModel.getCompanyID(),//@fa_cmp_id
                "null_string_output"//@output_message
            )
        } else {
            listOf(
                family.familyName,//@fa_name
                null,//@fa_parent
                null,//@fa_group
                null,//@fa_numberofyears
                null,//@fa_codelen
                null,//@fa_purchacc
                null,//@fa_depacc
                null,//@fa_depchargeacc
                null,//@fa_salesacc
                null,//@fa_costoffasoldacc
                null,//@fa_periodicityofprovision
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addst_family",
            parameters
        )
        if (queryResult.succeed) {
            val id = queryResult.result
            if (id.isNullOrEmpty() && SettingsModel.isSqlServerWebDb) {
                try {
                    val dbResult =
                        SQLServerWrapper.getQueryResult("select max(fa_name) as id from st_item")
                    dbResult?.let {
                        while (it.next()) {
                            family.familyId = it.getStringValue(
                                "id",
                                family.familyId
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (!id.isNullOrEmpty()) {
                family.familyId = id
            }
            return DataModel(family)
        } else {
            return DataModel(
                family,
                false
            )
        }
    }

    private fun updateFamily(
        family: Family
    ): DataModel {
        val columnName = if (SettingsModel.isSqlServerWebDb) "fa_newname" else "fa_name"
        val succeed = SQLServerWrapper.update(
            "st_family",
            listOf(
                columnName
            ),
            listOf(
                family.familyName
            ),
            "fa_name = '${family.familyId}'"
        )
        return DataModel(
            family,
            succeed
        )
    }

    private fun deleteByProcedure(family: Family): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "delst_family",
            listOf(
                family.familyId
            )
        )
        return DataModel(
            family,
            queryResult.succeed
        )
    }

}