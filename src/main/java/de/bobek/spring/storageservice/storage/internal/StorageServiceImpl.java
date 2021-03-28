package de.bobek.spring.storageservice.storage.internal;

import java.io.IOException;
import java.io.InputStream;

import de.bobek.spring.storageservice.storage.api.AddStorageItemData;
import de.bobek.spring.storageservice.storage.api.StorageItem;
import de.bobek.spring.storageservice.storage.api.StorageItemNotFoundException;
import de.bobek.spring.storageservice.storage.api.StorageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    @NonNull
    private final MetadataStore metadataStore;

    @NonNull
    private final ContentStore contentStore;

    @NonNull
    private final StorageItemAdapter storageItemAdapter;

    @Override
    public Page<StorageItem> list(String username, Pageable pageable) {
        return metadataStore.list(username, pageable)
                .map(storageItemAdapter::adapt);
    }

    @Override
    public StorageItem get(String username, String id) throws StorageItemNotFoundException {
        return metadataStore.find(username, id)
                .map(storageItemAdapter::adapt)
                .orElseThrow(() -> new StorageItemNotFoundException(username, id));
    }

    @Override
    public StorageItem add(AddStorageItemData data, InputStream content) throws IOException {
        var metadata = metadataStore.store(data);
        contentStore.store(metadata.getId(), content);
        return storageItemAdapter.adapt(metadata);
    }

    @Override
    public void delete(String username, String id) throws StorageItemNotFoundException {
        deleteMetadata(username, id);
        deleteContent(id);
    }

    private void deleteMetadata(String username, String id) throws StorageItemNotFoundException {
        metadataStore.delete(username, id)
                .orElseThrow(() -> new StorageItemNotFoundException(username, id));
    }

    private void deleteContent(String id) {
        try {
            contentStore.delete(id);
        }
        catch (IOException exception) {
            throw new ContentException(id, exception);
        }
    }
}
