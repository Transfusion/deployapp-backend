package io.github.transfusion.deployapp.dto.internal

import java.io.Serializable
import java.util.UUID

data class DeleteStorageCredentialEvent(
    val id: UUID,
) : Serializable
