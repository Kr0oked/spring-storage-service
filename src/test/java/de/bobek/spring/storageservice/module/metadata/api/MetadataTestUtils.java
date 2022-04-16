package de.bobek.spring.storageservice.module.metadata.api;

import java.time.Instant;

public final class MetadataTestUtils {

    private MetadataTestUtils() {
    }

    public static Metadata getMetadata() {
        return Metadata.builder()
                .id("123")
                .size(256L)
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();
    }
}
