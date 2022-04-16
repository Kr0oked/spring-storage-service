package de.bobek.spring.storageservice.module.storage.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;

import de.bobek.spring.storageservice.module.metadata.api.Metadata;
import de.bobek.spring.storageservice.module.storage.StorageItemTestUtils;
import de.bobek.spring.storageservice.module.storage.api.AddStorageItemData;
import de.bobek.spring.storageservice.module.storage.api.StorageItem;
import de.bobek.spring.storageservice.module.storage.api.StorageItemNotFoundException;
import de.bobek.spring.storageservice.module.storage.api.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import static de.bobek.spring.storageservice.Assertions.assertThat;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageControllerTest {

    @InjectMocks
    private StorageController storageController;

    @Mock
    private StorageService storageService;

    @Mock
    private FileInfoPageAdapter fileInfoPageAdapter;

    @Mock
    private FileInfoAdapter fileInfoAdapter;

    @Captor
    private ArgumentCaptor<AddStorageItemData> addStorageItemDataCaptor;

    @Captor
    private ArgumentCaptor<InputStream> inputStreamCaptor;

    @Test
    void list() {
        var userDetails = getUserDetails("johnDoe");
        var pageable = PageRequest.of(0, 10);
        var storageItemPage = new PageImpl<StorageItem>(emptyList());
        var fileInfoPage = new FileInfoPage().setNumber(123);

        when(storageService.list("johnDoe", pageable)).thenReturn(storageItemPage);
        when(fileInfoPageAdapter.adapt(storageItemPage)).thenReturn(fileInfoPage);

        var result = storageController.list(userDetails, pageable);

        assertThat(result).isEqualTo(fileInfoPage);
    }

    @Test
    void download() throws Exception {
        var userDetails = getUserDetails("johnDoe");

        var metadata = Metadata.builder()
                .id("abc")
                .size(256L)
                .contentType(MediaType.TEXT_PLAIN)
                .filename("file.txt")
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();
        var content = new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' });
        var storageItem = new StorageItem(metadata, () -> content);

        when(storageService.get("johnDoe", "abc")).thenReturn(storageItem);

        var responseEntity = storageController.download("abc", userDetails);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getInputStream()).isEqualTo(content);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
        assertThat(responseEntity.getHeaders().getContentLength()).isEqualTo(256L);

        var contentDisposition = responseEntity.getHeaders().getContentDisposition();
        assertThat(contentDisposition.getName()).isNull();
        assertThat(contentDisposition.getFilename()).isEqualTo("file.txt");
        assertThat(contentDisposition.getCharset()).isNull();
        assertThat(contentDisposition.getType()).isEqualTo("attachment");
    }

    @Test
    void downloadUsesApplicationOctetStreamAsContentTypeWhenItemHasNoContentType() throws Exception {
        var userDetails = getUserDetails("johnDoe");

        var metadata = Metadata.builder()
                .id("abc")
                .size(256L)
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();
        var storageItem = new StorageItem(metadata, InputStream::nullInputStream);

        when(storageService.get("johnDoe", "abc")).thenReturn(storageItem);

        var responseEntity = storageController.download("abc", userDetails);

        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    void downloadUsesApplicationOctetStreamAsContentTypeWhenItemContentTypeIsWildcardType() throws Exception {
        var userDetails = getUserDetails("johnDoe");

        var metadata = Metadata.builder()
                .id("abc")
                .size(256L)
                .contentType(MediaType.parseMediaType("*"))
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();
        var storageItem = new StorageItem(metadata, InputStream::nullInputStream);

        when(storageService.get("johnDoe", "abc")).thenReturn(storageItem);

        var responseEntity = storageController.download("abc", userDetails);

        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    void downloadUsesApplicationOctetStreamAsContentTypeWhenItemContentTypeIsWildcardSubtype() throws Exception {
        var userDetails = getUserDetails("johnDoe");

        var metadata = Metadata.builder()
                .id("abc")
                .size(256L)
                .contentType(MediaType.parseMediaType("application/*"))
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();
        var storageItem = new StorageItem(metadata, InputStream::nullInputStream);

        when(storageService.get("johnDoe", "abc")).thenReturn(storageItem);

        var responseEntity = storageController.download("abc", userDetails);

        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    void downloadWithoutFilename() throws Exception {
        var userDetails = getUserDetails("johnDoe");

        var metadata = Metadata.builder()
                .id("abc")
                .size(256L)
                .creationDate(Instant.parse("2007-12-03T10:15:30.00Z"))
                .build();
        var storageItem = new StorageItem(metadata, InputStream::nullInputStream);

        when(storageService.get("johnDoe", "abc")).thenReturn(storageItem);

        var responseEntity = storageController.download("abc", userDetails);

        assertThat(responseEntity.getHeaders().getContentDisposition().getFilename()).isNull();
    }

    @Test
    void downloadThrowsExceptionWhenFileNotFound() throws Exception {
        var userDetails = getUserDetails("johnDoe");
        var exception = new StorageItemNotFoundException("johnDoe", "abc");

        when(storageService.get("johnDoe", "abc")).thenThrow(exception);

        assertThatThrownBy(() -> storageController.download("abc", userDetails))
                .isEqualTo(exception);
    }

    @Test
    void upload() throws Exception {
        var file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(111L);
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getOriginalFilename()).thenReturn("file.txt");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' }));

        var userDetails = getUserDetails("johnDoe");
        var storageItem = StorageItemTestUtils.getStorageItem();
        var fileInfo = FileInfoTestUtils.getFileInfo();

        when(storageService.add(any(AddStorageItemData.class), any(InputStream.class))).thenReturn(storageItem);
        when(fileInfoAdapter.adapt(storageItem.getMetadata())).thenReturn(fileInfo);

        var result = storageController.upload(file, userDetails);

        assertThat(result).isEqualTo(fileInfo);

        verify(storageService).add(addStorageItemDataCaptor.capture(), inputStreamCaptor.capture());
        assertThat(addStorageItemDataCaptor.getValue())
                .hasUsername("johnDoe")
                .hasSize(111L)
                .hasContentType(Optional.of(MediaType.TEXT_PLAIN))
                .hasFilename(Optional.of("file.txt"));
        assertThat(inputStreamCaptor.getValue()).hasBinaryContent(new byte[] { 'a', 'b', 'c' });
    }

    @Test
    void uploadFileWithoutContentTypeAndFilename() throws Exception {
        var file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(111L);
        when(file.getContentType()).thenReturn(null);
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' }));

        var userDetails = getUserDetails("johnDoe");
        var storageItem = StorageItemTestUtils.getStorageItem();
        var fileInfo = FileInfoTestUtils.getFileInfo();

        when(storageService.add(any(AddStorageItemData.class), any(InputStream.class))).thenReturn(storageItem);
        when(fileInfoAdapter.adapt(storageItem.getMetadata())).thenReturn(fileInfo);

        var result = storageController.upload(file, userDetails);

        assertThat(result).isEqualTo(fileInfo);

        verify(storageService).add(addStorageItemDataCaptor.capture(), any(InputStream.class));
        assertThat(addStorageItemDataCaptor.getValue())
                .hasFilename(Optional.empty())
                .hasContentType(Optional.empty());
    }

    @Test
    void uploadThrowsExceptionWhenContentTypeInvalid() {
        var file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(111L);
        when(file.getContentType()).thenReturn("invalid");

        var userDetails = getUserDetails("johnDoe");

        assertThatThrownBy(() -> storageController.upload(file, userDetails))
                .isInstanceOf(InvalidContentTypeException.class)
                .hasMessage("Invalid Content-Type header")
                .hasCauseInstanceOf(InvalidMediaTypeException.class);
    }

    @Test
    void uploadThrowsExceptionWhenServiceThrowsIOException() throws Exception {
        var exception = new IOException("error");

        var file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(111L);
        when(file.getContentType()).thenReturn(null);
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' }));

        when(storageService.add(any(AddStorageItemData.class), any(InputStream.class))).thenThrow(exception);

        var userDetails = getUserDetails("johnDoe");

        assertThatThrownBy(() -> storageController.upload(file, userDetails))
                .isEqualTo(exception);
    }

    @Test
    void uploadThrowsExceptionWhenFileThrowsIOException() throws Exception {
        var exception = new IOException("error");

        var file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(111L);
        when(file.getContentType()).thenReturn(null);
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getInputStream()).thenThrow(exception);

        var userDetails = getUserDetails("johnDoe");

        assertThatThrownBy(() -> storageController.upload(file, userDetails))
                .isEqualTo(exception);
    }

    @Test
    void delete() throws Exception {
        var userDetails = getUserDetails("johnDoe");

        storageController.delete("abc", userDetails);

        verify(storageService).delete("johnDoe", "abc");
    }

    @Test
    void deleteThrowsExceptionWhenFileNotFound() throws Exception {
        var exception = new StorageItemNotFoundException("johnDoe", "abc");
        var userDetails = getUserDetails("johnDoe");

        doThrow(exception).when(storageService).delete("johnDoe", "abc");

        assertThatThrownBy(() -> storageController.delete("abc", userDetails))
                .isEqualTo(exception);
    }

    private UserDetails getUserDetails(String username) {
        return User.withUsername(username)
                .password("secret")
                .authorities(emptyList())
                .build();
    }
}
