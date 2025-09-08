package com.projectshopbando.shopbandoapi;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.projectshopbando.shopbandoapi.services.UploadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadFileServiceTest {
    @Spy
    private Cloudinary cloudinary = new Cloudinary(Map.of(
            "cloud_name", "mycloud",
            "api_key", "123456",
            "api_secret", "secret"
    ));

    @Mock
    private Uploader uploader;

    @InjectMocks
    private UploadService cloudinaryService;

    @Test
    void testGetSignature() {
            when(cloudinary.apiSignRequest(anyMap(), anyString(), anyInt())).thenReturn("signed123");
            Map<String, String> signature = cloudinaryService.getSignature();

            assertThat(signature).containsEntry("signature", "signed123");
            assertThat(signature).containsEntry("apiKey", "123456");
            assertThat(signature).containsEntry("cloudName", "mycloud");
    }

    @Test
    void testDeleteFileSuccess() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(anyString(), anyMap())).thenReturn(Map.of("result", "ok"));

        boolean result = cloudinaryService.deleteFile("testImage");

        assertThat(result).isTrue();
        verify(uploader).destroy(eq("products/testImage"), anyMap());
    }

    @Test
    void testDeleteFileThrowsException() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(anyString(), anyMap())).thenThrow(new IOException("Delete failed"));

        assertThrows(RuntimeException.class, () -> cloudinaryService.deleteFile("badImage"));

        verify(uploader).destroy(eq("products/badImage"), anyMap());
    }
}
