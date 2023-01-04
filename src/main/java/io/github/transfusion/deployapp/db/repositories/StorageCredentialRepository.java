package io.github.transfusion.deployapp.db.repositories;

import io.github.transfusion.deployapp.db.entities.StorageCredential;
import io.github.transfusion.deployapp.db.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

public interface StorageCredentialRepository extends PagingAndSortingRepository<StorageCredential, UUID> {
    @Query("SELECT s from StorageCredential s where s.id in :ids AND TYPE(s) in :classes")
    Page<StorageCredential> findByIdIn(Collection<UUID> ids, Collection<Class<? extends StorageCredential>> classes, Pageable pageable);

    @Query("SELECT s from StorageCredential s where s.id in :ids AND s.name LIKE %:name% AND TYPE(s) in :classes")
    Page<StorageCredential> findByIdInLikeName(Collection<UUID> ids, String name, Collection<Class<? extends StorageCredential>> classes, Pageable pageable);
//    Page<StorageCredential> findByIdInAndNameContainingIgnoreCase(Collection<UUID> ids, String name, Pageable pageable);

    @Query("SELECT s from StorageCredential s where s.user.id = :id AND TYPE(s) in :classes")
    Page<StorageCredential> findAllByUserId(UUID id, Collection<Class<? extends StorageCredential>> classes, Pageable pageable);
//    Page<StorageCredential> findAllByUserIdAndNameContainingIgnoreCase(UUID id, String name, Pageable pageable);

    @Query("SELECT s from StorageCredential s where s.user.id = :id AND s.name LIKE %:name% AND TYPE(s) in :classes")
    Page<StorageCredential> findAllByUserIdLikeName(UUID id, String name, Collection<Class<? extends StorageCredential>> classes, Pageable pageable);

//    Page<StorageCredential> findAllByUserIdAndNameContainingIgnoreCase(UUID id, String name, List<Class<? extends StorageCredential>> classes, Pageable pageable);
    @Modifying
    @Transactional
    @Query("UPDATE StorageCredential s set s.user = :user WHERE s.id in :ids")
    int migrateAnonymousStorageCredentials(User user, Collection<UUID> ids);
}
