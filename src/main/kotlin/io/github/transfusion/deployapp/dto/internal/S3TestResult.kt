package io.github.transfusion.deployapp.dto.internal

data class S3TestResult(
//    var success: Boolean,
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

//    var msg: String
)
