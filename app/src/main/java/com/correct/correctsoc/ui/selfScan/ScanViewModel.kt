package com.correct.correctsoc.ui.selfScan

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.correct.correctsoc.Retrofit.APIService
import com.correct.correctsoc.Retrofit.ClientVendorRetrofit
import com.correct.correctsoc.Retrofit.RetrofitClient
import com.correct.correctsoc.data.ResultResponse
import com.correct.correctsoc.data.UserIPResponse
import com.correct.correctsoc.data.openPorts.OpenPorts
import com.correct.correctsoc.data.openPorts.Port
import com.correct.correctsoc.helper.Constants.API_TAG
import com.correct.correctsoc.helper.RetrofitResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flow
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


    private val viewModelJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + viewModelJob)
    private var fetchJob: Job? = null

    val vendorResponse = MutableLiveData<String>()
    val isRequestSuccessfull = MutableLiveData<Boolean>()
    private val _userIPResponse = MutableLiveData<UserIPResponse>()
    private val _scanResponse = MutableLiveData<OpenPorts>()

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

    fun scan(context: Context, input: String, token:String) = viewModelScope.launch {
        fetchJob = coroutineScope.launch {
            try {
                val result = scanRepository.scan(input, token)
                if (result.isSuccessful) {
                    _scanResponse.postValue(result.body())
                    isRequestSuccessfull.postValue(true)
                } else {
                    _scanResponse.postValue(result.body())
                    isRequestSuccessfull.postValue(false)
                    Toast.makeText(context, result.message(), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
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