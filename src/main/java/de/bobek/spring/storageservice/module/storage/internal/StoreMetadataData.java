package de.bobek.spring.storageservice.module.storage.internal;

import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import org.springframework.http.MediaType;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class StoreMetadataData {

    @NonNull
    private final Long size;

    private final MediaType contentType;

    private final String filename;

    public long getSize() {
        return size;
    }

    public Optional<MediaType> getContentType() {
        return Optional.ofNullable(contentType);
    }

    public Optional<String> getFilename() {
        return Optional.ofNullable(filename);
    }
}
