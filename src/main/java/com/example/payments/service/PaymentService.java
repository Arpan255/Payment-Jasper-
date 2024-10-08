package com.example.payments.service;

import com.example.payments.dto.Paymentdto;
import com.example.payments.model.Payment;
import com.example.payments.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import net.sf.jasperreports.engine.*;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    public Payment initiatePayment(Paymentdto payment) {
        Payment p=Payment.builder()
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .username(payment.getUsername())
                .ponumber(payment.getPonumber())
                .invoicenumber(payment.getInvoicenumber())
                .targetBankAccount(payment.getTargetBankAccount())
                .tds(payment.getTds())
                .sourceBankAccount(payment.getSourceBankAccount())
                .status(payment.getStatus())
                .paymentdate(payment.getPaymentdate())
                .build();
        return paymentRepository.save(p);
    }
    // Method to initiate a list of payments
    public List<Payment> initiatePayments(List<Paymentdto> payments) {
        List<Payment> paymentList = payments.stream().map(payment -> Payment.builder()
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .username(payment.getUsername())
                .ponumber(payment.getPonumber())
                .invoicenumber(payment.getInvoicenumber())
                .targetBankAccount(payment.getTargetBankAccount())
                .tds(payment.getTds())
                .sourceBankAccount(payment.getSourceBankAccount())
                .status(payment.getStatus())
                .paymentdate(payment.getPaymentdate())
                .build()).collect(Collectors.toList());

        return paymentRepository.saveAll(paymentList);
    }
    // 1. Find pending payments
    public List<Payment> findPendingPayments() {
        return paymentRepository.findByStatus("PENDING");
    }

    // 2. Find total amount
    public Double getTotalAmount() {
        return paymentRepository.sumAllAmounts();
    }

    // 3. Find amount by invoice number
    public Double getAmountByInvoiceNumber(String invoiceNumber) {
        Payment payment = paymentRepository.findByInvoicenumber(invoiceNumber);
        return payment != null ? payment.getAmount() : 0.0;
    }

    // 4. Find complete and pending payments by payment date
    public Map<String, List<Payment>> getPaymentsByStatusAndDate(String paymentDate) {
        Map<String, List<Payment>> paymentsByStatus = new HashMap<>();
        paymentsByStatus.put("completed", paymentRepository.findByPaymentdateAndStatus(paymentDate, "PAID"));
        paymentsByStatus.put("pending", paymentRepository.findByPaymentdateAndStatus(paymentDate, "PENDING"));
        return paymentsByStatus;
    }

    // 5. Edit payment
    public Payment editPayment(String id, Paymentdto paymentdto) {
        Optional<Payment> optionalPayment = paymentRepository.findById(id);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setAmount(paymentdto.getAmount());
            payment.setCurrency(paymentdto.getCurrency());
            payment.setUsername(paymentdto.getUsername());
            payment.setPonumber(paymentdto.getPonumber());
            payment.setInvoicenumber(paymentdto.getInvoicenumber());
            payment.setTargetBankAccount(paymentdto.getTargetBankAccount());
            payment.setSourceBankAccount(paymentdto.getSourceBankAccount());
            payment.setTds(paymentdto.getTds());
            payment.setStatus(paymentdto.getStatus());
            payment.setPaymentdate(paymentdto.getPaymentdate());
            return paymentRepository.save(payment);
        }
        throw new RuntimeException("Payment not found");
    }

    // 6. Delete payment
    public void deletePayment(String id) {
        paymentRepository.deleteById(id);
    }

    //7.minus tax
    public double getTotalAmountMinusTds(String invoicenumber) {
         Payment p= paymentRepository.findByInvoicenumber(invoicenumber);
         int td=p.getTds();
         double am=p.getAmount();
         double x= (td/100.0)*am;
         return am-x;


    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

}
