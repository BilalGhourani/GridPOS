package com.grid.pos.ui.login

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.grid.pos.App
import com.grid.pos.SharedViewModel
import com.grid.pos.data.company.Company
import com.grid.pos.data.company.CompanyRepository
import com.grid.pos.data.connection.ConnectionRepository
import com.grid.pos.data.user.User
import com.grid.pos.data.user.UserRepository
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.LoginResponse
import com.grid.pos.model.LoginResponseModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ToastModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.useCases.CheckLicenseUseCase
import com.grid.pos.ui.navigation.Screen
import com.grid.pos.utils.Constants
import com.grid.pos.utils.CryptoUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val checkLicenseUseCase: CheckLicenseUseCase,
    private val repository: UserRepository,
    private val companyRepository: CompanyRepository,
    private val connectionRepository: ConnectionRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

    var usernameState = mutableStateOf("")
    var passwordState = mutableStateOf("")
    var companies = mutableListOf<Company>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun login(context: Context, callback: (String) -> Unit) {
        val username = usernameState.value.trim()
        val password = passwordState.value.trim()
        if (username.isEmpty() || password.isEmpty()) {
            showWarning("Please fill all inputs")
            return
        }
        if (App.getInstance().isMissingFirebaseConnection()) {
            showWarning("unable to connect to server", "Settings") {
                callback.invoke("SettingsView")
            }
            return
        }
        showLoading(true)

        viewModelScope.launch(Dispatchers.IO) {
            if (Constants.PLAY_STORE_VERSION) {
                val model = connectionRepository.login(username, password)
                if (model.success == 1 && model.encrypted != null) {
                    model.encrypted = CryptoUtils.decryptDES(
                        model.encrypted!!,
                        App.getInstance().getConfigValue("api_key")
                    )
                    fillConnectionParams(model)
                    proceedWithLogin(callback)
                } else {
                    withContext(Dispatchers.Main) {
                        showWarning("Username or Password are incorrect!")
                        sharedViewModel.showLoading(false)
                    }
                }
            } else {
                checkLicenseUseCase.invoke(context, onResult = { result, message ->
                    viewModelScope.launch(Dispatchers.Main) {
                        when (result) {
                            Constants.SUCCEEDED -> {
                                loginNow(username, password, callback)
                            }

                            else -> {
                                showWarning(message)
                                callback.invoke("LicenseView")
                            }
                        }
                    }
                })
            }
        }
    }

    private suspend fun loginNow(username: String, password: String, callback: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            val companyId = SettingsModel.getCompanyID()
            if (!companyId.isNullOrEmpty()) {
                SettingsModel.currentCompany = companyRepository.getCompanyById(companyId)
                if (SettingsModel.currentCompany?.companySS == true) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        showWarning(SettingsModel.companyAccessWarning)
                    }
                    return@withContext
                }
            }
            val loginResponse: LoginResponse = repository.getUserByCredentials(username, password)
            loginResponse.user?.let {
                SettingsModel.currentUser = it
                proceedWithLogin(callback)
            } ?: run {
                if (!loginResponse.error.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        showWarning(loginResponse.error, "Settings") {
                            callback.invoke("SettingsView")
                        }
                    }
                } else if (loginResponse.allUsersSize == 0) {
                    if (SettingsModel.isConnectedToSqlite() || SettingsModel.isConnectedToFireStore()) {
                        if (companies.isEmpty()) {
                            companies = companyRepository.getAllCompanies()
                        }
                        withContext(Dispatchers.Main) {
                            sharedViewModel.isRegistering = true
                            showLoading(false)
                            if (companies.isEmpty()) {
                                showWarning(
                                    "No companies found!, do you want to register?",
                                    "Register"
                                ) {
                                    callback.invoke("ManageCompaniesView")
                                }
                            } else if (SettingsModel.getCompanyID().isNullOrEmpty()) {
                                showWarning("select your current company to proceed!", "Settings") {
                                    callback.invoke("SettingsView")
                                }
                            } else {
                                showWarning(
                                    "No users found!, do you want to create a user?",
                                    "Create"
                                ) {
                                    callback.invoke("ManageUsersView")
                                }
                            }
                        }
                    } else {
                        viewModelScope.launch(Dispatchers.Main) {
                            showLoading(false)
                            showWarning("No users found!")
                        }
                    }
                } else {
                    viewModelScope.launch(Dispatchers.Main) {
                        showLoading(false)
                        showWarning("Username or Password are incorrect!")
                    }
                }
            }

        }
    }

    fun backPressed() {
        sharedViewModel.finish()
    }

    private suspend fun proceedWithLogin(callback: (String) -> Unit) {
        sharedViewModel.isLoggedIn = true
        sharedViewModel.homeWarning = null
        sharedViewModel.initiateValues()
        withContext(Dispatchers.Main) {
            usernameState.value = ""
            passwordState.value = ""
            showLoading(false)
            val user = checkUserSettings(SettingsModel.currentUser)
            user?.let {
                if (it.userPosMode && it.userTableMode) {
                    callback.invoke(Screen.HomeView.route)
                } else if (it.userPosMode) {
                    callback.invoke(Screen.POSView.route)
                } else if (it.userTableMode) {
                    callback.invoke(Screen.TablesView.route)
                } else {
                    callback.invoke(Screen.HomeView.route)
                }
            }
        }
    }

    private fun checkUserSettings(user: User?): User? {
        if (user != null) {
            if (sharedViewModel.checkPermission("Run In POS Mode", false)) {
                user.userPosMode = true
                user.userTableMode = false
            } else if (sharedViewModel.checkPermission(
                    "Table Management: Open Table Number",
                    false
                )
            ) {
                user.userPosMode = false
                user.userTableMode = true
            } else if (sharedViewModel.checkPermission("Run POS In Table Number Mode", false)) {
                user.userPosMode = false
                user.userTableMode = true
            }
        }
        return user
    }

    private fun fillConnectionParams(model: LoginResponseModel) {
        try {
            val conData = JSONObject(model.encrypted)
            SettingsModel.connectionType = CONNECTION_TYPE.SQL_SERVER.key
            SettingsModel.isSqlServerWebDb = true
            if (conData.has("user")) {
                SettingsModel.currentUser = Gson().fromJson(model.user.toString(), User::class.java)
                SettingsModel.sqlServerCompanyId = SettingsModel.currentUser?.userCompanyId
            }
            if (conData.has("server")) {
                SettingsModel.sqlServerPath = conData.optString("server")
                if (conData.has("port")) {
                    SettingsModel.sqlServerPath += ":${conData.optInt("port")}"
                }
            }
            if (conData.has("host")) {
                SettingsModel.sqlServerName = conData.optString("host")
            }
            if (conData.has("database")) {
                SettingsModel.sqlServerDbName = conData.optString("database")
            }
            if (conData.has("user")) {
                SettingsModel.sqlServerDbUser = conData.optString("user")
            }
            if (conData.has("password")) {
                SettingsModel.sqlServerDbPassword = conData.optString("password")
            }
            if (conData.has("options")) {
                val optionObj = JSONObject(conData.optString("options"))
                SettingsModel.encrypt = optionObj.optBoolean("encrypt", true)
                SettingsModel.trustServerCertificate =
                    conData.optBoolean("trustServerCertificate", true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}