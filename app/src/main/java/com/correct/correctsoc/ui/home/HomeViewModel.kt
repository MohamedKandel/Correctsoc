package com.correct.correctsoc.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.correct.correctsoc.Retrofit.APIService
import com.correct.correctsoc.Retrofit.RetrofitClient
import com.correct.correctsoc.data.auth.forget.ForgotResponse
import com.correct.correctsoc.data.user.UserPlanResponse
import kotlinx.coroutines.launch


class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val homeRepository = HomeRepository(
        RetrofitClient.getClient().create(APIService::class.java)
    )

    private val _getUserPlanResponse = MutableLiveData<UserPlanResponse>()
    private val _notificationMessage = MutableLiveData<ForgotResponse>()

    val notificationMessage: LiveData<ForgotResponse> get() = _notificationMessage
    val userPlanResponse: LiveData<UserPlanResponse> get() = _getUserPlanResponse

    fun getUserPlan(userID: String) = viewModelScope.launch {
        val result = homeRepository.getUserPlan(userID)
        _getUserPlanResponse.postValue(result.body())
    }

    fun getNotificationMessage() = viewModelScope.launch {
        val result = homeRepository.getNotificationMessage()
        _notificationMessage.postValue(result.body())
    }
}