package io.github.transfusion.deployapp.dto.request

data class CreateFtpCredentialRequest(
    val name: String = "",
    val server: String,
    val port: Int,
    val username: String,
    val password: String,
    val directory: String,
    val baseUrl: String,
)
