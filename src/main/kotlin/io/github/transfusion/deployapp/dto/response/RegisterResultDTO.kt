package io.github.transfusion.deployapp.dto.response

data class RegisterResultDTO(
    val success: Boolean,
    val already_registered: Boolean,
    val pending_verification: Boolean,
)
