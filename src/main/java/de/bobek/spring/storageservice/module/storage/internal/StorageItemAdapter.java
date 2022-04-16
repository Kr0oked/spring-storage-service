package de.bobek.spring.storageservice.module.storage.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import de.bobek.spring.storageservice.module.content.api.ContentStore;
import de.bobek.spring.storageservice.module.metadata.api.Metadata;
import de.bobek.spring.storageservice.module.storage.api.StorageItem;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StorageItemAdapter {

    @NonNull
    private final ContentStore contentStore;

    public StorageItem adapt(Metadata metadata) {
        var contentSupplier = getContentSupplier(metadata.getId());
        return new StorageItem(metadata, contentSupplier);
    }

    private Supplier<InputStream> getContentSupplier(String id) {
        return () -> {
            try {
                return contentStore.get(id);
            }
            catch (IOException exception) {
                throw new ContentException(id, exception);
            }
        };
    }
}
