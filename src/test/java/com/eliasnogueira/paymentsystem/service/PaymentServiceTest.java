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
package com.eliasnogueira.paymentsystem.service;

import com.eliasnogueira.paymentsystem.model.Payment;
import com.eliasnogueira.paymentsystem.model.PaymentRequest;
import com.eliasnogueira.paymentsystem.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private String uniqueId;

    @BeforeEach
    void setUp() {
        uniqueId = UUID.randomUUID().toString();
    }

    @Test
    void successfullyCreatePayment() {
        var request = new PaymentRequest();
        request.setUniqueId(uniqueId);
        request.setAmount(new BigDecimal("100.0"));
        request.setTimestamp(LocalDateTime.now());

        var payment = new Payment();
        payment.setUniqueId(request.getUniqueId());
        payment.setAmount(request.getAmount());
        payment.setTimestamp(request.getTimestamp());

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        var result = paymentService.createPaymentRequest(request);
        assertNotNull(result);
        assertAll("Successfully created payment", () -> {
            assertNull(result.getId());
            assertEquals(payment.getUniqueId(), result.getUniqueId());
            assertEquals(payment.getAmount(), result.getAmount());
            assertFalse(result.isPaid());
            assertNull(result.getCreditCardNumber());
            assertEquals(payment.getTimestamp(), result.getTimestamp());
        });
    }

    @Test
    void successfullyProcessPayment() {
        var payment = new Payment();
        payment.setUniqueId(uniqueId);
        payment.setAmount(new BigDecimal("100.0"));

        when(paymentRepository.findByUniqueId(uniqueId)).thenReturn(payment);

        String creditCardNumber = "1234567890123456";
        var paymentResponse = paymentService.
                processPayment(uniqueId, creditCardNumber, new BigDecimal("100.0"));

        assertAll("Payment request processed successfully", () -> {
            assertEquals("SUCCESS", paymentResponse.getStatus());
            assertEquals("Payment processed successfully", paymentResponse.getMessage());
            assertEquals(new BigDecimal("100.0"), paymentResponse.getAmount());
            assertEquals(uniqueId, paymentResponse.getUniqueId());
            assertEquals(creditCardNumber, paymentResponse.getCreditCardNumber());
            assertTrue(paymentResponse.isPaid());
        });
    }

    @Test
    void shouldNotProcessPaymentWhenCreditCardIsInvalid() {
        var payment = new Payment();
        payment.setUniqueId(uniqueId);
        payment.setAmount(new BigDecimal("100.0"));

        when(paymentRepository.findByUniqueId(uniqueId)).thenReturn(payment);

        var paymentResponse = paymentService.
                processPayment(uniqueId, "invalid", new BigDecimal("100.0"));

        assertAll("Invalid credit card number", () -> {
            assertEquals("FAILED", paymentResponse.getStatus());
            assertEquals("Invalid credit card number", paymentResponse.getMessage());
            assertEquals(new BigDecimal("100.0"), paymentResponse.getAmount());
            assertEquals(uniqueId, paymentResponse.getUniqueId());
            assertFalse(paymentResponse.isPaid());
            assertNull(paymentResponse.getCreditCardNumber());
        });
    }

    @Test
    void shouldNotProcessPaymentWhenPaymentRequestNotFound() {
        String notFoundUniqueId = UUID.randomUUID().toString();
        when(paymentRepository.findByUniqueId(notFoundUniqueId)).thenReturn(null);

        String creditCardNumber = "1234567890123456";
        var paymentResponse = paymentService.
                processPayment(notFoundUniqueId, creditCardNumber, new BigDecimal("100.0"));

        assertAll("Payment request not found", () -> {
            assertEquals("FAILED", paymentResponse.getStatus());
            assertEquals("Payment request not found", paymentResponse.getMessage());
            assertNull(paymentResponse.getAmount());
            assertEquals(notFoundUniqueId, paymentResponse.getUniqueId());
            assertFalse(paymentResponse.isPaid());
            assertNull(paymentResponse.getCreditCardNumber());
        });
    }

    @Test
    void shouldNotProcessPaymentAmountNotMatch() {
        var payment = new Payment();
        payment.setUniqueId(uniqueId);
        payment.setAmount(new BigDecimal("100.0"));

        when(paymentRepository.findByUniqueId(uniqueId)).thenReturn(payment);

        String creditCard = "1234567890123456";
        var paymentResponse = paymentService.
                processPayment(uniqueId, creditCard, new BigDecimal("200.0"));

        assertAll("Payment request not found", () -> {
            assertEquals("FAILED", paymentResponse.getStatus());
            assertEquals("Amount does not match the payment request", paymentResponse.getMessage());
            assertEquals(payment.getAmount(), paymentResponse.getAmount());
            assertEquals(uniqueId, paymentResponse.getUniqueId());
            assertFalse(paymentResponse.isPaid());
            assertNull(paymentResponse.getCreditCardNumber());
        });
    }
}
