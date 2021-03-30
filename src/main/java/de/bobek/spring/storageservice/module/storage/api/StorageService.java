package de.bobek.spring.storageservice.module.storage.api;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StorageService {

    Page<StorageItem> list(String username, Pageable pageable);

    StorageItem get(String username, String id) throws StorageItemNotFoundException;

    StorageItem add(AddStorageItemData data, InputStream content) throws IOException;

    void delete(String username, String id) throws StorageItemNotFoundException;
}
