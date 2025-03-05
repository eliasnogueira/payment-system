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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment createPaymentRequest(PaymentRequest paymentRequest) {
        Payment payment = new Payment();
        payment.setUniqueId(paymentRequest.getUniqueId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setTimestamp(paymentRequest.getTimestamp());
        return paymentRepository.save(payment);
    }

    public PaymentResponse processPayment(String uniqueId, String creditCardNumber, BigDecimal amount) {
        Payment payment = paymentRepository.findByUniqueId(uniqueId);
        if (payment == null) {
            return new PaymentResponse("FAILED", "Payment request not found", null, uniqueId);
        }

        if (!payment.getAmount().equals(amount)) {
            return new PaymentResponse("FAILED", "Amount does not match the payment request", payment.getAmount(), uniqueId);
        }

        if (!isValidCreditCard(creditCardNumber)) {
            return new PaymentResponse("FAILED", "Invalid credit card number", payment.getAmount(), uniqueId);
        }

        payment.setPaid(true);
        payment.setCreditCardNumber(creditCardNumber);
        paymentRepository.save(payment);

        return new PaymentResponse("SUCCESS", "Payment processed successfully", payment.getAmount(), uniqueId, true, creditCardNumber);
    }

    private boolean isValidCreditCard(String creditCardNumber) {
        return creditCardNumber != null && creditCardNumber.matches("\\d{16}");
    }
}
