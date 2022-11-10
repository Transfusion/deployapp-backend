package io.github.transfusion.deployapp.dto.internal

data class FtpTestResult(
    var testConnectionSuccess: Boolean = false,
    var testConnectionError: String? = null,

    var testWriteFolderSuccess: Boolean = false,
    var testWriteFolderError: String? = null,

    var testPublicAccessSuccess: Boolean = false,
    var testPublicAccessError: String? = null,
)
