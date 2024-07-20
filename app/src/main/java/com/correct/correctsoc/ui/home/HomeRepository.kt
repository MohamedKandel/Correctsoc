package com.correct.correctsoc.ui.home

import com.correct.correctsoc.Retrofit.APIService
import com.correct.correctsoc.data.user.AdsResponse
import com.correct.correctsoc.data.user.UserPlanResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class HomeRepository(private val apiService: APIService) {
    suspend fun getUserPlan(userID: String): Response<UserPlanResponse> =
        withContext(Dispatchers.IO) {
            apiService.getUserPlan(userID)
        }

    suspend fun getAds():Response<AdsResponse> =
        withContext(Dispatchers.IO) {
            apiService.getAdvertisements()
        }
}