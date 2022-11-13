package io.github.transfusion.deployapp.dto.response

//typealias FtpUpdateResultDTO = FtpCreateResultDTO;
//kotlin typealiases can't be used in java
import io.github.transfusion.deployapp.dto.internal.FtpTestResult
import java.util.*

data class FtpUpdateResultDTO(
    var success: Boolean,
    var id: UUID?,

    var testConnection: Boolean = false,
    var testConnectionError: String? = null,

    var testWriteFolder: Boolean = false,
    var testWriteFolderError: String? = null,

    var testPublicAccess: Boolean = false,
    var testPublicAccessError: String? = null,


    var credential: FtpCredentialDTO? = null,
) {
    constructor(overallSuccess: Boolean, id: UUID?, ftpTestResult: FtpTestResult, dto: FtpCredentialDTO?) : this(
        overallSuccess,
        id,

        ftpTestResult.testConnectionSuccess,
        ftpTestResult.testConnectionError,

        ftpTestResult.testWriteFolderSuccess,
        ftpTestResult.testWriteFolderError,

        ftpTestResult.testPublicAccessSuccess,
        ftpTestResult.testPublicAccessError,

        dto
    )
}
