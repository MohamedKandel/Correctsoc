package com.correct.correctsoc.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.correct.correctsoc.Retrofit.APIService
import com.correct.correctsoc.Retrofit.RetrofitClient
import com.correct.correctsoc.data.user.AdsResponse
import com.correct.correctsoc.data.user.UserPlanResponse
import kotlinx.coroutines.launch


class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val homeRepository = HomeRepository(
        RetrofitClient.getClient().create(APIService::class.java)
    )

    private val _getUserPlanResponse = MutableLiveData<UserPlanResponse>()
    private val _getAdvertisements = MutableLiveData<AdsResponse>()

    val advertisements: LiveData<AdsResponse> get() = _getAdvertisements
    val userPlanResponse: LiveData<UserPlanResponse> get() = _getUserPlanResponse

    fun getUserPlan(userID: String) = viewModelScope.launch {
        val result = homeRepository.getUserPlan(userID)
        _getUserPlanResponse.postValue(result.body())
    }

    fun getAdvertisement() = viewModelScope.launch {
        val result = homeRepository.getAds()
        _getAdvertisements.postValue(result.body())
    }
}