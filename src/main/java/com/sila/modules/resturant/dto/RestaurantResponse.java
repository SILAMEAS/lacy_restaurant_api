package com.sila.modules.resturant.dto;

import com.sila.modules.address.dto.AddressResponse;
import com.sila.share.dto.res.ImageDetailsResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantResponse {
    private Long id;
    private String name;
    private String description;
    private String cuisineType;
    private boolean open;
    private String openingHours;
    private LocalDateTime registrationDate;
    private AddressResponse address;
    private ContactInformationResponse contactInformation;
    private List<ImageDetailsResponse> imageUrls;
    private int rating;
    private String ownerName;

    private Long ownerId;


    private double deliveryFee;

    private double discount;
}