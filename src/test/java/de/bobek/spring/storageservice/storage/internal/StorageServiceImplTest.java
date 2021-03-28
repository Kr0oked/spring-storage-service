package de.bobek.spring.storageservice.storage.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import de.bobek.spring.storageservice.storage.StorageTestUtils;
import de.bobek.spring.storageservice.storage.api.AddStorageItemData;
import de.bobek.spring.storageservice.storage.api.Metadata;
import de.bobek.spring.storageservice.storage.api.StorageItemNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static de.bobek.spring.storageservice.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

    @InjectMocks
    private StorageServiceImpl storageServiceImpl;

    @Mock
    private MetadataStore metadataStore;

    @Mock
    private ContentStore contentStore;

    @Mock
    private StorageItemAdapter storageItemAdapter;

    @Test
    void list() {
        var metadata = StorageTestUtils.getMetadata();
        var storageItem = StorageTestUtils.getStorageItem();
        var pageRequest = PageRequest.of(0, 1);
        var metadataPage = new PageImpl<>(List.of(metadata), pageRequest, 123L);

        when(metadataStore.list("johnDoe", pageRequest)).thenReturn(metadataPage);
        when(storageItemAdapter.adapt(metadata)).thenReturn(storageItem);

        var page = storageServiceImpl.list("johnDoe", pageRequest);

        assertThat(page.getContent()).containsExactly(storageItem);
        assertThat(page.getPageable()).isEqualTo(pageRequest);
        assertThat(page.getTotalElements()).isEqualTo(123L);
    }

    @Test
    void get() throws Exception {
        var metadata = StorageTestUtils.getMetadata();
        var storageItem = StorageTestUtils.getStorageItem();

        when(metadataStore.find("johnDoe", "123")).thenReturn(Optional.of(metadata));
        when(storageItemAdapter.adapt(metadata)).thenReturn(storageItem);

        var result = storageServiceImpl.get("johnDoe", "123");

        assertThat(result).isEqualTo(storageItem);
    }

    @Test
    void getThrowsExceptionWhenStorageItemNotFound() {
        when(metadataStore.find("johnDoe", "123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storageServiceImpl.get("johnDoe", "123"))
                .isInstanceOf(StorageItemNotFoundException.class)
                .hasMessage("User 'johnDoe' does not have storage item with ID '123'")
                .hasNoCause();
    }

    @Test
    void add() throws Exception {
        var metadata = Metadata.builder()
                .id("123")
                .size(256L)
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();
        var storageItem = StorageTestUtils.getStorageItem();
        var data = getAddStorageItemData();
        var content = new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' });

        when(metadataStore.store(data)).thenReturn(metadata);
        when(storageItemAdapter.adapt(metadata)).thenReturn(storageItem);

        var result = storageServiceImpl.add(data, content);

        assertThat(result).isEqualTo(storageItem);

        var inOrder = inOrder(contentStore, storageItemAdapter);
        inOrder.verify(contentStore).store("123", content);
        inOrder.verify(storageItemAdapter).adapt(metadata);
    }

    @Test
    void addThrowsExceptionWhenContentCreationThrowsIOException() throws Exception {
        var metadata = Metadata.builder()
                .id("123")
                .size(256L)
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();
        var data = getAddStorageItemData();
        var content = new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' });
        var ioException = new IOException();

        when(metadataStore.store(data)).thenReturn(metadata);
        doThrow(ioException).when(contentStore).store("123", content);

        assertThatThrownBy(() -> storageServiceImpl.add(data, content))
                .isEqualTo(ioException);
    }

    @Test
    void delete() throws Exception {
        var metadata = Metadata.builder()
                .id("123")
                .size(256L)
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();

        when(metadataStore.delete("johnDoe", "123")).thenReturn(Optional.of(metadata));

        storageServiceImpl.delete("johnDoe", "123");

        verify(contentStore).delete("123");
    }

    @Test
    void deleteThrowsExceptionWhenContentDeletionThrowsIOException() throws Exception {
        var metadata = Metadata.builder()
                .id("123")
                .size(256L)
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();
        var ioException = new IOException();

        when(metadataStore.delete("johnDoe", "123")).thenReturn(Optional.of(metadata));
        doThrow(ioException).when(contentStore).delete("123");

        assertThatThrownBy(() -> storageServiceImpl.delete("johnDoe", "123"))
                .isInstanceOf(ContentException.class)
                .hasMessage("Operation for content '123' failed")
                .hasCause(ioException);
    }

    @Test
    void deleteThrowsExceptionWhenStorageItemNotFound() {
        when(metadataStore.delete("johnDoe", "123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storageServiceImpl.delete("johnDoe", "123"))
                .isInstanceOf(StorageItemNotFoundException.class)
                .hasMessage("User 'johnDoe' does not have storage item with ID '123'")
                .hasNoCause();
    }

    private AddStorageItemData getAddStorageItemData() {
        return AddStorageItemData.builder()
                .username("johnDoe")
                .size(256L)
                .build();
    }
}
