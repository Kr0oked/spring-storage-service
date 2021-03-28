package de.bobek.spring.storageservice.storage.web;

import java.io.IOException;
import java.util.Optional;

import de.bobek.spring.storageservice.storage.api.AddStorageItemData;
import de.bobek.spring.storageservice.storage.api.StorageItem;
import de.bobek.spring.storageservice.storage.api.StorageItemNotFoundException;
import de.bobek.spring.storageservice.storage.api.StorageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static java.util.function.Predicate.not;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
@Slf4j
public class StorageController {

    @NonNull
    private final StorageService storageService;

    @NonNull
    private final FileInfoPageAdapter fileInfoPageAdapter;

    @NonNull
    private final FileInfoAdapter fileInfoAdapter;

    @GetMapping
    public FileInfoPage list(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        var storageItemPage = storageService.list(userDetails.getUsername(), pageable);
        return fileInfoPageAdapter.adapt(storageItemPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserDetails userDetails) throws StorageItemNotFoundException {
        var storageItem = storageService.get(userDetails.getUsername(), id);
        return createResponseEntity(storageItem);
    }

    private ResponseEntity<Resource> createResponseEntity(StorageItem storageItem) {
        return ResponseEntity.ok()
                .contentType(getContentType(storageItem))
                .contentLength(storageItem.getMetadata().getSize())
                .header(CONTENT_DISPOSITION, getContentDisposition(storageItem).toString())
                .body(new InputStreamResource(storageItem.getContent()));
    }

    private MediaType getContentType(StorageItem storageItem) {
        return storageItem.getMetadata().getContentType()
                .filter(not(MimeType::isWildcardType))
                .filter(not(MimeType::isWildcardSubtype))
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    }

    private ContentDisposition getContentDisposition(StorageItem storageItem) {
        var contentDispositionBuilder = ContentDisposition.attachment();
        storageItem.getMetadata().getFilename().ifPresent(contentDispositionBuilder::filename);
        return contentDispositionBuilder.build();
    }

    @PostMapping("/upload")
    public FileInfo upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        var data = getAddStorageItemData(file, userDetails);

        try (var content = file.getInputStream()) {
            var storageItem = storageService.add(data, content);
            return fileInfoAdapter.adapt(storageItem.getMetadata());
        }
    }

    private AddStorageItemData getAddStorageItemData(MultipartFile file, UserDetails userDetails) {
        var dataBuilder = AddStorageItemData.builder()
                .username(userDetails.getUsername())
                .size(file.getSize());
        getContentType(file).ifPresent(dataBuilder::contentType);
        Optional.ofNullable(file.getOriginalFilename()).ifPresent(dataBuilder::filename);
        return dataBuilder.build();
    }

    private Optional<MediaType> getContentType(MultipartFile file) {
        try {
            return Optional.ofNullable(file.getContentType())
                    .map(MediaType::parseMediaType);
        }catch (InvalidMediaTypeException exception) {
            throw new InvalidContentTypeException(exception);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void delete(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserDetails userDetails) throws StorageItemNotFoundException {
        storageService.delete(userDetails.getUsername(), id);
    }
}
