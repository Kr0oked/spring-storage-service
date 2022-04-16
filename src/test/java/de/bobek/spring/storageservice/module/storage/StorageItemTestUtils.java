package de.bobek.spring.storageservice.module.storage;

import java.io.InputStream;

import de.bobek.spring.storageservice.module.storage.api.StorageItem;

import static de.bobek.spring.storageservice.module.metadata.api.MetadataTestUtils.getMetadata;

public final class StorageItemTestUtils {

    private StorageItemTestUtils() {
    }

    public static StorageItem getStorageItem() {
        return new StorageItem(getMetadata(), InputStream::nullInputStream);
    }
}
