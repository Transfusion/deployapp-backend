package io.github.transfusion.deployapp.dto.response

import io.github.transfusion.deployapp.dto.internal.S3TestResult


data class S3CreateResultDTO(
    var success: Boolean,
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
) {
    constructor(s3TestResult: S3TestResult) : this(
        s3TestResult.testHeadBucketSuccess &&
                !(!s3TestResult.skipTestPublicAccess && s3TestResult.testPublicAccessSuccess == false) &&
                s3TestResult.testSignedLinkSuccess,

        s3TestResult.testHeadBucketSuccess,
        s3TestResult.testHeadBucketError,
        s3TestResult.skipTestPublicAccess,
        s3TestResult.testPublicAccessSuccess,
        s3TestResult.testPublicAccessError,
        s3TestResult.testSignedLinkSuccess,
        s3TestResult.testSignedLinkError,
    )
}