package de.bobek.spring.storageservice.module.storage.internal.file;

import java.nio.file.Path;

public interface StorageLocationProvider {

    Path getLocation();
}
