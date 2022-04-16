package de.bobek.spring.storageservice.module.storage;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import de.bobek.spring.storageservice.common.TimeProvider;
import de.bobek.spring.storageservice.module.content.internal.FileSystemProperties;
import de.bobek.spring.storageservice.module.metadata.internal.ItemRepository;
import de.bobek.spring.storageservice.module.storage.web.FileInfo;
import de.bobek.spring.storageservice.module.storage.web.FileInfoPage;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.lang.Nullable;

import static de.bobek.spring.storageservice.Assertions.assertThat;
import static io.restassured.http.ContentType.JSON;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class StorageIntegrationTest {

    @TempDir
    private Path tempDir;

    @LocalServerPort
    private Integer port;

    @MockBean
    private TimeProvider timeProvider;

    @MockBean
    private FileSystemProperties fileSystemProperties;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        when(timeProvider.now()).thenReturn(Instant.parse("2007-12-03T10:15:30.00Z"));
        when(fileSystemProperties.getLocation()).thenReturn(tempDir);

        itemRepository.deleteAll();
    }

    @Test
    void list() {
        var fileInfoA = upload("alice", "fileA.txt", "AAA".getBytes(UTF_8), "text/plain");
        var fileInfoB = upload("alice", "fileB.exe", "BBB".getBytes(UTF_8), "application/octet-stream");

        var page = list("alice", null, null, null);
        assertThat(page)
                .hasTotalPages(1)
                .hasTotalElements(2L)
                .hasNumber(0)
                .hasSize(20)
                .hasNumberOfElements(2);
        assertThat(page.getContent()).containsExactly(fileInfoA, fileInfoB);
    }

    @Test
    void listPaging() {
        var fileInfoA = upload("alice", "fileA.txt", "AAA".getBytes(UTF_8), "text/plain");
        var fileInfoB = upload("alice", "fileB.txt", "BBB".getBytes(UTF_8), "text/plain");
        var fileInfoC = upload("alice", "fileC.txt", "CCC".getBytes(UTF_8), "text/plain");

        var firstPage = list("alice", 0, 2, null);
        assertThat(firstPage)
                .hasTotalPages(2)
                .hasTotalElements(3L)
                .hasNumber(0)
                .hasSize(2)
                .hasNumberOfElements(2);
        assertThat(firstPage.getContent()).containsExactly(fileInfoA, fileInfoB);

        var secondPage = list("alice", 1, 2, null);
        assertThat(secondPage)
                .hasTotalPages(2)
                .hasTotalElements(3L)
                .hasNumber(1)
                .hasSize(2)
                .hasNumberOfElements(1);
        assertThat(secondPage.getContent()).containsExactly(fileInfoC);
    }

    @Test
    void listSorting() {
        var fileInfoB = upload("alice", "fileB.txt", "BBB".getBytes(UTF_8), "text/plain");
        var fileInfoC = upload("alice", "fileC.txt", "CCC".getBytes(UTF_8), "text/plain");
        var fileInfoA = upload("alice", "fileA.txt", "AAA".getBytes(UTF_8), "text/plain");

        var page = list("alice", null, null, "filename,desc");
        assertThat(page)
                .hasTotalPages(1)
                .hasTotalElements(3L)
                .hasNumber(0)
                .hasSize(20)
                .hasNumberOfElements(3);
        assertThat(page.getContent()).containsExactly(fileInfoC, fileInfoB, fileInfoA);
    }

    @Test
    void listDosNotShowFilesOfOtherUsers() {
        upload("alice", "fileA.txt", "AAA".getBytes(UTF_8), "text/plain");
        var page = list("bob", null, null, null);

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void listFailsWhenCredentialsInvalid() {
        RestAssured
                .with().auth().preemptive().basic("chuck", "secret")
                .and().accept(JSON)
                .when().get("/storage")
                .then().assertThat().statusCode(UNAUTHORIZED.value());
    }

    @Test
    void upload() {
        var fileInfo = upload("alice", "fileA.txt", "AAA".getBytes(UTF_8), "text/plain");

        assertThat(fileInfo)
                .hasSize(3L)
                .hasContentType("text/plain")
                .hasFilename("fileA.txt")
                .hasCreationDate(Instant.parse("2007-12-03T10:15:30.00Z"));
    }

    @Test
    void uploadWithWildcardMimeType() {
        var fileInfo = upload("alice", "file.txt", "AAA".getBytes(UTF_8), "*");
        assertThat(fileInfo).hasContentType("*/*");

        RestAssured
                .with().auth().preemptive().basic("alice", "secret")
                .when().get("/storage/{id}", fileInfo.getId())
                .then().assertThat().statusCode(OK.value())
                .and().assertThat().header(CONTENT_TYPE, "application/octet-stream");
    }

    @Test
    void uploadSameFilesTwice() {
        var fileInfoA = upload("alice", "fileA.txt", "AAA".getBytes(UTF_8), "text/plain");
        var fileInfoB = upload("alice", "fileA.txt", "AAA".getBytes(UTF_8), "text/plain");
        assertThat(fileInfoA.getId()).isNotEqualTo(fileInfoB.getId());

        var page = list("alice", 0, 10, null);
        assertThat(page).hasNumberOfElements(2);
    }

    @Test
    void uploadFailsWhenContentTypeInvalid() {
        RestAssured
                .with().auth().preemptive().basic("alice", "secret")
                .and().multiPart("file", "file.txt", "AAA".getBytes(UTF_8), "invalid")
                .and().accept(JSON)
                .when().post("/storage/upload")
                .then().assertThat().statusCode(BAD_REQUEST.value());
    }

    @Test
    void uploadFailsWhenCredentialsInvalid() {
        RestAssured
                .with().auth().preemptive().basic("craig", "secret")
                .and().multiPart("file", "file.txt", "AAA".getBytes(UTF_8), "text/plain")
                .and().accept(JSON)
                .when().post("/storage/upload")
                .then().assertThat().statusCode(UNAUTHORIZED.value());
    }

    @Test
    void download() {
        var fileInfo = upload("alice", "file.txt", "AAA".getBytes(UTF_8), "text/plain");

        var content = download("alice", fileInfo);
        assertThat(content).asString().isEqualTo("AAA");
    }

    @Test
    void downloadFailsWhenFileBelongsToOtherUser() {
        var fileInfo = upload("alice", "file.txt", "AAA".getBytes(UTF_8), "text/plain");

        RestAssured
                .with().auth().preemptive().basic("bob", "secret")
                .when().get("/storage/{id}", fileInfo.getId())
                .then().assertThat().statusCode(NOT_FOUND.value());
    }

    @Test
    void downloadFailsWhenUnknownIdSpecified() {
        RestAssured
                .with().auth().preemptive().basic("alice", "secret")
                .when().get("/storage/{id}", "unknown")
                .then().assertThat().statusCode(NOT_FOUND.value());
    }

    @Test
    void downloadFailsWhenCredentialsInvalid() {
        RestAssured
                .with().auth().preemptive().basic("craig", "secret")
                .when().get("/storage/{id}", "unknown")
                .then().assertThat().statusCode(UNAUTHORIZED.value());
    }

    @Test
    void delete() {
        var fileInfo = upload("alice", "fileA.txt", "AAA".getBytes(UTF_8), "text/plain");

        delete("alice", fileInfo);

        var page = list("alice", 0, 10, null);
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void deleteFailsWhenFileBelongsToOtherUser() {
        var fileInfo = upload("alice", "file.txt", "AAA".getBytes(UTF_8), "text/plain");

        RestAssured
                .with().auth().preemptive().basic("bob", "secret")
                .when().delete("/storage/{id}", fileInfo.getId())
                .then().assertThat().statusCode(NOT_FOUND.value());
    }

    @Test
    void deleteFailsWhenUnknownIdSpecified() {
        RestAssured
                .with().auth().preemptive().basic("alice", "secret")
                .when().delete("/storage/{id}", "unknown")
                .then().assertThat().statusCode(NOT_FOUND.value());
    }

    @Test
    void deleteFailsWhenCredentialsInvalid() {
        RestAssured
                .with().auth().preemptive().basic("craig", "secret")
                .when().delete("/storage/{id}", "unknown")
                .then().assertThat().statusCode(UNAUTHORIZED.value());
    }

    private FileInfoPage list(String username, @Nullable Integer page, @Nullable Integer size, @Nullable String sort) {
        var specs = RestAssured
                .with().auth().preemptive().basic(username, "secret")
                .and().accept(JSON);

        Optional.ofNullable(page).ifPresent(value -> specs.queryParam("page", value));
        Optional.ofNullable(size).ifPresent(value -> specs.queryParam("size", value));
        Optional.ofNullable(sort).ifPresent(value -> specs.queryParam("sort", value));

        return specs.when().get("/storage")
                .then().assertThat().statusCode(OK.value())
                .and().assertThat().contentType(JSON)
                .and().extract().body().as(FileInfoPage.class);

    }

    private FileInfo upload(String username, String filename, byte[] content, String mimeType) {
        return RestAssured
                .with().auth().preemptive().basic(username, "secret")
                .and().multiPart("file", filename, content, mimeType)
                .and().accept(JSON)
                .when().post("/storage/upload")
                .then().assertThat().statusCode(OK.value())
                .and().assertThat().contentType(JSON)
                .and().extract().body().as(FileInfo.class);
    }

    private byte[] download(String username, FileInfo fileInfo) {
        var contentDisposition = String.format("attachment; filename=\"%s\"", fileInfo.getFilename());

        return RestAssured
                .with().auth().preemptive().basic(username, "secret")
                .when().get("/storage/{id}", fileInfo.getId())
                .then().assertThat().statusCode(OK.value())
                .and().assertThat().header(CONTENT_LENGTH, fileInfo.getSize().toString())
                .and().assertThat().header(CONTENT_TYPE, fileInfo.getContentType())
                .and().assertThat().header(CONTENT_DISPOSITION, contentDisposition)
                .extract().body().asByteArray();
    }

    private void delete(String username, FileInfo fileInfo) {
        RestAssured
                .with().auth().preemptive().basic(username, "secret")
                .when().delete("/storage/{id}", fileInfo.getId())
                .then().assertThat().statusCode(NO_CONTENT.value());
    }
}
