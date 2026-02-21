package com.sila.share.annotation;

import com.sila.share.enums.ROLE;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreAuthorization {
    ROLE[] value(); // Accepts enum values now
}
