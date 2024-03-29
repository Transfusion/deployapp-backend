package io.github.transfusion.deployapp.services;

import io.github.transfusion.deployapp.db.entities.FtpCredential;
import io.github.transfusion.deployapp.dto.internal.FtpTestResult;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.naming.AuthenticationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static io.github.transfusion.deployapp.db.entities.FtpCredential.PUBLIC_PREFIX;

@Service
public class FtpTesterService {

    private FTPClient getFTPClient(FtpCredential ftpCreds) throws IOException, AuthenticationException {
        FTPClient client = new FTPClient();
        client.setConnectTimeout(60000);
        client.connect(ftpCreds.getServer(), ftpCreds.getPort());
        // https://stackoverflow.com/questions/29848713/how-to-use-socket-setsotimeout
        client.setSoTimeout(60000);
        client.setDataTimeout(60000);
        // https://stackoverflow.com/questions/21294253/commons-ftpclient-storefile-hangs-if-ftp-server-becomes-unavailable
        client.setControlKeepAliveTimeout(60); // 60 SECONDS

        if (!client.login(ftpCreds.getUsername(), ftpCreds.getPassword()))
            throw new AuthenticationException(String.format("Login failed to server %s port %d", ftpCreds.getServer(), ftpCreds.getPort()));

        client.setFileType(FTP.BINARY_FILE_TYPE);
        client.enterLocalPassiveMode(); // default to PASV mode
        return client;
    }

    /**
     * @param ftpCreds {@link FtpCredential}
     * @return True if files which are written to the given FTP directory can be accessed from the given base url
     */
    @Async
    public CompletableFuture<FtpTestResult> test(FtpCredential ftpCreds) {
        FTPClient client;
        FtpTestResult result = new FtpTestResult();

        try {
            client = getFTPClient(ftpCreds);
            result.setTestConnectionSuccess(true);
        } catch (Exception e) {
            // all failed.
            result.setTestConnectionError(e.getLocalizedMessage());
            return CompletableFuture.completedFuture(result);
        }

        final String random = UUID.randomUUID().toString();

        try {
            client.makeDirectory(ftpCreds.getDirectory());
            client.makeDirectory(String.format("%s/%s", ftpCreds.getDirectory(), PUBLIC_PREFIX));

            String testPath = String.format("%s/%s%s", ftpCreds.getDirectory(), PUBLIC_PREFIX, random);

            client.storeFile(testPath, new ByteArrayInputStream(random.getBytes(StandardCharsets.UTF_8)));
            result.setTestWriteFolderSuccess(true);
        } catch (IOException e) {
            result.setTestWriteFolderSuccess(false);
            result.setTestWriteFolderError(e.getLocalizedMessage());
            return CompletableFuture.completedFuture(result);
        }

        try {
            // attempt to download it
            URL publicUrl = new URL(ftpCreds.getBaseUrl() + '/' + PUBLIC_PREFIX + random);

            RestTemplate restTemplate = new RestTemplate();
            URI uri = publicUrl.toURI();
            ResponseEntity<String> downloadResult = restTemplate.getForEntity(uri, String.class);

            if (downloadResult.getStatusCodeValue() != 200 || !Objects.equals(downloadResult.getBody(), random))
                throw new IllegalArgumentException(String.format("Make sure objects in directory %s at base url %s are publicly accessible.",
                        ftpCreds.getDirectory(), ftpCreds.getBaseUrl()));

            // and then delete it
            client.deleteFile(String.format("%s/%s%s", ftpCreds.getDirectory(), PUBLIC_PREFIX, random));
            client.disconnect();

            result.setTestPublicAccessSuccess(true);
        } catch (Exception e) {
            result.setTestPublicAccessSuccess(false);
            result.setTestPublicAccessError(e.getLocalizedMessage());
        }

        return CompletableFuture.completedFuture(result);
    }
}
