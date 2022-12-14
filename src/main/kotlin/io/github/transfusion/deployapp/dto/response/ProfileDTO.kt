package io.github.transfusion.deployapp.dto.response

import java.util.*

data class ProfileDTO(
    val authenticated: Boolean,
    val id: UUID?,
    val has_username: Boolean,
    val username: String,
    val name: String?,
    val email: String?,
    val has_password: Boolean,

    val oauth_login: Boolean,
    val oauth_registration_id: String? // null if oauth_login is false
)
