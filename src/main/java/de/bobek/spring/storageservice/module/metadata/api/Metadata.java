package de.bobek.spring.storageservice.module.metadata.api;

import java.time.Instant;
import java.util.Optional;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import org.springframework.http.MediaType;

@Builder
@ToString
@EqualsAndHashCode
public final class Metadata {

    @NonNull
    private final String id;

    @NonNull
    private final Long size;

    private final MediaType contentType;

    private final String filename;

    @NonNull
    private final Instant creationDate;

    public String getId() {
        return id;
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

    public Instant getCreationDate() {
        return creationDate;
    }
}
