package de.bobek.spring.storageservice.module.metadata.internal;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.bobek.spring.storageservice.common.TimeProvider;
import de.bobek.spring.storageservice.module.metadata.api.MetadataTestUtils;
import de.bobek.spring.storageservice.module.storage.api.AddStorageItemData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import static de.bobek.spring.storageservice.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseMetadataStoreTest {

    @InjectMocks
    private DatabaseMetadataStore databaseMetadataStore;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemAdapter itemAdapter;

    @Mock
    private TimeProvider timeProvider;

    @Captor
    private ArgumentCaptor<Item> itemCaptor;

    @Test
    void list() {
        var item = getItem();
        var metadata = MetadataTestUtils.getMetadata();
        var pageRequest = PageRequest.of(0, 1);
        var itemPage = new PageImpl<>(List.of(item), pageRequest, 123L);

        when(itemRepository.findAllByUsername("johnDoe", pageRequest)).thenReturn(itemPage);
        when(itemAdapter.adapt(item)).thenReturn(metadata);

        var page = databaseMetadataStore.list("johnDoe", pageRequest);

        assertThat(page.getContent()).containsOnly(metadata);
        assertThat(page.getPageable()).isEqualTo(pageRequest);
        assertThat(page.getTotalElements()).isEqualTo(123);
    }

    @Test
    void findReturnsMetadata() {
        var item = getItem();
        var metadata = MetadataTestUtils.getMetadata();
        var uuid = getUUID();

        when(itemRepository.findByIdAndUsername(uuid, "johnDoe")).thenReturn(Optional.of(item));
        when(itemAdapter.adapt(item)).thenReturn(metadata);

        var result = databaseMetadataStore.find("johnDoe", uuid.toString());

        assertThat(result).contains(metadata);
    }

    @Test
    void findReturnsEmptyWhenMetadataNotFound() {
        var uuid = getUUID();

        when(itemRepository.findByIdAndUsername(uuid, "johnDoe")).thenReturn(Optional.empty());

        var result = databaseMetadataStore.find("johnDoe", uuid.toString());

        assertThat(result).isEmpty();
    }

    @Test
    void findReturnsEmptyWhenIdIsNotValidUUID() {
        var result = databaseMetadataStore.find("johnDoe", "invalid");

        assertThat(result).isEmpty();
    }

    @Test
    void storeFull() {
        var item = getItem();
        var metadata = MetadataTestUtils.getMetadata();

        when(timeProvider.now()).thenReturn(Instant.parse("2007-12-03T10:15:30.00Z"));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemAdapter.adapt(item)).thenReturn(metadata);

        var data = AddStorageItemData.builder()
                .username("johnDoe")
                .size(256L)
                .contentType(MediaType.TEXT_PLAIN)
                .filename("file.txt")
                .build();

        var result = databaseMetadataStore.store(data);

        assertThat(result).isEqualTo(metadata);

        verify(itemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue())
                .hasId(null)
                .hasUsername("johnDoe")
                .hasSize(256L)
                .hasContentType("text/plain")
                .hasFilename("file.txt")
                .hasCreationDate(Instant.parse("2007-12-03T10:15:30.00Z"));
    }

    @Test
    void storeMinimal() {
        var item = getItem();
        var metadata = MetadataTestUtils.getMetadata();

        when(timeProvider.now()).thenReturn(Instant.parse("2007-12-03T10:15:30.00Z"));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemAdapter.adapt(item)).thenReturn(metadata);

        var data = AddStorageItemData.builder()
                .username("johnDoe")
                .size(256L)
                .build();

        var result = databaseMetadataStore.store(data);

        assertThat(result).isEqualTo(metadata);

        verify(itemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue())
                .hasContentType(null)
                .hasFilename(null);
    }

    @Test
    void deleteReturnsMetadata() {
        var item = getItem();
        var metadata = MetadataTestUtils.getMetadata();
        var uuid = getUUID();

        when(itemRepository.findByIdAndUsername(uuid, "johnDoe")).thenReturn(Optional.of(item));
        when(itemAdapter.adapt(item)).thenReturn(metadata);

        var result = databaseMetadataStore.delete("johnDoe", uuid.toString());

        assertThat(result).contains(metadata);

        var inOrder = inOrder(itemRepository, itemAdapter);
        inOrder.verify(itemAdapter).adapt(item);
        inOrder.verify(itemRepository).delete(item);
    }

    @Test
    void deleteReturnsEmptyWhenMetadataNotFound() {
        var uuid = getUUID();

        when(itemRepository.findByIdAndUsername(uuid, "johnDoe")).thenReturn(Optional.empty());

        var result = databaseMetadataStore.delete("johnDoe", uuid.toString());

        assertThat(result).isEmpty();
    }

    @Test
    void deleteReturnsEmptyWhenIdIsNotValidUUID() {
        var result = databaseMetadataStore.delete("johnDoe", "invalid");

        assertThat(result).isEmpty();
    }

    private Item getItem() {
        var item = new Item();
        item.setId(getUUID());
        return item;
    }

    private UUID getUUID() {
        return UUID.fromString("123e4567-e89b-42d3-a456-556642440000");
    }
}
