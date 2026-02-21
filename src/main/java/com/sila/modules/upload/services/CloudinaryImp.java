package com.sila.modules.upload.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.sila.config.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryImp implements CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryImp.class);
    private final Cloudinary cloudinary;

    @Override
    public Map<String, String> uploadFile(MultipartFile file) {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            log.info("File uploaded successfully: {}", uploadResult);
            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");
            logger.info("File uploaded successfully with publicId: {} and secureUrl: {}", publicId, secureUrl);
            Map<String, String> result = new HashMap<>();
            result.put("publicId", publicId);
            result.put("secureUrl", secureUrl);
            return result;
        } catch (IOException e) {
            String errorMessage = String.format("Failed to upload file: %s", file.getOriginalFilename());
            logger.error(errorMessage, e);
            throw new BadRequestException(errorMessage);
        } catch (Exception e) {
            String errorMessage = String.format("Unexpected error occurred while uploading file: %s", file.getOriginalFilename());
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    public Map<String, String> uploadFileRemoveBG(MultipartFile file) {
        try {
            // Configure upload options with background removal
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "background_removal", "cloudinary_ai" // Use Cloudinary AI Background Removal add-on
            );

            // Upload the file with background removal
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
            log.info("File uploaded successfully with background removal: {}", uploadResult);

            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");
            logger.info("File uploaded successfully with publicId: {} and secureUrl: {}", publicId, secureUrl);

            Map<String, String> result = new HashMap<>();
            result.put("publicId", publicId);
            result.put("secureUrl", secureUrl);
            return result;
        } catch (IOException e) {
            String errorMessage = String.format("Failed to upload file: %s", file.getOriginalFilename());
            logger.error(errorMessage, e);
            throw new BadRequestException(errorMessage);
        } catch (Exception e) {
            String errorMessage = String.format("Unexpected error occurred while uploading file: %s", file.getOriginalFilename());
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    public String deleteImage(String publicId) {
        try {
            var result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            logger.info("Image deleted successfully: {}", publicId);
            return result.toString();
        } catch (IOException e) {
            String errorMessage = String.format("Failed to delete image with public ID: %s", publicId);
            logger.error(errorMessage, e);
            throw new BadRequestException(errorMessage + e);
        } catch (Exception e) {
            String errorMessage = String.format("Unexpected error occurred while deleting image with public ID: %s", publicId);
            logger.error(errorMessage, e);
            throw new BadRequestException(errorMessage + e);
        }
    }

    @Override
    public List<String> deleteImages(List<String> publicIds) {
        return publicIds.stream().map(publicId -> {
            try {
                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                logger.info("Image deleted successfully: {}", publicId);
                return "✅ Deleted: " + publicId + " - Result: " + result.toString();
            } catch (IOException e) {
                String errorMessage = String.format("Failed to delete image with public ID: %s", publicId);
                logger.error(errorMessage, e);
                return "❌ Failed: " + publicId + " - " + errorMessage;
            } catch (Exception e) {
                String errorMessage = String.format("Unexpected error occurred while deleting image with public ID: %s", publicId);
                logger.error(errorMessage, e);
                return "❌ Failed: " + publicId + " - " + errorMessage;
            }
        }).toList();
    }


    public String getBackgroundRemovedImage(String publicId) {
        try {
            // Generate URL with background removal transformation
            String url = cloudinary.url()
                    .transformation(new Transformation().effect("background_removal"))
                    .format("png") // Ensure the format supports transparency
                    .generate(publicId);
            logger.info("Generated background-removed image URL: {}", url);
            return url;
        } catch (Exception e) {
            String errorMessage = String.format("Failed to generate background-removed URL for publicId: %s", publicId);
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    public <T, I> List<I> uploadImagesToCloudinary(List<MultipartFile> imageFiles, T parent, BiFunction<String, String, I> imageFactory, BiConsumer<I, T> setParent) {
        return imageFiles.stream().map(file -> {
            Map<String, String> uploadResult = uploadFile(file);
            String imageUrl = uploadResult.get("secureUrl");
            String publicId = uploadResult.get("publicId");

            I image = imageFactory.apply(imageUrl, publicId);
            setParent.accept(image, parent);
            return image;
        }).toList();
    }

    @Override
    public <T, I> void updateEntityImages(
            T entity,
            List<MultipartFile> images,
            Function<T, List<I>> getImages,
            BiConsumer<T, List<I>> setImages,
            BiFunction<String, String, I> imageCreator,
            BiConsumer<I, T> setEntityRef,
            Function<I, String> getPublicId
    ) {
        if (!CollectionUtils.isEmpty(images)) {
            List<I> uploadedImages = uploadImagesToCloudinary(
                    images,
                    entity,
                    (url, publicId) -> {
                        I image = imageCreator.apply(url, publicId);
                        setEntityRef.accept(image, entity);
                        return image;
                    },
                    setEntityRef
            );

            if (!CollectionUtils.isEmpty(uploadedImages)) {
                List<I> existingImages = getImages.apply(entity);
                if (existingImages == null) {
                    setImages.accept(entity, new ArrayList<>());
                } else {
                    deleteImages(existingImages.stream()
                            .map(getPublicId)
                            .toList());
                    existingImages.clear();
                }
                getImages.apply(entity).addAll(uploadedImages);
            }
        }
    }
}