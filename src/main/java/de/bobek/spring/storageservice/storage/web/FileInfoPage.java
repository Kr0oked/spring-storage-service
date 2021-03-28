package de.bobek.spring.storageservice.storage.web;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FileInfoPage {

    private Integer totalPages;

    private Long totalElements;

    private Integer number;

    private Integer size;

    private Integer numberOfElements;

    private List<FileInfo> content;
}
