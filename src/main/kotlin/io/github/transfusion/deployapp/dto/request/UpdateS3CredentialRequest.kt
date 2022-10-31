package io.github.transfusion.deployapp.dto.request

data class UpdateS3CredentialRequest(
    var server: String?,
    var awsRegion: String?,
    val accessKey: String = "",
    val secretKey: String = "",
    val bucket: String = "",
    val name: String = "",

    val skipTestPublicAccess: Boolean = false,
)