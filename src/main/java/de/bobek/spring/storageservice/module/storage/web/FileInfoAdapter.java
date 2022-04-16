package de.bobek.spring.storageservice.module.storage.web;

import de.bobek.spring.storageservice.module.metadata.api.Metadata;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
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
