package com.sila.modules.address.dto;

import com.sila.share.method.OnCreate;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {
    @NotEmpty(groups = OnCreate.class, message = "name is required")
    private String name;
    @NotEmpty(groups = OnCreate.class, message = "street is required")
    private String street;
    @NotEmpty(groups = OnCreate.class, message = "city is required")
    private String city;
    @NotEmpty(groups = OnCreate.class, message = "state is required")
    private String state;
    @NotEmpty(groups = OnCreate.class, message = "zip is required")
    private String zip;
    @NotEmpty(groups = OnCreate.class, message = "country is required")
    private String country;

    private Boolean currentUsage = false;
}
