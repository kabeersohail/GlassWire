package com.example.glasswire.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.glasswire.models.AppUsageModel

class SharedViewModel: ViewModel() {
    private val _appDataUsageList: MutableLiveData<List<AppUsageModel>> = MutableLiveData()
    val appDataUsageList: LiveData<List<AppUsageModel>> = _appDataUsageList

    fun setAppDataUsageList(appUsageModels: List<AppUsageModel>) {
        _appDataUsageList.postValue(appUsageModels)
    }

}