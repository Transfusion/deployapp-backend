package io.github.transfusion.deployapp.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.transfusion.deployapp.db.entities.S3Credential;
import io.github.transfusion.deployapp.dto.internal.S3TestResult;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static io.github.transfusion.deployapp.db.entities.S3Credential.CUSTOM_AWS_REGION;
import static io.github.transfusion.deployapp.db.entities.S3Credential.PUBLIC_PREFIX;

@Service
public class S3TesterService {

    Logger logger = LoggerFactory.getLogger(S3TesterService.class);

//    private CompletableFuture<Region> getBucketRegion(AwsCredentialsProvider credentialsProvider, String bucketName) {
//        return null;
//    }

    @Value("classpath:snippets/put_bucket_public_policy.json")
    Resource publicPolicyFile;

    @Value("classpath:snippets/put_bucket_public_policy_statement.json")
    Resource publicPolicyStatementFile;

    /**
     * Grants public read access to the /public prefix by setting a bucket policy
     * https://aws.amazon.com/premiumsupport/knowledge-center/read-access-objects-s3-bucket/
     *
     * @return the {@link PutBucketPolicyResponse}
     */
    public CompletableFuture<PutBucketPolicyResponse> initializePublicPolicy(S3Client s3Client, S3Credential s3Creds) throws IOException {

        String existingPolicyJSON = null;
        boolean policyExists;

        // attempt to get the existing policy first
        try {
            GetBucketPolicyResponse getBucketPolicyResponse = s3Client.getBucketPolicy(GetBucketPolicyRequest.builder()
                    .bucket(s3Creds.getBucket())
                    .build());
            existingPolicyJSON = getBucketPolicyResponse.policy();
            policyExists = true;
        } catch (S3Exception e) {
            int statusCode = e.awsErrorDetails().sdkHttpResponse().statusCode();
            // rethrow exception if other than "doesn't exist"
            if (statusCode != 404) throw e;
            policyExists = false;
        }

        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("bucket", s3Creds.getBucket());
        valuesMap.put("prefix", PUBLIC_PREFIX);
        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        String newPolicyJSON;
        if (policyExists) {
            // parse the existing JSON and add the new statement to it
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> policyJSONMap = objectMapper
                    .readValue(existingPolicyJSON, new TypeReference<>() {
                    });
            var policyStatementList = (List<LinkedHashMap<String, String>>) policyJSONMap.get("Statement");

            String bucketPublicPolicyStatementTemplate = new String(Files.readAllBytes(publicPolicyStatementFile
                    .getFile().toPath()));

            String policyStatement = stringSubstitutor.replace(bucketPublicPolicyStatementTemplate);

            var newStatement = objectMapper
                    .readValue(policyStatement, LinkedHashMap.class);
            policyStatementList.add(newStatement);
            newPolicyJSON = objectMapper.writeValueAsString(policyJSONMap);
        } else {
            String bucketPublicPolicyTemplate = new String(Files.readAllBytes(publicPolicyFile
                    .getFile().toPath()));

            newPolicyJSON = stringSubstitutor.replace(bucketPublicPolicyTemplate);
        }

        PutBucketPolicyRequest.Builder builder = PutBucketPolicyRequest.builder()
                .bucket(s3Creds.getBucket())
                .policy(newPolicyJSON);

        PutBucketPolicyRequest policyRequest = builder.build();
        PutBucketPolicyResponse resp = s3Client.putBucketPolicy(policyRequest);

        return CompletableFuture.completedFuture(resp);
    }

    /**
     * Internal method that tests whether we have a publicly accesible prefix in the bucket
     * Prerequisite: initializePublicPolicy has already been called.
     *
     * @param s3Creds {@link S3Credential}
     * @param client  S3 client already built using the builder
     * @return the resulting {@link ResponseEntity} of getting the random string
     */
    private CompletableFuture<ResponseEntity<String>> testPublicAccess(S3Client client, S3Credential s3Creds) {

        final String random = UUID.randomUUID().toString();
        final String key = PUBLIC_PREFIX + random;

        // now try to upload a random string there
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(s3Creds.getBucket())
                .key(key)
                .build();

        client.putObject(objectRequest, RequestBody.fromBytes(random.getBytes(StandardCharsets.UTF_8)));

        // ensure that it is publicly readable and that signed links work
        // will return something like https://deployapp-dev.s3.ap-southeast-1.amazonaws.com/76e5cbb2-6116-4524-835f-eaf90e3dc79a
        String publicUrl = client.utilities().getUrl(bldr -> bldr.bucket(s3Creds.getBucket()).key(key)).toExternalForm();

        // test publicly accessible (we're going to use this to store assets like icons, images, etc..)
        RestTemplate restTemplate = new RestTemplate();
        URI uri = URI.create(publicUrl);
        ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);

        if (result.getStatusCodeValue() != 200 || !Objects.equals(result.getBody(), random))
            throw new IllegalArgumentException(String.format("Make sure objects in bucket %s in region %s with endpoint %s and prefix %s are publicly accessible.",
                    s3Creds.getBucket(), s3Creds.getAwsRegion(), s3Creds.getServer(), PUBLIC_PREFIX));

        DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(s3Creds.getBucket())
                .delete(Delete.builder().objects(ObjectIdentifier.builder().key(key).build()).build()).build();
        client.deleteObjects(deleteObjectsRequest);

