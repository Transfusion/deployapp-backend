package io.github.transfusion.deployapp.services;

import io.github.transfusion.deployapp.dto.internal.MigrateAnonymousAppBinariesEvent;
import io.github.transfusion.deployapp.messaging.IntegrationEventsSender;
import io.github.transfusion.deployapp.session.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MigrationService {

    @Autowired
    private StorageCredentialsService storageCredentialsService;

    @Autowired
    private IntegrationEventsSender integrationEventsSender;

    @Autowired
    private SessionData sessionData;

    public void migrateAnonymousData(UUID userId) {
        storageCredentialsService.migrateAnonymousCredentials(userId);
        if (!sessionData.getAnonymousAppBinaries().isEmpty()) {
            MigrateAnonymousAppBinariesEvent event =
                    new MigrateAnonymousAppBinariesEvent(userId, sessionData.getAnonymousAppBinaries());
            integrationEventsSender.send(event);
        }
    }
}
