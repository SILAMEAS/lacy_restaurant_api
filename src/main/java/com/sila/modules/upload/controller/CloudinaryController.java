package com.sila.modules.upload.controller;

import com.sila.config.exception.BadRequestException;
import com.sila.modules.upload.KeyUploadResponse;
import com.sila.modules.upload.dto.UploadResponse;
import com.sila.modules.upload.services.CloudinaryService;
import com.sila.share.annotation.ValidFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@Tag(name = "Cloudinary Controller", description = "Operations related to cloudinary")
@RequiredArgsConstructor
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadImage(@RequestParam("file") @ValidFile(
            allowedTypes = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE}) MultipartFile file) {
        try {
            Map<String, String> uploadResult = cloudinaryService.uploadFile(file);
            UploadResponse response = new UploadResponse(
                    uploadResult.get(KeyUploadResponse.publicId.toString()),
                    uploadResult.get(KeyUploadResponse.secureUrl.toString())
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<String> deleteImage(@PathVariable String publicId) {
        try {
            String result = cloudinaryService.deleteImage(publicId);
            return ResponseEntity.ok("✅ Image deleted: " + result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error deleting image: " + e.getMessage());
        }
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<List<String>> deleteImages(@RequestBody List<String> publicIds) {
        try {
            List<String> results = cloudinaryService.deleteImages(publicIds);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of("❌ Error deleting images: " + e.getMessage()));
        }
    }

    @GetMapping("/remove-background/{publicId}")
    public ResponseEntity<String> getBackgroundRemovedImage(@PathVariable String publicId) {
        String url = cloudinaryService.getBackgroundRemovedImage(publicId);
        return ResponseEntity.ok(url);
    }
}