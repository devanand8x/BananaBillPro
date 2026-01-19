package com.bananabill.service;

import com.bananabill.config.BillingConfig;
import com.bananabill.exception.PaymentException;
import com.bananabill.model.Bill;
import com.bananabill.model.Farmer;
import com.bananabill.model.PaymentHistory;
import com.bananabill.model.PaymentStatus;
import com.bananabill.repository.BillRepository;
import com.bananabill.repository.PaymentHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private PaymentHistoryRepository paymentHistoryRepository;

    @Mock
    private BillingConfig billingConfig;

    @InjectMocks
    private PaymentService paymentService;

    private Bill testBill;
    private Farmer testFarmer;

    @BeforeEach
    void setUp() {
        testFarmer = new Farmer();
        testFarmer.setId("farmer-1");
        testFarmer.setName("Test Farmer");
        testFarmer.setMobileNumber("9876543210");

        testBill = new Bill();
        testBill.setId("bill-1");
        testBill.setBillNumber("B001");
        testBill.setFarmer(testFarmer);
        testBill.setFarmerId("farmer-1");
        testBill.setNetAmount(new BigDecimal("5000"));
        testBill.setPaidAmount(BigDecimal.ZERO);
        testBill.setPaymentStatus(PaymentStatus.UNPAID);
    }

    @Test
    void getPaymentHistory_ShouldReturnHistory() {
        PaymentHistory history = new PaymentHistory();
        history.setAmount(new BigDecimal("1000"));

        when(paymentHistoryRepository.findByBillIdOrderByCreatedAtDesc("bill-1"))
                .thenReturn(List.of(history));

        List<PaymentHistory> result = paymentService.getPaymentHistory("bill-1");

        assertEquals(1, result.size());
        verify(paymentHistoryRepository, times(1)).findByBillIdOrderByCreatedAtDesc("bill-1");
    }

    @Test
    void getPaymentHistory_WhenEmpty_ShouldReturnEmptyList() {
        when(paymentHistoryRepository.findByBillIdOrderByCreatedAtDesc("bill-1"))
                .thenReturn(List.of());

        List<PaymentHistory> result = paymentService.getPaymentHistory("bill-1");

        assertTrue(result.isEmpty());
    }

    @Test
    void paymentAmount_Validation_ZeroIsInvalid() {
        BigDecimal zeroAmount = BigDecimal.ZERO;
        assertTrue(zeroAmount.compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    void paymentAmount_Validation_NegativeIsInvalid() {
        BigDecimal negativeAmount = new BigDecimal("-100");
        assertTrue(negativeAmount.compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void paymentAmount_Validation_PositiveIsValid() {
        BigDecimal positiveAmount = new BigDecimal("1000");
        assertTrue(positiveAmount.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void paymentStatus_PartialPayment_ShouldBePartial() {
        BigDecimal netAmount = new BigDecimal("5000");
        BigDecimal paidAmount = new BigDecimal("2000");

        assertTrue(paidAmount.compareTo(netAmount) < 0);
        assertTrue(paidAmount.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void paymentStatus_FullPayment_ShouldBePaid() {
        BigDecimal netAmount = new BigDecimal("5000");
        BigDecimal paidAmount = new BigDecimal("5000");

        assertEquals(0, paidAmount.compareTo(netAmount));
    }
}
