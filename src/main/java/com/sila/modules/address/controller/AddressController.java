package com.sila.modules.address.controller;

import com.sila.modules.address.dto.AddressRequest;
import com.sila.modules.address.dto.AddressResponse;
import com.sila.modules.address.services.AddressService;
import com.sila.share.annotation.PreAuthorization;
import com.sila.share.enums.ROLE;
import com.sila.share.method.OnUpdate;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Address Controller", description = "Address operations related to Cart")
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
@Slf4j
public class AddressController {
    private final AddressService addressService;

    @PreAuthorization({ROLE.USER})
    @PostMapping()
    ResponseEntity<AddressResponse> addAddress(
            @ModelAttribute @Valid AddressRequest addressRequest
    ) throws Exception {
        return addressService.add(addressRequest);
    }

    @PutMapping("/{id}")
    ResponseEntity<AddressResponse> updateAddress(
            @Validated(OnUpdate.class)
            @ModelAttribute AddressRequest addressRequest,
            @PathVariable Long id
    ) throws Exception {
        return new ResponseEntity<>(addressService.update(addressRequest, id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<String> deleteAddress(
            @PathVariable Long id
    ) throws Exception {
        return addressService.delete(id);
    }

    @GetMapping()
    ResponseEntity<List<AddressResponse>> getAddresses(

    ) {
        return addressService.gets();
    }

    @GetMapping("/{id}")
    ResponseEntity<AddressResponse> getAddressById(@PathVariable Long id

    ) {
        return addressService.byId(id);
    }
}
