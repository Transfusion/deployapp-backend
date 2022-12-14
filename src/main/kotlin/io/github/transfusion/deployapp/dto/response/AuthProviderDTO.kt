package io.github.transfusion.deployapp.dto.response

import java.util.*

data class AuthProviderDTO(
    val userId: UUID,
    val providerKey: String,
    val providerName: String,
    val providerInfoName: String?
)
