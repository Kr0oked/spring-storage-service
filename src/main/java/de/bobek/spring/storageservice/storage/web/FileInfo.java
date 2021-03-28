package de.bobek.spring.storageservice.storage.web;

import java.time.Instant;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FileInfo {

    private String id;

    private Long size;

    private String contentType;

    private String filename;

    private Instant creationDate;
}
