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
package com.eliasnogueira.paymentsystem.controller;

import com.eliasnogueira.paymentsystem.model.Payment;
import com.eliasnogueira.paymentsystem.model.PaymentRequest;
import com.eliasnogueira.paymentsystem.model.PaymentResponse;
import com.eliasnogueira.paymentsystem.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void shouldSuccessfullyCreatePaymentRequest() {
        var paymentRequest = new PaymentRequest();
        paymentRequest.setUniqueId("12345");
        paymentRequest.setAmount(new BigDecimal("100.0"));

        var payment = new Payment();
        payment.setUniqueId("12345");
        payment.setAmount(new BigDecimal("100.0"));

        when(paymentService.createPaymentRequest(any())).thenReturn(payment);

        ResponseEntity<Payment> response = paymentController.createPaymentRequest(paymentRequest);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertAll(
                () -> assertEquals("12345", response.getBody().getUniqueId()),
                () -> assertEquals(new BigDecimal("100.0"), response.getBody().getAmount())
        );
    }

    @Test
    void shouldSuccessfullyProcessPaymentRequest() {
        var paymentResponse = new PaymentResponse("SUCCESS", "Payment processed successfully", new BigDecimal("100.0"), "12345");

        when(paymentService.
                processPayment("12345", "1234567890123456", new BigDecimal("100.0")))
                .thenReturn(paymentResponse);

        ResponseEntity<PaymentResponse> response = paymentController.
                processPayment("12345", "1234567890123456", new BigDecimal("100.0"));

        assertNotNull(response.getBody());
        assertAll(
                () -> assertEquals("SUCCESS", response.getBody().getStatus()),
                () -> assertEquals("Payment processed successfully", response.getBody().getMessage()),
                () -> assertEquals("12345", response.getBody().getUniqueId()),
                () -> assertEquals(new BigDecimal("100.0"), response.getBody().getAmount())
        );
    }
}
