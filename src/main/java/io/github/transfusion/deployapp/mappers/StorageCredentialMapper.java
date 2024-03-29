package io.github.transfusion.deployapp.mappers;

import io.github.transfusion.deployapp.db.entities.FtpCredential;
import io.github.transfusion.deployapp.db.entities.S3Credential;
import io.github.transfusion.deployapp.db.entities.StorageCredential;
import io.github.transfusion.deployapp.dto.request.CreateFtpCredentialRequest;
import io.github.transfusion.deployapp.dto.request.CreateS3CredentialRequest;
import io.github.transfusion.deployapp.dto.request.UpdateFtpCredentialRequest;
import io.github.transfusion.deployapp.dto.request.UpdateS3CredentialRequest;
import io.github.transfusion.deployapp.dto.response.FtpCredentialDTO;
import io.github.transfusion.deployapp.dto.response.S3CredentialDTO;
import io.github.transfusion.deployapp.dto.response.StorageCredentialDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
//import org.mapstruct.factory.Mappers;


@Mapper(
        componentModel = "spring"
)
public interface StorageCredentialMapper {
    default StorageCredentialDTO toDTO(StorageCredential s) {
        if (s instanceof S3Credential) {
            return mapS3CredentialToDTO((S3Credential) s);
        } else if (s instanceof FtpCredential) {
            return mapFtpCredentialToDTO((FtpCredential) s);
        } else {
            return null;
        }
    }

//    StorageCredentialMapper instance = Mappers.getMapper(StorageCredentialMapper.class);

    @Mapping(target = "type", constant = S3Credential.IDENTIFIER)
    S3CredentialDTO mapS3CredentialToDTO(S3Credential s);

    void updateS3CredentialFromCreateRequest(CreateS3CredentialRequest request, @MappingTarget S3Credential s);
    void updateS3CredentialFromUpdateRequest(UpdateS3CredentialRequest request, @MappingTarget S3Credential s);

    @Mapping(target = "type", constant = FtpCredential.IDENTIFIER)
    FtpCredentialDTO mapFtpCredentialToDTO(FtpCredential s);

    void updateFtpCredentialFromCreateRequest(CreateFtpCredentialRequest request, @MappingTarget FtpCredential s);
    void updateFtpCredentialFromUpdateRequest(UpdateFtpCredentialRequest request, @MappingTarget FtpCredential s);
}
