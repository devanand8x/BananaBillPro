package com.bananabill.repository;

import com.bananabill.model.Bill;
import com.bananabill.model.Farmer;
import com.bananabill.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillRepositoryTest {

    @Mock
    private BillRepository billRepository;

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
        testBill.setGrossWeight(new BigDecimal("100"));
        testBill.setNetAmount(new BigDecimal("5000"));
        testBill.setPaymentStatus(PaymentStatus.UNPAID);
        testBill.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByBillNumber_ShouldReturnBill() {
        when(billRepository.findByBillNumber("B001")).thenReturn(Optional.of(testBill));

        Optional<Bill> result = billRepository.findByBillNumber("B001");

        assertTrue(result.isPresent());
        assertEquals("B001", result.get().getBillNumber());
        verify(billRepository, times(1)).findByBillNumber("B001");
    }

    @Test
    void findByBillNumber_WhenNotFound_ShouldReturnEmpty() {
        when(billRepository.findByBillNumber("INVALID")).thenReturn(Optional.empty());

        Optional<Bill> result = billRepository.findByBillNumber("INVALID");

        assertFalse(result.isPresent());
    }

    @Test
    void findByFarmerId_ShouldReturnBillsList() {
        when(billRepository.findByFarmerId("farmer-1")).thenReturn(List.of(testBill));

        List<Bill> result = billRepository.findByFarmerId("farmer-1");

        assertEquals(1, result.size());
        assertEquals("farmer-1", result.get(0).getFarmerId());
    }

    @Test
    void existsByBillNumber_ShouldReturnTrue() {
        when(billRepository.existsByBillNumber("B001")).thenReturn(true);

        Boolean exists = billRepository.existsByBillNumber("B001");

        assertTrue(exists);
    }

    @Test
    void findRecentBills_ShouldReturnPagedResults() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(billRepository.findRecentBills(pageable)).thenReturn(List.of(testBill));

        List<Bill> result = billRepository.findRecentBills(pageable);

        assertEquals(1, result.size());
    }

    @Test
    void countByPaymentStatus_ShouldReturnCount() {
        when(billRepository.countByPaymentStatus(PaymentStatus.UNPAID)).thenReturn(5L);

        Long count = billRepository.countByPaymentStatus(PaymentStatus.UNPAID);

        assertEquals(5L, count);
    }

    @Test
    void countUnpaidBills_ShouldReturnCount() {
        when(billRepository.countUnpaidBills()).thenReturn(10L);

        Long count = billRepository.countUnpaidBills();

        assertEquals(10L, count);
    }

    @Test
    void findByCreatedAtBetween_ShouldReturnBills() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        when(billRepository.findByCreatedAtBetween(start, end)).thenReturn(List.of(testBill));

        List<Bill> result = billRepository.findByCreatedAtBetween(start, end);

        assertEquals(1, result.size());
    }
}
