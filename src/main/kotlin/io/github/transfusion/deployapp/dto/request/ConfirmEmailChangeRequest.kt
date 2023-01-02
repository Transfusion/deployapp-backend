package io.github.transfusion.deployapp.dto.request

import java.util.UUID

data class ConfirmEmailChangeRequest(
    val token: UUID,
    val email: String,
)
