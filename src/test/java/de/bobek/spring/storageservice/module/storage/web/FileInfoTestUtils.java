package de.bobek.spring.storageservice.module.storage.web;

public final class FileInfoTestUtils {

    private FileInfoTestUtils() {
    }

    public static FileInfo getFileInfo() {
        return new FileInfo()
                .setId("123");
    }
}
