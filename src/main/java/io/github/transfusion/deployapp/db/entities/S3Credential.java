package io.github.transfusion.deployapp.db.entities;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "s3_credentials")
@PrimaryKeyJoinColumn(name = "id")
public class S3Credential extends StorageCredential {
    public static final String IDENTIFIER = "S3";
    public static String PUBLIC_PREFIX = "public/";
//    @Id
//    @Column(name = "id", nullable = false)
//    private UUID id;

    @Column(name = "server", nullable = false)
    private String server;

    @Column(name = "aws_region", length = 30)
    private String awsRegion;

    @Column(name = "access_key", nullable = false, length = 30)
    private String accessKey;

    @Column(name = "secret_key", nullable = false, length = 50)
    private String secretKey;

    @Column(name = "bucket", nullable = false, length = 100)
    private String bucket;

//    public UUID getId() {
//        return id;
//    }
//
//    public void setId(UUID id) {
//        this.id = id;
//    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }
}