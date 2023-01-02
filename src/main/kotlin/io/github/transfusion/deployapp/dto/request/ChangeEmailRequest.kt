package io.github.transfusion.deployapp.dto.request

data class ChangeEmailRequest(
    val email: String,

    // the base url to use in the confirmation email
    val redirectBaseUrl: String,
)
