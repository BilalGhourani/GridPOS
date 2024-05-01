package com.grid.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.User.User
import com.grid.pos.data.User.UserRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.ui.pos.POSState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityScopedViewModel : ViewModel() {
    var posState: POSState? = null


}