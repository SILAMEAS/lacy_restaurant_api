package com.sila.modules.upload.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public interface CloudinaryService {

    List<String> deleteImages(List<String> publicIds);


    Map<String, String> uploadFile(MultipartFile file); // Updated to return Map

    Map<String, String> uploadFileRemoveBG(MultipartFile file);

    String deleteImage(String publicId) throws IOException;

    String getBackgroundRemovedImage(String publicId);

    <T, I> List<I> uploadImagesToCloudinary(
            List<MultipartFile> imageFiles,
            T parent,
            BiFunction<String, String, I> imageFactory, // (url, publicId) -> new Image
            BiConsumer<I, T> setParent // (image, parent) -> image.setParent(parent)
    );

    <T, I> void updateEntityImages(
            T entity,
            List<MultipartFile> images,
            Function<T, List<I>> getImages,
            BiConsumer<T, List<I>> setImages,
            BiFunction<String, String, I> imageCreator,
            BiConsumer<I, T> setEntityRef,
            Function<I, String> getPublicId
    );

}
