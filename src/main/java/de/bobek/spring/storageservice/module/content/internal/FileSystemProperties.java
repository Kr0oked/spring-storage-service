package de.bobek.spring.storageservice.module.content.internal;

import java.nio.file.Path;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("file-system")
@ConstructorBinding
@Validated
@RequiredArgsConstructor
@Getter
public class FileSystemProperties {

    @NotNull
    private final Path location;
}
