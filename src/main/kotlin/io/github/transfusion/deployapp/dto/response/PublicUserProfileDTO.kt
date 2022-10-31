package io.github.transfusion.deployapp.dto.response

import java.util.*

data class PublicUserProfileDTO(
    val id: UUID,
    val username: String?,
    val name: String?,
    val email: String?
)
