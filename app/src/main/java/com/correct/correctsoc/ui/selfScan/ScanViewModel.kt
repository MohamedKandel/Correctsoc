package com.correct.correctsoc.ui.selfScan

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.correct.correctsoc.Retrofit.APIService
import com.correct.correctsoc.Retrofit.ClientVendorRetrofit
import com.correct.correctsoc.Retrofit.RetrofitClient
import com.correct.correctsoc.data.UserIPResponse
import com.correct.correctsoc.data.openPorts.OpenPorts
import com.correct.correctsoc.helper.Constants.API_TAG
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.RetrofitResponse
import kotlinx.coroutines.launch


class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val scanRepository = ScanRepository(
        RetrofitClient.getClient()
            .create(APIService::class.java)
    )

    private val vendorScanRepo = ScanRepository(
        ClientVendorRetrofit.getClient()
            .create(APIService::class.java)
    )

    var userIPResponse = MutableLiveData<UserIPResponse>()
    val scanResponse = MutableLiveData<OpenPorts>()
    val vendorResponse = MutableLiveData<String>()
    val isRequestSuccessfull = MutableLiveData<Boolean>()

    fun getUserIP() = viewModelScope.launch {
        val result = scanRepository.getUserIP()
        if (result.isSuccessful) {
            userIPResponse.postValue(result.body())
        } else {
            Log.e(API_TAG, "getUserIP: ${result.code()}")
        }
    }

    fun scan(input: String, context: Context) = viewModelScope.launch {
        val result = scanRepository.scan(input)
        if (result.isSuccessful) {
            scanResponse.postValue(result.body())
            isRequestSuccessfull.postValue(true)
        } else {
            Log.e(API_TAG, "scan: ${result.code()}")
            Log.e(API_TAG, "scan: ${result.message()}")
            isRequestSuccessfull.postValue(false)
            Toast.makeText(context, result.message(), Toast.LENGTH_SHORT).show()
        }
    }


    fun fetchResponse(macAddress: String) {
        vendorScanRepo.getVendor(macAddress, object : RetrofitResponse<String> {
            override fun onSuccess(response: String) {
                vendorResponse.postValue(response)
            }

            override fun onFailed(error: String) {
                vendorResponse.postValue(error)
            }
        })
    }
}