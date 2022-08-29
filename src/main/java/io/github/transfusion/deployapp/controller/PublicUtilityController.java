package io.github.transfusion.deployapp.controller;

import io.github.transfusion.deployapp.dto.response.AWSRegionDTO;
import io.github.transfusion.deployapp.mappers.AWSRegionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.Region;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/utility/public")
public class PublicUtilityController {

    @Autowired
    private AWSRegionMapper awsRegionMapper;

    @GetMapping("/S3Regions")
    public ResponseEntity<List<AWSRegionDTO>> profile() {
        return new ResponseEntity<>(Region.regions().stream()
                .map(awsRegionMapper::mapAWSRegionToDTO)
                .collect(Collectors.toList()), HttpStatus.OK);
    }
}
