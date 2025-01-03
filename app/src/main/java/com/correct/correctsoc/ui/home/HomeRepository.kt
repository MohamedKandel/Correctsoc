package com.correct.correctsoc.ui.home

import com.correct.correctsoc.Retrofit.APIService
import com.correct.correctsoc.data.auth.forget.ForgotResponse
import com.correct.correctsoc.data.user.UserPlanResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class HomeRepository(private val apiService: APIService) {
    suspend fun getUserPlan(userID: String): Response<UserPlanResponse> =
        withContext(Dispatchers.IO) {
            apiService.getUserPlan(userID)
        }

    suspend fun getNotificationMessage(): Response<ForgotResponse> =
        withContext(Dispatchers.IO) {
            apiService.getNotificationMessage()
        }
}