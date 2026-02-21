package com.sila.modules.order.controller;

import com.sila.modules.order.dto.OrderResponse;
import com.sila.modules.order.services.OrderService;
import com.sila.share.annotation.PreAuthorization;
import com.sila.share.dto.req.PaginationRequest;
import com.sila.share.enums.ROLE;
import com.sila.share.pagination.EntityResponseHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order")
@RequiredArgsConstructor
public class OrderController {
    final OrderService orderService;

    @GetMapping
    public ResponseEntity<EntityResponseHandler<OrderResponse>> getOrder(@ModelAttribute PaginationRequest request) {
        return new ResponseEntity<>(orderService.getAll(request), HttpStatus.OK);
    }

    @PreAuthorization({ROLE.USER})
    @PostMapping("/cart/{cartId}")
    public ResponseEntity<OrderResponse> placeOrder(@PathVariable Long cartId) {
        return ResponseEntity.ok(orderService.placeOrder(cartId));
    }

    @PreAuthorization({ROLE.OWNER})
    @DeleteMapping("/bulk")
    public ResponseEntity<String> deleteAllOrderInRestaurantAsOwner() {
        return ResponseEntity.ok(orderService.deleteAllPlaceOrderInRestaurant());
    }

    @DeleteMapping("{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.deletePlaceOrder(orderId));
    }


}
