package com.sila.modules.resturant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sila.modules.address.model.Address;
import com.sila.modules.favorite.model.Favorite;
import com.sila.modules.food.model.Food;
import com.sila.modules.profile.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {
    public double deliveryFee = 0;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JsonIgnore
    private User owner;
    @NotEmpty(message = "name can't be empty")
    private String name;
    private String description;
    private String cuisineType;
    @ManyToOne
    @JoinColumn(name = "address_id", nullable = true)
    private Address address;
    @Embedded
    private ContactInformation contactInformation;
    private String openingHours;
    private LocalDateTime registrationDate;
    private boolean open;
    @JsonIgnore
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Food> foods = new ArrayList<>();
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ImageRestaurant> images = new ArrayList<>();
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Favorite> favorites;
    private int rating = 0;
    private double restaurantDiscount = 0;

    public void addImage(ImageRestaurant restaurant) {
        this.images.add(restaurant);
    }
}
