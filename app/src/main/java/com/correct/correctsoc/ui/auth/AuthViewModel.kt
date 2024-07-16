package com.correct.correctsoc.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.correct.correctsoc.Retrofit.APIService
import com.correct.correctsoc.Retrofit.APIWorker
import com.correct.correctsoc.Retrofit.RetrofitClient
import com.correct.correctsoc.data.auth.AuthResponse
import com.correct.correctsoc.data.auth.ConfirmPhoneBody
import com.correct.correctsoc.data.auth.GenerateOTPBody
import com.correct.correctsoc.data.auth.LoginBody
import com.correct.correctsoc.data.auth.forget.ForgotResponse
import com.correct.correctsoc.data.auth.RegisterBody
import com.correct.correctsoc.data.auth.ResetPasswordBody
import com.correct.correctsoc.data.auth.SignOutBody
import com.correct.correctsoc.data.auth.UpdatePasswordBody
import com.correct.correctsoc.data.auth.UpdatePhoneBody
import com.correct.correctsoc.data.auth.UpdateUsernameBody
import com.correct.correctsoc.data.auth.ValidateOTPBody
import com.correct.correctsoc.helper.Constants.STATUS
import com.correct.correctsoc.helper.Constants.TOKEN_KEY
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(
        RetrofitClient.getClient().create(APIService::class.java)
    )
    private val workManager = WorkManager.getInstance(application)

    private val _registerResponse = MutableLiveData<AuthResponse>()
    private val _OTPResponse = MutableLiveData<AuthResponse>()
    private val _forgetResponse = MutableLiveData<ForgotResponse>()
    private val _resetPasswordResponse = MutableLiveData<AuthResponse>()
    private val _loginResponse = MutableLiveData<AuthResponse>()
    private val _signOutResponse = MutableLiveData<AuthResponse>()
    private val _updateUsernameResponse = MutableLiveData<AuthResponse>()
    private val _generateOTPResponse = MutableLiveData<AuthResponse>()
    private val _updatePhoneResponse = MutableLiveData<AuthResponse>()
    private val _updatePasswordResponse = MutableLiveData<AuthResponse>()
    private val _validateTokenResponse = MutableLiveData<Boolean>()
    private val _validateOTPResponse = MutableLiveData<AuthResponse>()
    private val _changeDeviceStatus = MutableLiveData<Boolean>()
    private val _deleteAccountResponse = MutableLiveData<ForgotResponse>()

    val deleteAccountResponse:LiveData<ForgotResponse> get() = _deleteAccountResponse
    val changeDeviceStatus: LiveData<Boolean> get() = _changeDeviceStatus
    val validateOTPResponse: LiveData<AuthResponse> get() = _validateOTPResponse
    val validateTokenResponse: LiveData<Boolean> get() = _validateTokenResponse
    val updatePasswordResponse: LiveData<AuthResponse> get() = _updatePasswordResponse
    val updatePhoneResponse: LiveData<AuthResponse> get() = _updatePhoneResponse
    val generateOTPResponse: LiveData<AuthResponse> get() = _generateOTPResponse
    val updateUsernameResponse: LiveData<AuthResponse> get() = _updateUsernameResponse
    val signOutResponse: LiveData<AuthResponse> get() = _signOutResponse
    val loginResponse: LiveData<AuthResponse> get() = _loginResponse
    val resetPasswordResponse: LiveData<AuthResponse> get() = _resetPasswordResponse
    val forgetResponse: LiveData<ForgotResponse> get() = _forgetResponse
    val otpResponse: LiveData<AuthResponse> get() = _OTPResponse
    val registerResponse: LiveData<AuthResponse> get() = _registerResponse


    fun registerUser(body: RegisterBody) = viewModelScope.launch {
        val result = authRepository.registerUser(body)
        if (result.isSuccessful) {
            // register successful
            _registerResponse.postValue(result.body())
        } else {
            // failed to register
            _registerResponse.postValue(result.body())
        }
    }

    fun confirmOTP(body: ConfirmPhoneBody) = viewModelScope.launch {
        val result = authRepository.confirmOTP(body)
        if (result.isSuccessful) {
            _OTPResponse.postValue(result.body())
        } else {
            _OTPResponse.postValue(result.body())
        }
    }

    fun resendOTP(phoneNumber: String) = viewModelScope.launch {
        val result = authRepository.resendOTP(phoneNumber)
        if (result.isSuccessful) {
            _OTPResponse.postValue(result.body())
        } else {
            _OTPResponse.postValue(result.body())
        }
    }

    fun forgetPassword(phoneNumber: String) = viewModelScope.launch {
        val result = authRepository.forgetPassword(phoneNumber)
        if (result.isSuccessful) {
            _forgetResponse.postValue(result.body())
        } else {
            _forgetResponse.postValue(result.body())
        }
    }

    fun resetPassword(body: ResetPasswordBody, token: String) = viewModelScope.launch {
        val result = authRepository.resetPassword(body, token)
        if (result.isSuccessful) {
            _resetPasswordResponse.postValue(result.body())
        } else {
            _resetPasswordResponse.postValue(result.body())
        }
    }

    fun login(body: LoginBody) = viewModelScope.launch {
        val result = authRepository.login(body)
        if (result.isSuccessful) {
            _loginResponse.postValue(result.body())
        } else {
            _loginResponse.postValue(result.body())
        }
    }

    fun signOut(body: SignOutBody, token: String) = viewModelScope.launch {
        val result = authRepository.signOut(body, token)
        if (result.isSuccessful) {
            _signOutResponse.postValue(result.body())
        } else {
            _signOutResponse.postValue(result.body())
        }
    }

    fun updateUsername(body: UpdateUsernameBody, token: String) = viewModelScope.launch {
        val result = authRepository.updateUsername(body, token)
        if (result.isSuccessful) {
            _updateUsernameResponse.postValue(result.body())
        } else {
            _updateUsernameResponse.postValue(result.body())
        }
    }

    fun generateOTP(body: GenerateOTPBody, token: String) = viewModelScope.launch {
        val result = authRepository.generateOTPUpdatePhone(body, token)
        if (result.isSuccessful) {
            _generateOTPResponse.postValue(result.body())
        } else {
            _generateOTPResponse.postValue(result.body())
        }
    }

    fun updatePhone(body: UpdatePhoneBody, token: String) = viewModelScope.launch {
        val result = authRepository.updatePhone(body, token)
        if (result.isSuccessful) {
            _updatePhoneResponse.postValue(result.body())
        } else {
            _updatePhoneResponse.postValue(result.body())
        }
    }

    fun updatePassword(body: UpdatePasswordBody, token: String) = viewModelScope.launch {
        val result = authRepository.updatePassword(body, token)
        if (result.isSuccessful) {
            _updatePasswordResponse.postValue(result.body())
        } else {
            _updatePasswordResponse.postValue(result.body())
        }
    }

    fun validateToken(token: String) = viewModelScope.launch {
        val result = authRepository.validateToken(token)
        _validateTokenResponse.value = result.isSuccessful
    }

    fun validateOTP(body: ValidateOTPBody) = viewModelScope.launch {
        val result = authRepository.validateOTP(body)
        if (result.isSuccessful) {
            _validateOTPResponse.postValue(result.body())
        } else {
            _validateOTPResponse.postValue(result.body())
        }
    }

    fun setDeviceOn(token: String) = viewModelScope.launch {
        val result = authRepository.setDeviceOn(token)
        _changeDeviceStatus.postValue(result.isSuccessful)
    }

    fun setDeviceOff(token: String) = viewModelScope.launch {
        val result = authRepository.setDeviceOff(token)
        _changeDeviceStatus.postValue(result.isSuccessful)
    }

    fun deleteAccount(userID: String, token: String) = viewModelScope.launch {
        val result = authRepository.deleteAccount(userID,token)
        _deleteAccountResponse.postValue(result.body())
    }

}