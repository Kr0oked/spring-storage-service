package de.bobek.spring.storageservice.module.storage.api;

import java.util.Optional;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import org.springframework.http.MediaType;

@Builder
@ToString
@EqualsAndHashCode
public final class AddStorageItemData {

    @NonNull
    private final String username;

    @NonNull
    private final Long size;

    private final MediaType contentType;

    private final String filename;

    public String getUsername() {
        return username;
    }

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
