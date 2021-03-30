package de.bobek.spring.storageservice.module.storage.api;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(NOT_FOUND)
public final class StorageItemNotFoundException extends Exception {

    private static final long serialVersionUID = -8834613705811778044L;

    public StorageItemNotFoundException(String username, String storageItemId) {
        super(String.format("User '%s' does not have storage item with ID '%s'", username, storageItemId));
    }
}
