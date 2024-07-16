package com.correct.correctsoc.ui.selfScan

import com.correct.correctsoc.Retrofit.APIService
import com.correct.correctsoc.data.UserIPResponse
import com.correct.correctsoc.data.openPorts.OpenPorts
import com.correct.correctsoc.data.openPorts.Port
import com.correct.correctsoc.helper.RetrofitResponse
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanRepository(private val apiService: APIService) {

    suspend fun getUserIP(token: String): Response<UserIPResponse> =
        withContext(Dispatchers.IO) {
            apiService.getUserIP(token)
        }

    suspend fun scan(input: String, token: String): Response<OpenPorts> =
        withContext(Dispatchers.IO) {
            apiService.scan(input, token)
        }

    fun getVendor(macAddress: String, callback: RetrofitResponse<String>) {
        apiService.getVendor(macAddress).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        callback.onSuccess(response.body()!!.string())
                    } catch (ex: Exception) {
                        callback.onFailed(ex.message.toString())
                    }
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        if (errorBody != null) {
                            val jsonParser = JsonParser()
                            val errorJson = jsonParser.parse(errorBody).asJsonObject
                            val errorDetail =
                                errorJson.getAsJsonObject("errors").get("detail").asString
                            callback.onSuccess(errorDetail)
                        } else {
                            callback.onFailed("Unknown error")
                        }
                    } catch (ex: Exception) {
                        callback.onFailed(ex.message.toString())
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback.onFailed("Unknown error")
            }
        })
    }
}