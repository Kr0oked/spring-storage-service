package de.bobek.spring.storageservice.module.storage.web;

import java.time.Instant;

import de.bobek.spring.storageservice.module.storage.api.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;

import static de.bobek.spring.storageservice.Assertions.assertThat;

class FileInfoAdapterTest {

    private FileInfoAdapter fileInfoAdapter;

    @BeforeEach
    void setUp() {
        fileInfoAdapter = new FileInfoAdapter();
    }

    @Test
    void adaptWithAllAttributes() {
        var metadata = Metadata.builder()
                .id("123")
                .size(256L)
                .contentType(MediaType.TEXT_PLAIN)
                .filename("file.txt")
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();

        var fileInfo = fileInfoAdapter.adapt(metadata);

        assertThat(fileInfo)
                .hasId("123")
                .hasSize(256L)
                .hasContentType(MediaType.TEXT_PLAIN_VALUE)
                .hasFilename("file.txt")
                .hasCreationDate(Instant.parse("2007-12-03T10:15:30.00Z"));
    }

    @Test
    void adaptWithOnlyRequiredAttributes() {
        var metadata = Metadata.builder()
                .id("123")
                .size(256L)
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();

        var fileInfo = fileInfoAdapter.adapt(metadata);

        assertThat(fileInfo)
                .hasContentType(null)
                .hasFilename(null);
    }
}
