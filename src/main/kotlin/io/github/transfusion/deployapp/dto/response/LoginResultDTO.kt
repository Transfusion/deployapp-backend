package io.github.transfusion.deployapp.dto.response

import java.util.UUID

data class LoginResultDTO(
    val success: Boolean,
    val userId: UUID?,
    val badCredentials: Boolean?,
    val disabled: Boolean?,
    val error: String?
)
