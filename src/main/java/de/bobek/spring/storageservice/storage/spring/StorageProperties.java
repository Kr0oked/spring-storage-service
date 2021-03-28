package de.bobek.spring.storageservice.storage.spring;

import java.nio.file.Path;

import javax.validation.constraints.NotNull;

import de.bobek.spring.storageservice.storage.internal.file.StorageLocationProvider;
import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("storage")
@Validated
@Getter
@Setter
public class StorageProperties implements StorageLocationProvider {

    @NotNull
    private Path location;
}
