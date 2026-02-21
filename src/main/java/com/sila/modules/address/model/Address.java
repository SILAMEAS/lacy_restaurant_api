package com.sila.modules.address.model;

import com.sila.modules.profile.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty(message = "street can't empty")
    private String street;
    @NotEmpty(message = "name can't empty")
    private String name;
    @NotEmpty(message = "city can't empty")
    private String city;
    @NotEmpty(message = "state can't empty")
    private String state;
    @NotEmpty(message = "zip can't empty")
    private String zip;
    @NotEmpty(message = "country can't empty")
    private String country;
    private Boolean currentUsage = false;
    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_user_address"))
    @ManyToOne
    private User user;
}
