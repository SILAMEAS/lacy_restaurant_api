package com.sila.modules.resturant.dto;

import com.sila.modules.address.model.Address;
import com.sila.modules.resturant.model.ContactInformation;
import com.sila.share.method.OnCreate;
import com.sila.share.method.OnUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class RestaurantRequest {
    @NotEmpty(message = "name is required")
    private String name;

    @NotEmpty(message = "description is required")
    private String description;

    private String cuisineType;

    @Valid
    @NotNull(groups = OnCreate.class, message = "address is required during create")
    private Address address;

    @Valid
    @NotNull(groups = OnCreate.class, message = "contactInformation is required")
    private ContactInformation contactInformation;

    @NotEmpty(message = "openingHours is required")
    private String openingHours;

    @NotEmpty(groups = OnCreate.class, message = "images is required")
    private List<MultipartFile> images;

    @NotNull
    private Boolean open; // use Boolean to allow null-check validation

    private String ownerName;

    @Min(value = 0, message = "Discount must be a positive value.", groups = OnUpdate.class)
    @DecimalMax(value = "99.99", message = "You can't set a 100% discount or more.", groups = OnUpdate.class)
    private Double discount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Delivery fee must be 0 or more", groups = OnUpdate.class)
    @DecimalMax(value = "5.0", inclusive = true, message = "Delivery fee must be 5 or less", groups = OnUpdate.class)
    private Double deliveryFee;
}
