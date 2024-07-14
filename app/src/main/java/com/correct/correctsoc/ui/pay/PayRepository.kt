package com.correct.correctsoc.ui.pay

import com.correct.correctsoc.Retrofit.APIService
import com.correct.correctsoc.data.auth.forget.ForgotResponse
import com.correct.correctsoc.data.pay.SubscibeGooglePayBody
import com.correct.correctsoc.data.pay.SubscribeCodeBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response


class PayRepository(private val apiService: APIService) {

    suspend fun getCost(devices: Int, months: Int, years: Int): Response<ForgotResponse> =
        withContext(Dispatchers.IO) {
            apiService.getCost(devices, months, years)
        }

    suspend fun orderPayWithGooglePay(): Response<ForgotResponse> =
        withContext(Dispatchers.IO) {
            apiService.orderPayWithGooglePay()
        }

    suspend fun subscribeWithGooglePay(body: SubscibeGooglePayBody): Response<ForgotResponse> =
        withContext(Dispatchers.IO) {
            apiService.subscribeWithGooglePay(body)
        }

    suspend fun subscribeWithCode(body: SubscribeCodeBody): Response<ForgotResponse> =
        withContext(Dispatchers.IO) {
            apiService.subscribeWithCode(body)
        }
}