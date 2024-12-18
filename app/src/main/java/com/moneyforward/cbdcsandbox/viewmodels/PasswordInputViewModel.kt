package com.moneyforward.cbdcsandbox.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PasswordInputViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

}