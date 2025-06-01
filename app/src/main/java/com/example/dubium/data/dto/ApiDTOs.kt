// Exactamente iguales a los del backend pero con @SerializedName
package com.example.dubium.data.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("token") val token: String?,
    @SerializedName("user") val user: UserDto?,
    @SerializedName("message") val message: String?
)

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("createdAt") val createdAt: String
)

data class ErrorResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String?
)