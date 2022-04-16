package de.bobek.spring.storageservice.module.content.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import de.bobek.spring.storageservice.module.content.api.ContentStore;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileSystemContentStore implements ContentStore {

    @NonNull
    private final FileSystemProperties fileSystemProperties;

    @Override
    public InputStream get(String id) throws IOException {
        var path = buildPath(id);
        return Files.newInputStream(path);
    }

    @Override
    public void store(String id, InputStream content) throws IOException {
        var path = buildPath(id);

        try (var fileStorage = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
            content.transferTo(fileStorage);
            log.trace("Stored content at {}", path);
        }
    }

    @Override
    public void delete(String id) throws IOException {
        var path = buildPath(id);
        var deleted = Files.deleteIfExists(path);
        if (deleted) {
            log.debug("Deleted content at {}", path);
        }
        else {
            log.warn("Content at {} did not exist", path);
        }
    }

    private Path buildPath(String id) {
        return fileSystemProperties.getLocation().resolve(id);
    }
}
