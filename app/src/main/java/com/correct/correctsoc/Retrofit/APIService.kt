package com.correct.correctsoc.Retrofit

import com.correct.correctsoc.data.auth.AuthResponse
import com.correct.correctsoc.data.auth.ConfirmPhoneBody
import com.correct.correctsoc.data.auth.GenerateOTPBody
import com.correct.correctsoc.data.auth.LoginBody
import com.correct.correctsoc.data.auth.RegisterBody
import com.correct.correctsoc.data.auth.ResetPasswordBody
import com.correct.correctsoc.data.auth.SignOutBody
import com.correct.correctsoc.data.auth.UpdatePasswordBody
import com.correct.correctsoc.data.auth.UpdatePhoneBody
import com.correct.correctsoc.data.auth.UpdateUsernameBody
import com.correct.correctsoc.data.auth.ValidateOTPBody
import com.correct.correctsoc.data.auth.forget.ForgotResponse
import com.correct.correctsoc.data.openPorts.OpenPorts
import com.correct.correctsoc.data.pay.PromoCodePercentResponse
import com.correct.correctsoc.data.pay.SubscibeGooglePayBody
import com.correct.correctsoc.data.pay.SubscribeCodeBody
import com.correct.correctsoc.data.user.UserPlanResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {
    @POST("Authentication/RegisterUser")
    suspend fun registerUser(@Body body: RegisterBody): Response<AuthResponse>

    @POST("Authentication/ConfirmEmail")
    suspend fun confirmOTP(@Body body: ConfirmPhoneBody): Response<AuthResponse>

    @POST("Authentication/SendNewOTP")
    suspend fun resendOTP(@Query("email") phone: String): Response<AuthResponse>

    @POST("Authentication/ForgetPassword")
    suspend fun forgetPassword(
        @Query("email") email: String
    ): Response<ForgotResponse>

    @POST("Authentication/ValidateResetOtp")
    suspend fun validateOTP(@Body body: ValidateOTPBody): Response<AuthResponse>

    @POST("Authentication/ResetPassword")
    suspend fun resetPassword(
        @Body body: ResetPasswordBody,
        @Header("Authorization") token: String
    ): Response<AuthResponse>

    @POST("Authentication/SignIn")
    suspend fun login(@Body body: LoginBody): Response<AuthResponse>

    @POST("Authentication/SignOut")
    suspend fun signOut(
        @Body body: SignOutBody,
        @Header("Authorization") token: String
    ): Response<AuthResponse>

    @PUT("Authentication/UpdateUserName")
    suspend fun updateUsername(
        @Body body: UpdateUsernameBody,
        @Header("Authorization") token: String
    ): Response<AuthResponse>

    @POST("Authentication/GenerateUpdatePhoneOTP")
    suspend fun sendOTPUpdatePhone(
        @Body body: GenerateOTPBody,
        @Header("Authorization") token: String
    ): Response<AuthResponse>

    @PUT("Authentication/UpdatePhone")
    suspend fun updatePhone(
        @Body body: UpdatePhoneBody,
        @Header("Authorization") token: String
    ): Response<AuthResponse>

    @PUT("Authentication/UpdatePassword")
    suspend fun updatePassword(
        @Body body: UpdatePasswordBody,
        @Header("Authorization") token: String
    ): Response<AuthResponse>

    @GET("Authentication/ValidateToken")
    suspend fun validateToken(@Header("Authorization") token: String): Response<Void>

    /*@GET("NmapScan/GetUserIp")
    suspend fun getUserIP(@Header("Authorization") token: String): Response<UserIPResponse>*/

    @POST("NmapScan/NmapAppScan")
    suspend fun scan(
        @Query("IpORWeblink") input: String,
        @Header("Authorization") token: String
    ): Response<OpenPorts>

    @GET("User/Cost")
    suspend fun getCost(
        @Query("numberDevices") number: Int,
        @Query("months") months: Int,
        @Query("years") years: Int
    ): Response<ForgotResponse>

    @POST("User/SetDeviceOn")
    suspend fun setDeviceOn(@Header("Authorization") token: String): Response<Void>

    @POST("User/SetDeviceOff")
    suspend fun setDeviceOff(@Header("Authorization") token: String): Response<Void>

    @POST("Payment/OrderPay")
    suspend fun orderPayWithGooglePay(): Response<ForgotResponse>

    @POST("Payment/Subscribe")
    suspend fun subscribeWithGooglePay(@Body body: SubscibeGooglePayBody): Response<ForgotResponse>

    @POST("User/Subscribe")
    suspend fun subscribeWithCode(@Body body: SubscribeCodeBody): Response<ForgotResponse>

    @GET("User/GetUser")
    suspend fun getUserPlan(@Query("id") id: String): Response<UserPlanResponse>

    @POST("Payment/GetPromotionPercentag")
    suspend fun getPromoCodePercent(@Query("code") code: String): Response<PromoCodePercentResponse>

    @DELETE("Authentication/DeleteAccount")
    suspend fun deleteAccount(@Query("userId") userID: String,
                              @Header("Authorization") token: String): Response<ForgotResponse>

    @POST("Authentication/GetEmail")
    suspend fun getMailByPhone(@Query("phone") phone: String): Response<String>

    @GET("User/GetNotificationMessage")
    suspend fun getNotificationMessage(): Response<ForgotResponse>

    // get vendor name of device with mac address
    @GET("{macAddress}")
    fun getVendor(@Path("macAddress") macAddress: String): Call<ResponseBody>

    @GET("ip")
    suspend fun getExternalIP(): Response<String>
}