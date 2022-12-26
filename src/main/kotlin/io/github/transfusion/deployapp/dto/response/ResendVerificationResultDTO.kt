package io.github.transfusion.deployapp.dto.response

data class ResendVerificationResultDTO(
    val success: Boolean,
    val email_changed: Boolean
)
