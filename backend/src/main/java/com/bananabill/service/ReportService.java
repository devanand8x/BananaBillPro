package com.bananabill.service;

import com.bananabill.model.Bill;
import com.bananabill.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

        private final BillRepository billRepository;

        public ReportService(BillRepository billRepository) {
                this.billRepository = billRepository;
        }

        /**
         * Get monthly report for a specific year and month
         */
        public Map<String, Object> getMonthlyReport(int year, int month) {
                // Get start and end of month
                YearMonth yearMonth = YearMonth.of(year, month);
                LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
                LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

                // Get all bills for this month
                List<Bill> bills = billRepository.findByCreatedAtBetween(startOfMonth, endOfMonth);

                // Calculate statistics
                long totalBills = bills.size();

                double totalAmount = bills.stream()
                                .mapToDouble(bill -> bill.getNetAmount().doubleValue())
                                .sum();

                double averageAmount = totalBills > 0 ? totalAmount / totalBills : 0;

                // Total weight
                double totalWeight = bills.stream()
                                .mapToDouble(bill -> bill.getFinalNetWeight().doubleValue())
                                .sum();

                // Get farmer-wise breakdown
                Map<String, Map<String, Object>> farmerBreakdown = bills.stream()
                                .collect(Collectors.groupingBy(
                                                bill -> bill.getFarmer().getId(),
                                                Collectors.collectingAndThen(
                                                                Collectors.toList(),
                                                                farmerBills -> {
                                                                        Map<String, Object> farmerStats = new HashMap<>();
                                                                        farmerStats.put("name", farmerBills.get(0)
                                                                                        .getFarmer().getName());
                                                                        farmerStats.put("mobile", farmerBills.get(0)
                                                                                        .getFarmer().getMobileNumber());
                                                                        farmerStats.put("billCount",
                                                                                        farmerBills.size());
                                                                        farmerStats.put("totalAmount", farmerBills
                                                                                        .stream()
                                                                                        .mapToDouble(b -> b
                                                                                                        .getNetAmount()
                                                                                                        .doubleValue())
                                                                                        .sum());
                                                                        farmerStats.put("totalWeight", farmerBills
                                                                                        .stream()
                                                                                        .mapToDouble(b -> b
                                                                                                        .getFinalNetWeight()
                                                                                                        .doubleValue())
                                                                                        .sum());
                                                                        return farmerStats;
                                                                })));

                // Convert to list and sort by total amount (descending)
                List<Map<String, Object>> farmerList = new ArrayList<>(farmerBreakdown.values());
                farmerList.sort((a, b) -> Double.compare(
                                (Double) b.get("totalAmount"),
                                (Double) a.get("totalAmount")));

                // Build response
                Map<String, Object> report = new HashMap<>();
                report.put("year", year);
                report.put("month", month);
                report.put("monthName", yearMonth.getMonth().toString());
                report.put("totalBills", totalBills);
                report.put("totalAmount", Math.round(totalAmount * 100.0) / 100.0);
                report.put("averageAmount", Math.round(averageAmount * 100.0) / 100.0);
                report.put("totalWeight", Math.round(totalWeight * 100.0) / 100.0);
                report.put("farmers", farmerList);
                report.put("bills", bills);

                return report;
        }

        /**
         * Get available months that have bills
         */
        public List<Map<String, Object>> getAvailableMonths() {
                List<Bill> allBills = billRepository.findAll();

                return allBills.stream()
                                .map(bill -> {
                                        LocalDate date = bill.getCreatedAt().toLocalDate();
                                        return YearMonth.of(date.getYear(), date.getMonth());
                                })
                                .distinct()
                                .sorted(Comparator.reverseOrder())
                                .map(ym -> {
                                        Map<String, Object> monthInfo = new HashMap<>();
                                        monthInfo.put("year", ym.getYear());
                                        monthInfo.put("month", ym.getMonthValue());
                                        monthInfo.put("monthName", ym.getMonth().toString());
                                        monthInfo.put("label", ym.getMonth().toString() + " " + ym.getYear());
                                        return monthInfo;
                                })
                                .collect(Collectors.toList());
        }

        /**
         * Get report for a specific date range
         */
        public Map<String, Object> getDateRangeReport(LocalDate startDate, LocalDate endDate) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

                // Get all bills for this date range
                List<Bill> bills = billRepository.findByCreatedAtBetween(startDateTime, endDateTime);

                // Calculate statistics
                long totalBills = bills.size();

                double totalAmount = bills.stream()
                                .mapToDouble(bill -> bill.getNetAmount().doubleValue())
                                .sum();

                double averageAmount = totalBills > 0 ? totalAmount / totalBills : 0;

                // Total weight
                double totalWeight = bills.stream()
                                .mapToDouble(bill -> bill.getFinalNetWeight().doubleValue())
                                .sum();

                // Get farmer-wise breakdown
                Map<String, Map<String, Object>> farmerBreakdown = bills.stream()
                                .collect(Collectors.groupingBy(
                                                bill -> bill.getFarmer().getId(),
                                                Collectors.collectingAndThen(
                                                                Collectors.toList(),
                                                                farmerBills -> {
                                                                        Map<String, Object> farmerStats = new HashMap<>();
                                                                        farmerStats.put("name", farmerBills.get(0)
                                                                                        .getFarmer().getName());
                                                                        farmerStats.put("mobile", farmerBills.get(0)
                                                                                        .getFarmer().getMobileNumber());
                                                                        farmerStats.put("billCount",
                                                                                        farmerBills.size());
                                                                        farmerStats.put("totalAmount", farmerBills
                                                                                        .stream()
                                                                                        .mapToDouble(b -> b
                                                                                                        .getNetAmount()
                                                                                                        .doubleValue())
                                                                                        .sum());
                                                                        farmerStats.put("totalWeight", farmerBills
                                                                                        .stream()
                                                                                        .mapToDouble(b -> b
                                                                                                        .getFinalNetWeight()
                                                                                                        .doubleValue())
                                                                                        .sum());
                                                                        return farmerStats;
                                                                })));

                // Convert to list and sort by total amount (descending)
                List<Map<String, Object>> farmerList = new ArrayList<>(farmerBreakdown.values());
                farmerList.sort((a, b) -> Double.compare(
                                (Double) b.get("totalAmount"),
                                (Double) a.get("totalAmount")));

                // Build response
                Map<String, Object> report = new HashMap<>();
                report.put("startDate", startDate.toString());
                report.put("endDate", endDate.toString());
                report.put("totalBills", totalBills);
                report.put("totalAmount", Math.round(totalAmount * 100.0) / 100.0);
                report.put("averageAmount", Math.round(averageAmount * 100.0) / 100.0);
                report.put("totalWeight", Math.round(totalWeight * 100.0) / 100.0);
                report.put("farmers", farmerList);
                report.put("bills", bills);

                return report;
        }
}
