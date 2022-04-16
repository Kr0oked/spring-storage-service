package de.bobek.spring.storageservice.module.content.internal;

import java.io.ByteArrayInputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileSystemContentStoreTest {

    @TempDir
    private Path tempDir;

    @InjectMocks
    private FileSystemContentStore fileSystemContentStore;

    @Mock
    private FileSystemProperties fileSystemProperties;

    @Test
    void getStoredContent() throws Exception {
        var content = new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' });

        when(fileSystemProperties.getLocation()).thenReturn(tempDir);

        fileSystemContentStore.store("123", content);

        var storedContent = fileSystemContentStore.get("123");

        assertThat(storedContent).hasBinaryContent(new byte[] { 'a', 'b', 'c' });
        assertThat(tempDir).isDirectoryContaining("glob:**123");
    }

    @Test
    void deleteStoredContent() throws Exception {
        var content = new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' });

        when(fileSystemProperties.getLocation()).thenReturn(tempDir);

        fileSystemContentStore.store("123", content);
        fileSystemContentStore.delete("123");

        assertThat(tempDir).isEmptyDirectory();
    }

    @Test
    void storeThrowsExceptionWhenFileAlreadyExists() throws Exception {
        var content = new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' });

        when(fileSystemProperties.getLocation()).thenReturn(tempDir);

        fileSystemContentStore.store("123", content);

        assertThatThrownBy(() -> fileSystemContentStore.store("123", content))
                .isInstanceOf(FileAlreadyExistsException.class);
    }

    @Test
    void getThrowsExceptionWhenFileNotFound() {
        when(fileSystemProperties.getLocation()).thenReturn(tempDir);

        assertThatThrownBy(() -> fileSystemContentStore.get("123"))
                .isInstanceOf(NoSuchFileException.class);
    }

    @Test
    void deleteDoesNothingWhenFileNotFound() {
        when(fileSystemProperties.getLocation()).thenReturn(tempDir);

        assertThatNoException().isThrownBy(() -> fileSystemContentStore.delete("123"));
    }
}
