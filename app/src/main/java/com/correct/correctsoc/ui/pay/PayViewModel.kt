package com.correct.correctsoc.ui.pay

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.correct.correctsoc.Retrofit.APIService
import com.correct.correctsoc.Retrofit.RetrofitClient
import com.correct.correctsoc.data.auth.forget.ForgotResponse
import com.correct.correctsoc.data.pay.SubscibeGooglePayBody
import com.correct.correctsoc.data.pay.SubscribeCodeBody
import kotlinx.coroutines.launch

class PayViewModel(application: Application) : AndroidViewModel(application) {
    private val payRepository = PayRepository(
        RetrofitClient.getClient()
            .create(APIService::class.java)
    )


    private val _getCostResponse = MutableLiveData<ForgotResponse>()
    private val _orderPayWithGooglePayResponse = MutableLiveData<ForgotResponse>()
    private val _subscribeWithGooglePayResponse = MutableLiveData<ForgotResponse>()
    private val _subscribeWithCodeResponse = MutableLiveData<ForgotResponse>()

    val subscribeWithCodeResponse: LiveData<ForgotResponse> get() = _subscribeWithCodeResponse
    val subscribeWithGooglePay: LiveData<ForgotResponse> get() = _subscribeWithGooglePayResponse
    val orderPayWithGooglePayResponse: LiveData<ForgotResponse> get() = _orderPayWithGooglePayResponse
    val getCostResponse: LiveData<ForgotResponse> get() = _getCostResponse

    fun getCost(devices: Int, months: Int, years: Int) = viewModelScope.launch {
        val result = payRepository.getCost(devices, months, years)
        if (result.isSuccessful) {
            _getCostResponse.postValue(result.body())
        } else {
            _getCostResponse.postValue(result.body())
        }
    }

    fun orderPayWithGooglePay() = viewModelScope.launch {
        val result = payRepository.orderPayWithGooglePay()
        if (result.isSuccessful) {
            _orderPayWithGooglePayResponse.postValue(result.body())
        } else {
            _orderPayWithGooglePayResponse.postValue(result.body())
        }
    }

    fun subscribeWithGooglePay(body: SubscibeGooglePayBody) = viewModelScope.launch {
        val result = payRepository.subscribeWithGooglePay(body)
        if (result.isSuccessful) {
            _subscribeWithGooglePayResponse.postValue(result.body())
        } else {
            _subscribeWithGooglePayResponse.postValue(result.body())
        }
    }

    fun subscribeWithCode(body: SubscribeCodeBody) = viewModelScope.launch {
        val result = payRepository.subscribeWithCode(body)
        if (result.isSuccessful) {
            _subscribeWithCodeResponse.postValue(result.body())
        }
    }
}