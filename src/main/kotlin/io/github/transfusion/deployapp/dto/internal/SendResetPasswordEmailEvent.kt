package io.github.transfusion.deployapp.dto.internal

data class SendResetPasswordEmailEvent(
    val email: String,
    val redirectBaseUrl: String,
    val token: String
)
