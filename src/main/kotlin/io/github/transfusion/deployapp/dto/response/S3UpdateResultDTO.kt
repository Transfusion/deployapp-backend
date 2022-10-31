package io.github.transfusion.deployapp.dto.response

//typealias S3UpdateResultDTO = S3CreateResultDTO;
//kotlin typealiases can't be used in java
import io.github.transfusion.deployapp.dto.internal.S3TestResult
import java.util.*


data class S3UpdateResultDTO(
    var success: Boolean,
    var id: UUID?,
    var testHeadBucketSuccess: Boolean = false,
    // error msg if the above is false
    var testHeadBucketError: String? = null,

    var skipTestPublicAccess: Boolean = false,
//    var testInitializePublicPolicySuccess: Boolean,
    var testPublicAccessSuccess: Boolean? = null,
    // error msg if the above is false
    var testPublicAccessError: String? = null,

    var testSignedLinkSuccess: Boolean = false,
    // error msg if the above is false
    var testSignedLinkError: String? = null,

    var credential: S3CredentialDTO? = null,
) {
    constructor(overallSuccess: Boolean, id: UUID?, s3TestResult: S3TestResult, dto: S3CredentialDTO?) : this(
        overallSuccess,
        id,
        s3TestResult.testHeadBucketSuccess,
        s3TestResult.testHeadBucketError,
        s3TestResult.skipTestPublicAccess,
        s3TestResult.testPublicAccessSuccess,
        s3TestResult.testPublicAccessError,
        s3TestResult.testSignedLinkSuccess,
        s3TestResult.testSignedLinkError,

        dto
    )
}