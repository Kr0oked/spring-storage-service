package de.bobek.spring.storageservice.storage.internal.file;

import java.nio.file.Path;

public interface StorageLocationProvider {

    Path getLocation();
}
