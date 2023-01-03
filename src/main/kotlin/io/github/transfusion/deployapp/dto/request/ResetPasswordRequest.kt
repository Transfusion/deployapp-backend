package io.github.transfusion.deployapp.dto.request

data class ResetPasswordRequest(
    val email: String,
    val redirectBaseUrl: String,
)
