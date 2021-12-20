package de.bobek.spring.storageservice.module.storage.web;

import de.bobek.spring.storageservice.module.storage.api.StorageItem;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class FileInfoPageAdapter {

    @NonNull
    private final FileInfoAdapter fileInfoAdapter;

    public FileInfoPage adapt(Page<StorageItem> storageItemPage) {
        var fileInfo = storageItemPage.getContent().stream()
                .map(StorageItem::getMetadata)
                .map(fileInfoAdapter::adapt)
                .collect(toList());

        return new FileInfoPage()
                .setTotalPages(storageItemPage.getTotalPages())
                .setTotalElements(storageItemPage.getTotalElements())
                .setNumber(storageItemPage.getNumber())
                .setSize(storageItemPage.getSize())
                .setNumberOfElements(storageItemPage.getNumberOfElements())
                .setContent(fileInfo);
    }
}
