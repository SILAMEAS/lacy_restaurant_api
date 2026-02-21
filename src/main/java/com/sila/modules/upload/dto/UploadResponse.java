package com.sila.modules.upload.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadResponse {
    // Getters and setters
    private String publicId;
    private String secureUrl;

    // Constructors
    public UploadResponse() {
    }

    public UploadResponse(String publicId, String secureUrl) {
        this.publicId = publicId;
        this.secureUrl = secureUrl;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }
}