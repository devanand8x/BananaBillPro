package com.bananabill.controller;

import com.bananabill.dto.CreateBillRequest;
import com.bananabill.model.Bill;
import com.bananabill.model.Farmer;
import com.bananabill.model.PaymentStatus;
import com.bananabill.service.BillService;
import com.bananabill.service.WhatsAppService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = { "USER" })
class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillService billService;

    @MockBean
    private WhatsAppService whatsAppService;

    private Bill testBill;
    private CreateBillRequest createRequest;

    @BeforeEach
    void setUp() {
        // Create test farmer
        Farmer farmer = new Farmer();
        farmer.setId("farmer-1");
        farmer.setName("Test Farmer");
        farmer.setMobileNumber("9876543210");

        // Create test bill
        testBill = new Bill();
        testBill.setId("bill-1");
        testBill.setBillNumber("B001");
        testBill.setFarmer(farmer);
        testBill.setGrossWeight(new BigDecimal("100"));
        testBill.setPattiWeight(new BigDecimal("5"));
        testBill.setBoxCount(2);
        testBill.setNetWeight(new BigDecimal("93"));
        testBill.setDandaWeight(new BigDecimal("6.51"));
        testBill.setTutWastage(new BigDecimal("0"));
        testBill.setFinalNetWeight(new BigDecimal("99.51"));
        testBill.setRatePerKg(new BigDecimal("50"));
        testBill.setTotalAmount(new BigDecimal("4975.50"));
        testBill.setMajuri(new BigDecimal("100"));
        testBill.setNetAmount(new BigDecimal("4875.50"));
        testBill.setPaymentStatus(PaymentStatus.UNPAID);
        testBill.setCreatedAt(LocalDateTime.now());

        // Create test request
        createRequest = new CreateBillRequest();
        createRequest.setFarmerId("farmer-1");
        createRequest.setGrossWeight(new BigDecimal("100"));
        createRequest.setPattiWeight(new BigDecimal("5"));
        createRequest.setBoxCount(2);
        createRequest.setTutWastage(new BigDecimal("0"));
        createRequest.setRatePerKg(new BigDecimal("50"));
        createRequest.setMajuri(new BigDecimal("100"));
    }

    @Test
    void createBill_ShouldReturnCreatedBill() throws Exception {
        when(billService.createBill(any(CreateBillRequest.class))).thenReturn(testBill);

        mockMvc.perform(post("/bills")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bill created successfully"))
                .andExpect(jsonPath("$.data.billNumber").value("B001"));

        verify(billService, times(1)).createBill(any(CreateBillRequest.class));
    }

    @Test
    void getBillById_ShouldReturnBill() throws Exception {
        when(billService.getBillById("bill-1")).thenReturn(testBill);

        mockMvc.perform(get("/bills/bill-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("bill-1"))
                .andExpect(jsonPath("$.data.billNumber").value("B001"));

        verify(billService, times(1)).getBillById("bill-1");
    }

    @Test
    void updateBill_ShouldReturnUpdatedBill() throws Exception {
        when(billService.updateBill(eq("bill-1"), any(CreateBillRequest.class))).thenReturn(testBill);

        mockMvc.perform(put("/bills/bill-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bill updated successfully"));

        verify(billService, times(1)).updateBill(eq("bill-1"), any(CreateBillRequest.class));
    }

    @Test
    void deleteBill_ShouldReturnSuccess() throws Exception {
        doNothing().when(billService).deleteBill("bill-1");

        mockMvc.perform(delete("/bills/bill-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bill deleted successfully"));

        verify(billService, times(1)).deleteBill("bill-1");
    }

    @Test
    void getRecentBills_ShouldReturnBillsList() throws Exception {
        when(billService.getRecentBills(10)).thenReturn(List.of(testBill));

        mockMvc.perform(get("/bills/recent?limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].billNumber").value("B001"));

        verify(billService, times(1)).getRecentBills(10);
    }

    @Test
    void getTodayStats_ShouldReturnCount() throws Exception {
        when(billService.getTodayBillsCount()).thenReturn(5L);

        mockMvc.perform(get("/bills/stats/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.count").value(5));

        verify(billService, times(1)).getTodayBillsCount();
    }

    @Test
    void getTotalStats_ShouldReturnCount() throws Exception {
        when(billService.getTotalBillsCount()).thenReturn(100L);

        mockMvc.perform(get("/bills/stats/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.count").value(100));

        verify(billService, times(1)).getTotalBillsCount();
    }

    @Test
    void markAsPaid_ShouldReturnUpdatedBill() throws Exception {
        testBill.setPaymentStatus(PaymentStatus.PAID);
        when(billService.markAsPaid("bill-1")).thenReturn(testBill);

        mockMvc.perform(post("/bills/bill-1/mark-paid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bill marked as paid"));

        verify(billService, times(1)).markAsPaid("bill-1");
    }

    @Test
    void recordPayment_ShouldReturnUpdatedBill() throws Exception {
        when(billService.recordPayment(eq("bill-1"), any(BigDecimal.class))).thenReturn(testBill);

        mockMvc.perform(post("/bills/bill-1/record-payment?amount=1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment recorded"));

        verify(billService, times(1)).recordPayment(eq("bill-1"), any(BigDecimal.class));
    }

    @Test
    void searchBillsWithFilters_ShouldReturnFilteredBills() throws Exception {
        Map<String, Object> result = Map.of(
                "bills", List.of(testBill),
                "count", 1);
        when(billService.searchBillsWithFilters(anyString(), any(), any(), anyString()))
                .thenReturn(result);

        mockMvc.perform(get("/bills/search-with-filters")
                .param("mobileNumber", "9876543210")
                .param("paymentStatus", "UNPAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.count").value(1));

        verify(billService, times(1)).searchBillsWithFilters(anyString(), any(), any(), anyString());
    }
}
