package com.example.servicedemo.local_binding

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ServiceViewModel : ViewModel() {


    val _serviceStarted = MutableLiveData(false)


    val _serviceStopped = MutableLiveData(false)


    val _serviceBind = MutableLiveData(false)


    val _serviceUnBind = MutableLiveData(false)

    val currentRandomValue = MutableLiveData<Int>(0)

    fun setServiceStarted() {
        _serviceStarted.value = true
    }

    fun setServiceStopped() {
        _serviceStopped.value = true
    }

    fun setServiceBind() {
        _serviceBind.value = true
    }

    fun setServiceUnBind() {
        _serviceUnBind.value = true
    }

    fun setRandomValue(value: Int) {
        currentRandomValue.postValue(value)
    }

}