package com.sila.modules.payment.services;

import com.sila.modules.payment.PAYMENT_METHOD;
import com.sila.modules.payment.dto.PaymentResponse;
import org.springframework.stereotype.Service;


@Service
public class PaymentImp implements PaymentService {
    @Override
    public PaymentResponse payForOrder(Long orderId, PAYMENT_METHOD method) {
        return null;
    }
}
