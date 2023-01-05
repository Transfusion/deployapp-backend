package io.github.transfusion.deployapp.dto.internal

import java.io.Serializable
import java.util.UUID

data class MigrateAnonymousAppBinariesEvent(
    val userId: UUID,
    val anonymousAppBinaries: Set<UUID>
    // tell the storage management service to assign all these appbinaries to the given user
) : Serializable