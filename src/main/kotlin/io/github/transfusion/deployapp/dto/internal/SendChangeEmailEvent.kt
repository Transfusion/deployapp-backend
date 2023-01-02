package io.github.transfusion.deployapp.dto.internal

import java.util.*

data class SendChangeEmailEvent(
    val userId: UUID, // in case we decouple the AccountVerificationService into a microservice
    val email: String,
    val newEmail: String,

    val redirectBaseUrl: String,
    val token: String,
)
