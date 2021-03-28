package de.bobek.spring.storageservice.storage.web;

import de.bobek.spring.storageservice.storage.api.Metadata;

import org.springframework.http.MediaType;

public class FileInfoAdapter {

    public FileInfo adapt(Metadata metadata) {
        var fileInfo = new FileInfo()
                .setId(metadata.getId())
                .setSize(metadata.getSize())
                .setCreationDate(metadata.getCreationDate());

        metadata.getContentType()
                .map(MediaType::toString)
                .ifPresent(fileInfo::setContentType);

        metadata.getFilename()
                .ifPresent(fileInfo::setFilename);

        return fileInfo;
    }
}
