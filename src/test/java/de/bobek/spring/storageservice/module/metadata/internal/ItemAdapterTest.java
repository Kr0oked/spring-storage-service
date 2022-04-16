package de.bobek.spring.storageservice.module.metadata.internal;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;

import static de.bobek.spring.storageservice.Assertions.assertThat;

class ItemAdapterTest {

    private ItemAdapter itemAdapter;

    @BeforeEach
    void setUp() {
        itemAdapter = new ItemAdapter();
    }

    @Test
    void adaptFull() {
        var item = new Item();
        item.setId(UUID.fromString("123e4567-e89b-42d3-a456-556642440000"));
        item.setUsername("johnDoe");
        item.setSize(256L);
        item.setContentType(MediaType.TEXT_PLAIN_VALUE);
        item.setFilename("file.txt");
        item.setCreationDate(Instant.parse("2007-12-03T10:15:30.00Z"));

        var metadata = itemAdapter.adapt(item);

        assertThat(metadata)
                .hasId("123e4567-e89b-42d3-a456-556642440000")
                .hasSize(256L)
                .hasContentType(Optional.of(MediaType.TEXT_PLAIN))
                .hasFilename(Optional.of("file.txt"))
                .hasCreationDate(Instant.parse("2007-12-03T10:15:30.00Z"));
    }

    @Test
    void adaptMinimal() {
        var item = new Item();
        item.setId(UUID.fromString("123e4567-e89b-42d3-a456-556642440000"));
        item.setUsername("johnDoe");
        item.setSize(256L);
        item.setCreationDate(Instant.parse("2007-12-03T10:15:30.00Z"));

        var metadata = itemAdapter.adapt(item);

        assertThat(metadata)
                .hasContentType(Optional.empty())
                .hasFilename(Optional.empty());
    }
}
