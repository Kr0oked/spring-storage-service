package de.bobek.spring.storageservice.module.metadata.internal;

import java.util.Optional;
import java.util.UUID;

import de.bobek.spring.storageservice.common.TimeProvider;
import de.bobek.spring.storageservice.module.metadata.api.Metadata;
import de.bobek.spring.storageservice.module.metadata.api.MetadataStore;
import de.bobek.spring.storageservice.module.storage.api.AddStorageItemData;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;

@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseMetadataStore implements MetadataStore {

    @NonNull
    private final ItemRepository itemRepository;

    @NonNull
    private final ItemAdapter itemAdapter;

    @NonNull
    private final TimeProvider timeProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<Metadata> list(String username, Pageable pageable) {
        return itemRepository.findAllByUsername(username, pageable)
                .map(itemAdapter::adapt);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Metadata> find(String username, String id) {
        return adaptId(id)
                .flatMap(uuid -> itemRepository.findByIdAndUsername(uuid, username))
                .map(itemAdapter::adapt);
    }

    @Override
    @Transactional
    public Metadata store(AddStorageItemData data) {
        var item = new Item();
        item.setUsername(data.getUsername());
        item.setSize(data.getSize());
        item.setCreationDate(timeProvider.now());

        data.getContentType()
                .map(MimeType::toString)
                .ifPresent(item::setContentType);

        data.getFilename()
                .ifPresent(item::setFilename);

        item = itemRepository.save(item);
        log.debug("Saved {}", item);

        return itemAdapter.adapt(item);
    }

    @Override
    @Transactional
    public Optional<Metadata> delete(String username, String id) {
        return adaptId(id)
                .flatMap(uuid -> itemRepository.findByIdAndUsername(uuid, username))
                .map(this::delete);
    }

    private Metadata delete(Item item) {
        var metadata = itemAdapter.adapt(item);
        itemRepository.delete(item);
        log.debug("Deleted item '{}'", item.getId());
        return metadata;
    }

    private Optional<UUID> adaptId(String id) {
        try {
            return Optional.of(UUID.fromString(id));
        }
        catch (IllegalArgumentException exception) {
            log.trace("Provided id '{}' is not a valid UUID", id, exception);
            return Optional.empty();
        }
    }
}
