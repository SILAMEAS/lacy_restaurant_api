package com.sila.modules.profile.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sila.modules.address.model.Address;
import com.sila.modules.cart.model.Cart;
import com.sila.modules.chat.model.ChatMessage;
import com.sila.modules.chat.model.ChatRoom;
import com.sila.modules.favorite.model.Favorite;
import com.sila.modules.order.model.Order;
import com.sila.modules.resturant.model.Restaurant;
import com.sila.share.enums.ROLE;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString(exclude = {"favourites", "addresses", "cart"}) // exclude fields that can loop
@EntityListeners(AuditingEntityListener.class) // 👈 required for auditing
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String profile;
    private String email;
    private String password;
    private ROLE role = ROLE.USER;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "owner")
    private List<Favorite> favourites = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
    private List<Address> addresses = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Cart> carts = new ArrayList<>();

    @CreationTimestamp // 👈 auto-set on insert
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp // 👈 auto-set on update
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> sentMessages = new ArrayList<>();

    @ManyToMany(mappedBy = "members")
    private List<ChatRoom> chatRooms = new ArrayList<>();
}
