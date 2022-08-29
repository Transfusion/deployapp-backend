package io.github.transfusion.deployapp.dto.response

data class AWSRegionDTO(
    val isGlobalRegion: Boolean,
    val id: String,
    val description: String,
)