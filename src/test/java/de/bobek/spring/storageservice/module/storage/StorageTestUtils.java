package de.bobek.spring.storageservice.module.storage;

import java.io.InputStream;
import java.time.Instant;

import de.bobek.spring.storageservice.module.storage.api.Metadata;
import de.bobek.spring.storageservice.module.storage.api.StorageItem;
import de.bobek.spring.storageservice.module.storage.web.FileInfo;

public final class StorageTestUtils {

    private StorageTestUtils() {
    }

    public static StorageItem getStorageItem() {
        return new StorageItem(getMetadata(), InputStream::nullInputStream);
    }

    public static Metadata getMetadata() {
        return Metadata.builder()
                .id("123")
                .size(256L)
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();
    }

    public static FileInfo getFileInfo() {
        return new FileInfo()
                .setId("123");
    }
}
