package com.correct.correctsoc.ui.selfScan

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.correct.correctsoc.Retrofit.APIService
import com.correct.correctsoc.Retrofit.ClientExternalIP
import com.correct.correctsoc.Retrofit.ClientVendorRetrofit
import com.correct.correctsoc.Retrofit.RetrofitClient
import com.correct.correctsoc.data.GeneralResponse
import com.correct.correctsoc.data.UserIPResponse
import com.correct.correctsoc.data.openPorts.OpenPorts
import com.correct.correctsoc.helper.Constants.API_TAG
import com.correct.correctsoc.helper.RetrofitResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.HttpException


class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val scanRepository = ScanRepository(
        RetrofitClient.getClient()
            .create(APIService::class.java)
    )

    private val externalIPRepository = ScanRepository(
        ClientExternalIP.getClient()
            .create(APIService::class.java)
    )

    private val vendorScanRepo = ScanRepository(
        ClientVendorRetrofit.getClient()
            .create(APIService::class.java)
    )


    private val viewModelJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + viewModelJob)
    private var fetchJob: Job? = null

    val vendorResponse = MutableLiveData<String>()
    val isRequestSuccessfull = MutableLiveData<GeneralResponse>()

    private val _externalIPResponse = MutableLiveData<String>()
    private val _userIPResponse = MutableLiveData<UserIPResponse>()
    private val _scanResponse = MutableLiveData<OpenPorts>()

    val externalIPResponse: LiveData<String> get() = _externalIPResponse
    val scanResponse: LiveData<OpenPorts> get() = _scanResponse
    val userIPResponse: LiveData<UserIPResponse> get() = _userIPResponse

    fun getUserIP(token: String) = viewModelScope.launch {
        val result = scanRepository.getUserIP(token)
        if (result.isSuccessful) {
            _userIPResponse.postValue(result.body())
        } else {
            _userIPResponse.postValue(result.body())
        }
    }

    fun getExternalIP() = viewModelScope.launch {
        val result = externalIPRepository.getExternalIP()
        if (result.isSuccessful) {
            _externalIPResponse.postValue(result.body())
        } else {
            _externalIPResponse.postValue("Failed to get IP")
        }
    }

    fun scan(context: Context, input: String, token: String) = viewModelScope.launch {
        fetchJob = coroutineScope.launch {
            try {
                val result = scanRepository.scan(input, token)
                if (result.isSuccessful) {
                    _scanResponse.postValue(result.body())
                    isRequestSuccessfull.postValue(GeneralResponse(true, "success", 200))
                } else {
                    _scanResponse.postValue(result.body())
                    isRequestSuccessfull.postValue(
                        GeneralResponse(
                            false,
                            result.message(),
                            result.code()
                        )
                    )
                }
            } catch (e: HttpException) {
                Log.e(API_TAG, "scan: ${e.code()}")
                Log.e(API_TAG, "scan: ", e)
            }
        }
    }

    fun cancelFetch() {
        fetchJob?.cancel("Internet connection lost")
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
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