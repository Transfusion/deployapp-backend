package io.github.transfusion.deployapp.dto.request

data class ResendVerificationRequest(
    val email: String,
    val newEmail: String?,
    // the base url to use in the confirmation email
    val redirectBaseUrl: String,
)
