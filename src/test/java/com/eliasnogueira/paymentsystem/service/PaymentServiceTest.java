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
import com.eliasnogueira.paymentsystem.model.PaymentResponse;
import com.eliasnogueira.paymentsystem.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    public void testCreatePaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setUniqueId("12345");
        request.setAmount(new BigDecimal("100.0"));
        request.setTimestamp(LocalDateTime.now());

        Payment payment = new Payment();
        payment.setUniqueId(request.getUniqueId());
        payment.setAmount(request.getAmount());
        payment.setTimestamp(request.getTimestamp());

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.createPaymentRequest(request);
        assertNotNull(result);
        assertEquals("12345", result.getUniqueId());
    }

    @Test
    public void testProcessPayment_Success() {
        Payment payment = new Payment();
        payment.setUniqueId("12345");
        payment.setAmount(new BigDecimal("100.0"));

        when(paymentRepository.findByUniqueId("12345")).thenReturn(payment);

        PaymentResponse response = paymentService.processPayment("12345", "1234567890123456", new BigDecimal("100.0"));
        assertEquals("SUCCESS", response.getStatus());
        assertTrue(response.isPaid());
        assertEquals("1234567890123456", response.getCreditCardNumber()); // Ver
    }

    @Test
    public void testProcessPayment_InvalidCreditCard() {
        Payment payment = new Payment();
        payment.setUniqueId("12345");
        payment.setAmount(new BigDecimal("100.0"));

        when(paymentRepository.findByUniqueId("12345")).thenReturn(payment);

        PaymentResponse response = paymentService.processPayment("12345", "invalid", new BigDecimal("100.0"));
        assertEquals("FAILED", response.getStatus());
    }
}
