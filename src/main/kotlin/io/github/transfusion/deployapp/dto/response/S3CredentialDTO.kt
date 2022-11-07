package io.github.transfusion.deployapp.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import java.io.Serializable
import java.time.Instant
import java.util.*

data class S3CredentialDTO(
    val server: String,
    val awsRegion: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String,

    override val id: UUID,
    override val name: String?,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING)
    override val createdOn: Instant,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING)
    override val checkedOn: Instant,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING)
    override val lastUsed: Instant?,
    override val type: String,

    ) : StorageCredentialDTO, Serializable;