package io.github.transfusion.deployapp.mappers;

import io.github.transfusion.deployapp.dto.response.AWSRegionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.RegionMetadata;

@Mapper(
        componentModel = "spring"
)
public interface AWSRegionMapper {

    @Named("getRegionDescription")
    static String getRegionDescription(Region r) {
        RegionMetadata m = RegionMetadata.of(r);
        if (m == null) return r.id();
        return m.description();
    }

    @Mapping(expression = "java(r.id())", target = "id")
    @Mapping(expression = "java(r.isGlobalRegion())", target = "isGlobalRegion")
    @Mapping(source = "r", target = "description", qualifiedByName = "getRegionDescription")
    AWSRegionDTO mapAWSRegionToDTO(Region r);
}
