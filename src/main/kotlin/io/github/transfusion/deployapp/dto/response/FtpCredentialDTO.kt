package io.github.transfusion.deployapp.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant
import java.util.*

data class FtpCredentialDTO(
    val server: String,
    val port: Number,
    val username: String,
    val password: String,
    val directory: String,
    val baseUrl: String,

    override val id: UUID,
    override val name: String?,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING)
    override val createdOn: Instant?,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING)
    override val checkedOn: Instant?,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING)
    override val lastUsed: Instant?,
    override val type: String,

    ) : StorageCredentialDTO