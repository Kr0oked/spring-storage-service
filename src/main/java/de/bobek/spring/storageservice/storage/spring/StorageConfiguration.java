package de.bobek.spring.storageservice.storage.spring;

import de.bobek.spring.storageservice.common.TimeProvider;
import de.bobek.spring.storageservice.storage.api.StorageService;
import de.bobek.spring.storageservice.storage.internal.ContentStore;
import de.bobek.spring.storageservice.storage.internal.MetadataStore;
import de.bobek.spring.storageservice.storage.internal.StorageItemAdapter;
import de.bobek.spring.storageservice.storage.internal.StorageServiceImpl;
import de.bobek.spring.storageservice.storage.internal.db.DatabaseMetadataStore;
import de.bobek.spring.storageservice.storage.internal.db.ItemAdapter;
import de.bobek.spring.storageservice.storage.internal.db.ItemRepository;
import de.bobek.spring.storageservice.storage.internal.file.FileSystemContentStore;
import de.bobek.spring.storageservice.storage.internal.file.StorageLocationProvider;
import de.bobek.spring.storageservice.storage.web.FileInfoAdapter;
import de.bobek.spring.storageservice.storage.web.FileInfoPageAdapter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfiguration {

    @Bean
    public StorageService storageService(
            MetadataStore metadataStore,
            ContentStore contentStore,
            StorageItemAdapter storageItemAdapter) {
        return new StorageServiceImpl(metadataStore, contentStore, storageItemAdapter);
    }

    @Bean
    public StorageItemAdapter storageItemAdapter(ContentStore contentStore) {
        return new StorageItemAdapter(contentStore);
    }

    @Bean
    public MetadataStore metadataStore(
            ItemRepository itemRepository,
            ItemAdapter itemAdapter,
            TimeProvider timeProvider) {
        return new DatabaseMetadataStore(itemRepository, itemAdapter, timeProvider);
    }

    @Bean
    public ItemAdapter itemAdapter() {
        return new ItemAdapter();
    }

    @Bean
    public ContentStore contentStore(StorageLocationProvider storageLocationProvider) {
        return new FileSystemContentStore(storageLocationProvider);
    }

    @Bean
    public FileInfoPageAdapter fileInfoPageAdapter(FileInfoAdapter fileInfoAdapter) {
        return new FileInfoPageAdapter(fileInfoAdapter);
    }

    @Bean
    public FileInfoAdapter fileInfoAdapter() {
        return new FileInfoAdapter();
    }
}
