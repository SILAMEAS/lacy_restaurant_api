package com.sila.share.annotation;

import com.sila.share.annotation.constraint.FileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.springframework.http.MediaType;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FileValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFile {
    String message() default "Invalid file";

    long maxSize() default 2 * 1024 * 1024; // 2MB

    String[] allowedTypes() default {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE};

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}