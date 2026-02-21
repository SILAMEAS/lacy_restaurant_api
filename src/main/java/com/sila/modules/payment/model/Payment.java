package com.sila.modules.payment.model;


import com.sila.modules.order.model.Order;
import com.sila.modules.payment.PAYMENT_METHOD;
import com.sila.modules.payment.PAYMENT_STATUS;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Order order;

    private PAYMENT_METHOD paymentMethod;

    private double amount;

    private LocalDateTime paidAt;

    private PAYMENT_STATUS status;
}
