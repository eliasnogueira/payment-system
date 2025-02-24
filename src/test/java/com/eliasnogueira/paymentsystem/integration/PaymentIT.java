/*
 * MIT License
 *
 * Copyright (c) 2025 Elias Nogueira
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.eliasnogueira.paymentsystem.integration;

import com.eliasnogueira.paymentsystem.model.Payment;
import com.eliasnogueira.paymentsystem.model.PaymentRequest;
import com.eliasnogueira.paymentsystem.repository.PaymentRepository;
import com.eliasnogueira.paymentsystem.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@ActiveProfiles("${spring.profiles.active}")
class PaymentIT {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setUniqueId(UUID.randomUUID().toString());
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCreditCardNumber("1234567890123456");
    }

    @Test
    void shouldSuccessfullyCreatePayment() {
        Payment savedPayment = paymentRepository.save(payment);
        assertAll("Successfully created payment",
                () -> assertEquals(payment.getUniqueId(), savedPayment.getUniqueId()),
                () -> assertEquals(payment.getAmount(), savedPayment.getAmount()),
                () -> assertFalse(savedPayment.isPaid()),
                () -> assertEquals(payment.getCreditCardNumber(), savedPayment.getCreditCardNumber()),
                () -> assertNull(savedPayment.getTimestamp()));
    }

    @Test
    void shouldSuccessfullyFindPayment() {
        Payment savedPayment = paymentRepository.save(payment);
        Payment paymentFound = paymentRepository.findByUniqueId(savedPayment.getUniqueId());

        assertAll("Successfully found payment",
                () -> assertEquals(savedPayment.getUniqueId(), paymentFound.getUniqueId()),
                () -> assertEquals(savedPayment.getAmount(), paymentFound.getAmount()),
                () -> assertFalse(paymentFound.isPaid()),
                () -> assertEquals(payment.getCreditCardNumber(), savedPayment.getCreditCardNumber()),
                () -> assertNull(paymentFound.getTimestamp()));
    }

    @Test
    void shouldSuccessfullyCreatePaymentRequest() {
        var paymentService = new PaymentService(paymentRepository);

        var paymentRequest = new PaymentRequest();
        paymentRequest.setUniqueId(payment.getUniqueId());
        paymentRequest.setAmount(payment.getAmount());
        paymentRequest.setTimestamp(LocalDateTime.now());

        var payment = paymentService.createPaymentRequest(paymentRequest);
        payment.setCreditCardNumber("1234567891234567");

        var paymentResponse = paymentService.
                processPayment(paymentRequest.getUniqueId(), payment.getCreditCardNumber(), paymentRequest.getAmount());

        assertAll("Full Payment Process",
                () -> assertEquals("SUCCESS", paymentResponse.getStatus()),
                () -> assertEquals(paymentResponse.getUniqueId(), paymentRequest.getUniqueId()),
                () -> assertEquals(paymentResponse.getAmount(), paymentRequest.getAmount()),
                () -> assertTrue(paymentResponse.isPaid()),
                () -> assertEquals(paymentResponse.getCreditCardNumber(), payment.getCreditCardNumber()),
                () -> assertEquals("Payment processed successfully", paymentResponse.getMessage()));
    }
}
