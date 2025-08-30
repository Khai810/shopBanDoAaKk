package com.projectshopbando.shopbandoapi.controllers;

import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import com.projectshopbando.shopbandoapi.services.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {
    private final UploadService uploadService;

    @GetMapping("/sign")
    public ResponseEntity<ResponseObject<?>> getSignature(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .data(uploadService.getSignature())
                        .build());
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<ResponseObject<?>> deleteFile(@PathVariable String publicId){
        uploadService.deleteFile(publicId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseObject.builder()
                        .message("File deleted")
                        .build());
    }
}
