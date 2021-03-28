package de.bobek.spring.storageservice.storage.api;

import java.io.InputStream;
import java.util.function.Supplier;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class StorageItem {

    @NonNull
    private final Metadata metadata;

    @NonNull
    private final Supplier<InputStream> contentSupplier;

    public Metadata getMetadata() {
        return metadata;
    }

    public InputStream getContent() {
        return contentSupplier.get();
    }
}
