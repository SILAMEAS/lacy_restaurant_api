package com.sila.modules.address.services;

import com.sila.modules.address.dto.AddressRequest;
import com.sila.modules.address.dto.AddressResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AddressService {
    ResponseEntity<AddressResponse> add(AddressRequest addressRequest) throws Exception;

    ResponseEntity<String> delete(Long addressId) throws Exception;

    ResponseEntity<AddressResponse> byId(Long addressId);

    ResponseEntity<List<AddressResponse>> gets();

    AddressResponse update(AddressRequest addressRequest, Long addressId) throws Exception;

    List<AddressResponse> getByUser();
}
