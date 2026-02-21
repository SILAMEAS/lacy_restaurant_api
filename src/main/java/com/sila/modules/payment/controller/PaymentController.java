package com.sila.modules.payment.controller;

import com.sila.modules.payment.PAYMENT_METHOD;
import com.sila.modules.payment.dto.PaymentResponse;
import com.sila.modules.payment.services.PaymentService;
import com.sila.share.annotation.PreAuthorization;
import com.sila.share.enums.ROLE;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment Controller", description = "User operations related to Payment")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    final PaymentService paymentService;

    @PreAuthorization({ROLE.USER})
    @PostMapping()
    public ResponseEntity<PaymentResponse> pay(@RequestParam Long orderId, @RequestParam PAYMENT_METHOD method) {
        return ResponseEntity.ok(paymentService.payForOrder(orderId, method));
    }
}
