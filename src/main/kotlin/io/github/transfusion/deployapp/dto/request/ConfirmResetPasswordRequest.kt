package io.github.transfusion.deployapp.dto.request

import java.util.UUID

data class ConfirmResetPasswordRequest(
    val token: UUID,
    val password: String
)
