package de.bobek.spring.storageservice.storage.internal.db;

import java.util.Optional;

import de.bobek.spring.storageservice.storage.api.Metadata;

import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

public class ItemAdapter {

    @Transactional(propagation = MANDATORY, readOnly = true)
    public Metadata adapt(Item item) {
        var metadataBuilder = Metadata.builder()
                .id(item.getId().toString())
                .size(item.getSize())
                .creationDate(item.getCreationDate());

        Optional.ofNullable(item.getContentType())
                .map(MediaType::parseMediaType)
                .ifPresent(metadataBuilder::contentType);

        Optional.ofNullable(item.getFilename())
                .ifPresent(metadataBuilder::filename);

        return metadataBuilder.build();
    }
}