        return CompletableFuture.completedFuture(result);
    }

    /**
     * Internal method that tests whether the desired bucket exists
     *
     * @param client  S3 client already built using the builder
     * @param s3Creds credentials
     * @return HeadBucketResponse
     */
    private CompletableFuture<HeadBucketResponse> testHeadBucket(S3Client client, S3Credential s3Creds) {
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(s3Creds.getBucket())
                .build();

        // throws NoSuchBucketException
        HeadBucketResponse resp = client.headBucket(headBucketRequest);
        return CompletableFuture.completedFuture(resp);
    }

    /**
     * Internal method that tests whether presigned link generation works
     *
     * @param client    S3 client already built using the builder
     * @param preSigner S3 Presigner already built using the builder
     * @param s3Creds   credentials
     * @return
     */
    private CompletableFuture<ResponseEntity<String>> testSignedLink(S3Client client,
                                                                     S3Presigner preSigner,
                                                                     S3Credential s3Creds) {

        final String random = UUID.randomUUID().toString();
        final String key = random;

        // now try to upload a random string there
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(s3Creds.getBucket())
                .key(key)
                .build();

        client.putObject(objectRequest, RequestBody.fromBytes(random.getBytes(StandardCharsets.UTF_8)));

        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(s3Creds.getBucket())
                        .key(key)
                        .build();

        GetObjectPresignRequest getObjectPresignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(3))
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedGetObjectRequest =
                preSigner.presignGetObject(getObjectPresignRequest);

        String presignedUrl = presignedGetObjectRequest.url().toString();
        logger.debug("presigned url generated" + presignedUrl);

        // test publicly accessible (we're going to use this to store assets like icons, images, etc..)
        RestTemplate restTemplate = new RestTemplate();
        URI uri = URI.create(presignedUrl);
        ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);

        if (result.getStatusCodeValue() != 200 || !Objects.equals(result.getBody(), random))
            throw new IllegalArgumentException(String.format("Make sure getting objects via presigned URLs in bucket %s in region %s with endpoint %s is allowed.",
                    s3Creds.getBucket(), s3Creds.getAwsRegion(), s3Creds.getServer()));

        DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(s3Creds.getBucket())
                .delete(Delete.builder().objects(ObjectIdentifier.builder().key(key).build()).build()).build();
        client.deleteObjects(deleteObjectsRequest);

        return CompletableFuture.completedFuture(result);
    }

    /**
     * @param s3Creds                    {@link S3Credential}
     * @param skipInitializePublicAccess set to true if you're using a partially S3-compliant storage and want to grant access to the public/ prefix yourself
     * @return {@link CompletableFuture<S3TestResult>}
     */
    @Async
    public CompletableFuture<S3TestResult> test(S3Credential s3Creds, boolean skipInitializePublicAccess) {

        S3TestResult testResult = new S3TestResult();
        testResult.setSkipTestPublicAccess(skipInitializePublicAccess);

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                s3Creds.getAccessKey(),
                s3Creds.getSecretKey());

        S3Presigner.Builder preSignerBuilder = S3Presigner.builder();
        S3ClientBuilder clientBuilder = S3Client.builder();
        if (s3Creds.getAwsRegion().equals(CUSTOM_AWS_REGION)) {
            String endpoint = s3Creds.getServer().replace("https://", "");
            URI uri = URI.create(String.format("https://%s", endpoint));
            preSignerBuilder.endpointOverride(uri);
            clientBuilder.endpointOverride(uri);
            preSignerBuilder.region(Region.US_EAST_1);
            clientBuilder.region(Region.US_EAST_1); // the default region
        } else {
            preSignerBuilder.region(Region.of(s3Creds.getAwsRegion()));
            clientBuilder.region(Region.of(s3Creds.getAwsRegion()));
        }
        clientBuilder.credentialsProvider(StaticCredentialsProvider.create(awsCreds));
        preSignerBuilder.credentialsProvider(StaticCredentialsProvider.create(awsCreds));

        S3Client client = clientBuilder.build();
        S3Presigner preSigner = preSignerBuilder.build();

        /* ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketsResponse = client.listBuckets(listBucketsRequest);
        listBucketsResponse.buckets().stream().forEach(x -> System.out.println(x.name())); */

        try {
            testHeadBucket(client, s3Creds);
            testResult.setTestHeadBucketSuccess(true);
            testResult.setTestHeadBucketError(null);
        } catch (Exception e) {
            // impossible to proceed; set everything else to failed
            e.printStackTrace();
            testResult.setTestHeadBucketSuccess(false);
            testResult.setTestHeadBucketError(e.getLocalizedMessage());

            testResult.setTestPublicAccessSuccess(false);
            testResult.setTestPublicAccessError(null);

            testResult.setTestSignedLinkSuccess(false);
            testResult.setTestSignedLinkError(null);
            return CompletableFuture.completedFuture(testResult);
        }

        if (!skipInitializePublicAccess) {
            try {
                initializePublicPolicy(client, s3Creds);
                testPublicAccess(client, s3Creds);
                testResult.setTestPublicAccessSuccess(true);
                testResult.setTestPublicAccessError(null);
            } catch (Exception e) {
                e.printStackTrace();
                testResult.setTestPublicAccessSuccess(false);
                testResult.setTestPublicAccessError(e.getLocalizedMessage());
            }
        }

        // now test presigned URL generation
        try {
            testSignedLink(client, preSigner, s3Creds);
            testResult.setTestSignedLinkSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
            testResult.setTestSignedLinkSuccess(false);
            testResult.setTestSignedLinkError(e.getLocalizedMessage());
        }

//        return CompletableFuture.completedFuture(new S3CreateResultDTO(true, String.format("Successfully tested bucket %s in endpoint %s", s3Creds.getBucket(), s3Creds.getServer())));
        return CompletableFuture.completedFuture(testResult);
    }
}
