package io.github.transfusion.deployapp.controller;

import io.github.transfusion.deployapp.dto.response.AWSRegionDTO;
import io.github.transfusion.deployapp.dto.response.PublicUserProfileDTO;
import io.github.transfusion.deployapp.mappers.AWSRegionMapper;
import io.github.transfusion.deployapp.mappers.PublicProfileMapper;
import io.github.transfusion.deployapp.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.Region;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/utility/public")
public class PublicUtilityController {

    @Autowired
    private AWSRegionMapper awsRegionMapper;

    @GetMapping("/S3Regions")
    public ResponseEntity<List<AWSRegionDTO>> s3Regions() {
        return new ResponseEntity<>(Region.regions().stream()
                .map(awsRegionMapper::mapAWSRegionToDTO)
                .collect(Collectors.toList()), HttpStatus.OK);
    }

    @Autowired
    private AccountService accountService;

    @Autowired
    private PublicProfileMapper publicProfileMapper;

    @GetMapping("/user/{id}")
    public ResponseEntity<PublicUserProfileDTO> getPublicUserProfile(@PathVariable("id") UUID id) {
        return new ResponseEntity<>(publicProfileMapper.toDTO(accountService.getUserById(id)), HttpStatus.OK);
    }

    // TODO: public endpoint for organization details
}
