package io.github.transfusion.deployapp.dto.request

data class RegisterRequest(
    val email: String,
    val password: String,
    // the base url to use in the confirmation email
    val redirectBaseUrl: String,
)
