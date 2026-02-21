package com.sila.modules.category.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sila.modules.food.model.Food;
import com.sila.modules.resturant.model.Restaurant;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @JsonIgnore
    @ManyToOne
    private Restaurant restaurant;
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Food> foodList;

    private String url;

    private String publicId;
}
