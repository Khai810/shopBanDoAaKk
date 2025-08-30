package com.projectshopbando.shopbandoapi.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadService {
    private final String fileFolder = "products";
    private final Cloudinary cloudinary;

    public Map<String, String> getSignature() {
        long timestamp = System.currentTimeMillis() / 1000;

        Map<String, Object> paramsToSign = ObjectUtils.asMap(
                "timestamp", timestamp,
                "folder", fileFolder
        );

        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinary.config.apiSecret, 1);

        return Map.of(
                "timestamp", String.valueOf(timestamp),
                "signature", signature,
                "apiKey", cloudinary.config.apiKey,
                "cloudName", cloudinary.config.cloudName
        );

    }

    public boolean deleteFile(String publicId){
        try {
            Map result = cloudinary.uploader().destroy(fileFolder + "/" + publicId, ObjectUtils.emptyMap());
            if (result.get("result").equals("ok")) return true;
            else{
                throw new BadRequestException(result.get("result").toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
