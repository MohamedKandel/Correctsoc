package com.correct.correctsoc.ui.auth

import com.correct.correctsoc.Retrofit.APIService
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRepository(private val apiService: APIService) {
    suspend fun registerUser(body: RegisterBody): Response<AuthResponse> =
        withContext(Dispatchers.IO) {
            apiService.registerUser(body)
        }

    suspend fun confirmOTP(body: ConfirmPhoneBody): Response<AuthResponse> =
        withContext(Dispatchers.IO) {
            apiService.confirmOTP(body)
        }

    suspend fun resendOTP(phone: String): Response<AuthResponse> = withContext(Dispatchers.IO) {
        apiService.resendOTP(phone)
    }

    suspend fun forgetPassword(phone: String): Response<ForgotResponse> =
        withContext(Dispatchers.IO) {
            apiService.forgetPassword(phone)
        }

    suspend fun resetPassword(body: ResetPasswordBody, token: String): Response<AuthResponse> =
        withContext(Dispatchers.IO) {
            apiService.resetPassword(body, token)
        }

    suspend fun login(body: LoginBody): Response<AuthResponse> =
        withContext(Dispatchers.IO) {
            apiService.login(body)
        }

    suspend fun signOut(body: SignOutBody, token: String): Response<AuthResponse> =
        withContext(Dispatchers.IO) {
            apiService.signOut(body, token)
        }

    suspend fun updateUsername(body: UpdateUsernameBody, token: String): Response<AuthResponse> =
        withContext(Dispatchers.IO) {
            apiService.updateUsername(body, token)
        }

    suspend fun generateOTPUpdatePhone(
        body: GenerateOTPBody,
        token: String
    ): Response<AuthResponse> =
        withContext(Dispatchers.IO) {
            apiService.sendOTPUpdatePhone(body, token)
        }

    suspend fun updatePhone(body: UpdatePhoneBody, token: String): Response<AuthResponse> =
        withContext(Dispatchers.IO) {
            apiService.updatePhone(body, token)
        }

    suspend fun updatePassword(body: UpdatePasswordBody, token: String): Response<AuthResponse> =
        withContext(Dispatchers.IO) {
            apiService.updatePassword(body, token)
        }

    suspend fun validateToken(token: String): Response<Void> =
        withContext(Dispatchers.IO) {
            apiService.validateToken(token)
        }

    suspend fun validateOTP(body: ValidateOTPBody): Response<AuthResponse> =
        withContext(Dispatchers.IO) {
            apiService.validateOTP(body)
        }

    suspend fun setDeviceOn(token: String): Response<Void> =
        withContext(Dispatchers.IO) {
            apiService.setDeviceOn(token)
        }

    suspend fun setDeviceOff(token: String): Response<Void> =
        withContext(Dispatchers.IO) {
            apiService.setDeviceOff(token)
        }

    suspend fun deleteAccount(userID: String, token: String): Response<ForgotResponse> =
        withContext(Dispatchers.IO) {
            apiService.deleteAccount(userID, token)
        }

    suspend fun getMailByPhone(phone: String): Response<String> =
        withContext(Dispatchers.IO) {
            apiService.getMailByPhone(phone)
        }
}