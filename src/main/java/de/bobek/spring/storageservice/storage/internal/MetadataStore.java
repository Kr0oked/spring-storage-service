package de.bobek.spring.storageservice.storage.internal;

import java.util.Optional;

import de.bobek.spring.storageservice.storage.api.AddStorageItemData;
import de.bobek.spring.storageservice.storage.api.Metadata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MetadataStore {

    Page<Metadata> list(String username, Pageable pageable);

    Optional<Metadata> find(String username, String id);

    Metadata store(AddStorageItemData data);

    Optional<Metadata> delete(String username, String id);
}
