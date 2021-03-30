package de.bobek.spring.storageservice.module.storage.internal.file;

import java.nio.file.Path;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties("storage.file-system")
@Validated
@Getter
@Setter
public class FileSystemProperties implements StorageLocationProvider {

    @NotNull
    private Path location;
}
