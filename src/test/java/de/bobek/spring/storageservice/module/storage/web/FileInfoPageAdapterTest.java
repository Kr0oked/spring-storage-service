package de.bobek.spring.storageservice.module.storage.web;

import java.util.List;

import de.bobek.spring.storageservice.module.storage.StorageItemTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static de.bobek.spring.storageservice.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileInfoPageAdapterTest {

    @InjectMocks
    private FileInfoPageAdapter fileInfoPageAdapter;

    @Mock
    private FileInfoAdapter fileInfoAdapter;

    @Test
    void adapt() {
        var storageItem = StorageItemTestUtils.getStorageItem();
        var storageItemPage = new PageImpl<>(List.of(storageItem), PageRequest.of(1, 2), 4L);
        var fileInfo = FileInfoTestUtils.getFileInfo();

        when(fileInfoAdapter.adapt(storageItem.getMetadata())).thenReturn(fileInfo);

        var fileInfoPage = fileInfoPageAdapter.adapt(storageItemPage);

        assertThat(fileInfoPage)
                .hasTotalPages(2)
                .hasTotalElements(4L)
                .hasNumber(1)
                .hasSize(2)
                .hasNumberOfElements(1)
                .hasOnlyContent(fileInfo);
    }
}
