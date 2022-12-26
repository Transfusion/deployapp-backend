package io.github.transfusion.deployapp.dto.internal

import java.util.UUID

data class SendVerificationEmailEvent(
    val userId: UUID, // in case we decouple the AccountVerificationService into a microservice
    val email: String,

    val redirectBaseUrl: String,
    val token: String,
)
