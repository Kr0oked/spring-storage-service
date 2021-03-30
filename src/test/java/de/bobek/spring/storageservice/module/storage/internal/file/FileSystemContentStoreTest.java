package de.bobek.spring.storageservice.module.storage.internal.file;

import java.io.ByteArrayInputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileSystemContentStoreTest {

    @TempDir
    Path tempDir;

    private FileSystemContentStore fileSystemContentStore;

    @BeforeEach
    void setUp() {
        fileSystemContentStore = new FileSystemContentStore(() -> tempDir);
    }

    @Test
    void getStoredContent() throws Exception {
        var content = new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' });

        fileSystemContentStore.store("123", content);

        var storedContent = fileSystemContentStore.get("123");

        assertThat(storedContent).hasBinaryContent(new byte[] { 'a', 'b', 'c' });
        assertThat(tempDir).isDirectoryContaining("glob:**123");
    }

    @Test
    void deleteStoredContent() throws Exception {
        var content = new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' });

        fileSystemContentStore.store("123", content);
        fileSystemContentStore.delete("123");

        assertThat(tempDir).isEmptyDirectory();
    }

    @Test
    void storeThrowsExceptionWhenFileAlreadyExists() throws Exception {
        var content = new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' });

        fileSystemContentStore.store("123", content);

        assertThatThrownBy(() -> fileSystemContentStore.store("123", content))
                .isInstanceOf(FileAlreadyExistsException.class);
    }

    @Test
    void getThrowsExceptionWhenFileNotFound() {
        assertThatThrownBy(() -> fileSystemContentStore.get("123"))
                .isInstanceOf(NoSuchFileException.class);
    }

    @Test
    void deleteDoesNothingWhenFileNotFound() {
        assertThatNoException().isThrownBy(() -> fileSystemContentStore.delete("123"));
    }
}
