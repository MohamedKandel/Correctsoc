package com.correct.correctsoc.Retrofit

import com.correct.correctsoc.data.UserIPResponse
import com.correct.correctsoc.data.openPorts.OpenPorts
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {
    @GET("GetUserIp")
    suspend fun getUserIP(): Response<UserIPResponse>

    @POST("NmapAppScan")
    suspend fun Scan(@Query("input") input: String): Response<OpenPorts>

    @GET("{macAddress}")
    fun getVendor(@Path("macAddress") macAddress: String) : Call<ResponseBody>
}