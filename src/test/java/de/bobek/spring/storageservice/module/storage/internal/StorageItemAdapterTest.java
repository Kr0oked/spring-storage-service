package de.bobek.spring.storageservice.module.storage.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;

import de.bobek.spring.storageservice.module.content.api.ContentStore;
import de.bobek.spring.storageservice.module.metadata.api.Metadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.bobek.spring.storageservice.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageItemAdapterTest {

    @InjectMocks
    private StorageItemAdapter storageItemAdapter;

    @Mock
    private ContentStore contentStore;

    @Test
    void adapt() throws Exception {
        var metadata = Metadata.builder()
                .id("123")
                .size(256L)
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();

        var storageItem = storageItemAdapter.adapt(metadata);

        verifyNoInteractions(contentStore);
        when(contentStore.get("123")).thenReturn(new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' }));

        assertThat(storageItem).hasMetadata(metadata);
        assertThat(storageItem.getContent()).hasBinaryContent(new byte[] { 'a', 'b', 'c' });
    }

    @Test
    void storageItemThrowsExceptionWhenGettingContentFails() throws Exception {
        var metadata = Metadata.builder()
                .id("123")
                .size(256L)
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();

        var storageItem = storageItemAdapter.adapt(metadata);

        var ioException = new IOException();
        when(contentStore.get("123")).thenThrow(ioException);

        assertThatThrownBy(storageItem::getContent)
                .isInstanceOf(ContentException.class)
                .hasMessage("Operation for content '123' failed")
                .hasCause(ioException);
    }
}
