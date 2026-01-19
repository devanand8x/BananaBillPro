import apiClient from './apiClient';
import { AxiosError } from 'axios';

// Types matching backend DTOs
export interface Farmer {
  id: string;
  name: string;
  mobileNumber: string;
  address?: string;
}

export interface Bill {
  id: string;
  billNumber: string;
  farmerId: string;
  farmer: {
    id: string;
    name: string;
    mobile: string;
    village?: string;
  };
  vehicleNumber?: string;
  weight: {
    grossWeight: number;
    pattiWeight: number;
    boxCount: number;
    netWeight: number;
    dandaWeight: number;
    tutWastage: number;
    finalNetWeight: number;
  };
  payment: {
    ratePerKg: number;
    totalAmount: number;
    majuri: number;
    netAmount: number;
    status: 'UNPAID' | 'PAID' | 'PARTIAL';
    paidAmount?: number;
    paymentDate?: string;
  };
  createdAt: string;

  // Flattened accessors for backward compatibility
  grossWeight: number;
  pattiWeight: number;
  boxCount: number;
  netWeight: number;
  dandaWeight: number;
  tutWastage: number;
  finalNetWeight: number;
  ratePerKg: number;
  totalAmount: number;
  majuri: number;
  netAmount: number;
  paymentStatus: 'UNPAID' | 'PAID' | 'PARTIAL';
  paidAmount?: number;
  dueDate?: string;
  updatedAt: string;
}

export type BillWithFarmer = Bill;

// Backend response type for transformation
interface BackendBillResponse {
  id: string;
  billNumber: string;
  farmerId?: string;
  farmer?: {
    id?: string;
    name?: string;
    mobile?: string;
    mobileNumber?: string;
    village?: string;
    address?: string;
  };
  vehicleNumber?: string;
  weight?: {
    grossWeight?: number;
    pattiWeight?: number;
    boxCount?: number;
    netWeight?: number;
    dandaWeight?: number;
    tutWastage?: number;
    finalNetWeight?: number;
  };
  payment?: {
    ratePerKg?: number;
    totalAmount?: number;
    majuri?: number;
    netAmount?: number;
    status?: 'UNPAID' | 'PAID' | 'PARTIAL';
    paymentStatus?: 'UNPAID' | 'PAID' | 'PARTIAL';
    paidAmount?: number;
  };
  grossWeight?: number;
  pattiWeight?: number;
  boxCount?: number;
  netWeight?: number;
  dandaWeight?: number;
  tutWastage?: number;
  finalNetWeight?: number;
  ratePerKg?: number;
  totalAmount?: number;
  majuri?: number;
  netAmount?: number;
  paymentStatus?: 'UNPAID' | 'PAID' | 'PARTIAL';
  paidAmount?: number;
  createdAt: string;
}

export interface CreateBillInput {
  farmerId: string;
  vehicleNumber?: string | null;
  grossWeight: number;
  pattiWeight: number;
  boxCount: number;
  tutWastage: number;
  ratePerKg: number;
  majuri: number;
}

// ========== FARMER SERVICE ==========
export const farmerService = {
  async findByMobile(mobile: string): Promise<Farmer | null> {
    try {
      const response = await apiClient.get(`/farmers/mobile/${mobile}`);
      return response.data.data || response.data;
    } catch (error: unknown) {
      const axiosError = error as AxiosError;
      if (axiosError.response?.status === 404) return null;
      throw error;
    }
  },

  async upsert(farmer: { mobileNumber: string; name: string; address?: string }): Promise<Farmer> {
    const response = await apiClient.post('/farmers', farmer);
    return response.data.data || response.data;
  },

  async getAll(): Promise<Farmer[]> {
    const response = await apiClient.get('/farmers');
    return response.data.data || response.data;
  }
};

// Transform nested backend response to flat frontend structure
function transformBillResponse(backendBill: BackendBillResponse): Bill {
  // Handle both nested (from /bills/recent) and flat (from /bills/search-with-filters) structures
  const isNested = backendBill.weight && backendBill.payment;

  const farmer = backendBill.farmer ?? {};
  const weight = isNested ? (backendBill.weight ?? {}) : backendBill;
  const payment = isNested ? (backendBill.payment ?? {}) : backendBill;

  return {
    id: backendBill.id,
    billNumber: backendBill.billNumber,
    farmerId: farmer.id ?? backendBill.farmerId ?? '',
    farmer: {
      id: farmer.id ?? '',
      name: farmer.name ?? '',
      mobile: farmer.mobile ?? farmer.mobileNumber ?? '',
      village: farmer.village ?? farmer.address ?? '',
    },
    vehicleNumber: backendBill.vehicleNumber,
    grossWeight: weight.grossWeight ?? 0,
    pattiWeight: weight.pattiWeight ?? 0,
    boxCount: weight.boxCount ?? 0,
    netWeight: weight.netWeight ?? 0,
    dandaWeight: weight.dandaWeight ?? 0,
    tutWastage: weight.tutWastage ?? 0,
    finalNetWeight: weight.finalNetWeight ?? 0,
    ratePerKg: payment.ratePerKg ?? 0,
    totalAmount: payment.totalAmount ?? 0,
    majuri: payment.majuri ?? 0,
    netAmount: payment.netAmount ?? 0,
    paymentStatus: backendBill.paymentStatus ?? payment.paymentStatus ?? 'UNPAID',
    paidAmount: payment.paidAmount ?? 0,
    createdAt: backendBill.createdAt,
    updatedAt: backendBill.createdAt,
  } as Bill;
}

// ========== BILL SERVICE ==========
export const billService = {
  async create(billData: CreateBillInput): Promise<Bill> {
    const response = await apiClient.post('/bills', billData);
    const backendBill = response.data.data || response.data;
    return transformBillResponse(backendBill);
  },

  async getById(id: string): Promise<Bill | null> {
    try {
      const response = await apiClient.get(`/bills/${id}`);
      const backendBill = response.data.data || response.data;
      return transformBillResponse(backendBill);
    } catch (error: unknown) {
      const axiosError = error as AxiosError;
      if (axiosError.response?.status === 404) return null;
      throw error;
    }
  },

  async getByBillNumber(billNumber: string): Promise<Bill | null> {
    try {
      const response = await apiClient.get(`/bills/number/${billNumber}`);
      const backendBill = response.data.data || response.data;
      return transformBillResponse(backendBill);
    } catch (error: unknown) {
      const axiosError = error as AxiosError;
      if (axiosError.response?.status === 404) return null;
      throw error;
    }
  },

  async getByFarmerMobile(mobile: string): Promise<Bill[]> {
    try {
      const response = await apiClient.get(`/bills/farmer/${mobile}`);
      const backendBills = response.data.data || response.data;
      return backendBills.map(transformBillResponse);
    } catch (error) {
      return [];
    }
  },

  async getTodayCount(): Promise<number> {
    const response = await apiClient.get('/bills/stats/today');
    const data = response.data.data || response.data;
    return data.count;
  },

  async getTotalCount(): Promise<number> {
    const response = await apiClient.get('/bills/stats/total');
    const data = response.data.data || response.data;
    return data.count;
  },

  async getRecent(limit = 10): Promise<Bill[]> {
    const response = await apiClient.get(`/bills/recent?limit=${limit}`);
    const backendBills = response.data.data || response.data;
    return backendBills.map(transformBillResponse);
  },

  async sendToWhatsApp(billId: string, imageUrl: string): Promise<{ message: string; phone: string }> {
    const response = await apiClient.post(`/bills/${billId}/send-whatsapp`, { imageUrl });
    return response.data.data || response.data;
  },

  async update(id: string, billData: CreateBillInput): Promise<Bill> {
    const response = await apiClient.put(`/bills/${id}`, billData);
    return response.data.data || response.data;
  },

  async delete(id: string): Promise<void> {
    await apiClient.delete(`/bills/${id}`);
  },

  async getByDateRange(startDate: string, endDate: string): Promise<{
    bills: Bill[];
    totalAmount: number;
    totalWeight: number;
  }> {
    const response = await apiClient.get('/bills/filter', {
      params: { startDate, endDate }
    });
    return response.data.data || response.data;
  },

  async searchWithFilters(filters: {
    mobile?: string;
    startDate?: string;
    endDate?: string;
    paymentStatus?: string;
  }): Promise<Bill[]> {
    const params = new URLSearchParams();
    if (filters.mobile) params.append('mobileNumber', filters.mobile);
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    if (filters.paymentStatus) params.append('paymentStatus', filters.paymentStatus);

    const queryString = params.toString();
    const url = `/bills/search-with-filters${queryString ? `?${queryString}` : ''}`;
    console.log('SearchWithFilters URL:', url);
    const response = await apiClient.get(url);
    const data = response.data.data || response.data;
    console.log('SearchWithFilters response:', data);

    // Backend returns { bills: [...], count: N, ... }
    // We need just the bills array
    if (data.bills && Array.isArray(data.bills)) {
      console.log('Transforming', data.bills.length, 'bills');
      return data.bills.map(transformBillResponse);
    }
    console.log('Data is not in expected format, returning empty or direct array');
    return Array.isArray(data) ? data.map(transformBillResponse) : [];
  },
};

// ========== REPORT SERVICE ==========
export const reportService = {
  async getMonthlyReport(year: number, month: number): Promise<{
    totalBills: number;
    totalAmount: number;
    totalWeight: number;
    averageAmount: number;
    farmers: Array<{ name: string; mobile: string; billCount: number; totalAmount: number; totalWeight: number }>;
    bills: Bill[];
  }> {
    const response = await apiClient.get('/reports/monthly', {
      params: { year, month }
    });
    return response.data.data || response.data;
  },

  async getAvailableMonths(): Promise<Array<{ year: number; month: number; label: string }>> {
    const response = await apiClient.get('/reports/available-months');
    return response.data.data || response.data;
  },

  async sendStatementToWhatsApp(
    mobileNumber: string,
    farmerName: string,
    billCount: number,
    totalAmount: number,
    imageUrl: string
  ): Promise<{ success: boolean; message: string }> {
    const response = await apiClient.post('/reports/send-statement-whatsapp', null, {
      params: { mobileNumber, farmerName, billCount, totalAmount, imageUrl }
    });
    return response.data.data || response.data;
  },

  async getDateRangeReport(startDate: string, endDate: string): Promise<{ bills: Bill[]; totalAmount: number; totalWeight: number }> {
    const response = await apiClient.get('/reports/date-range', {
      params: { startDate, endDate }
    });
    return response.data.data || response.data;
  },
};

// ========== PAYMENT SERVICE ==========
export const paymentService = {
  async markAsPaid(billId: string): Promise<Bill> {
    const response = await apiClient.post(`/bills/${billId}/mark-paid`);
    return response.data.data || response.data;
  },

  async recordPayment(billId: string, amount: number): Promise<Bill> {
    const response = await apiClient.post(`/bills/${billId}/record-payment`, null, {
      params: { amount }
    });
    return response.data.data || response.data;
  },

  async getUnpaidBills(): Promise<Bill[]> {
    const response = await apiClient.get('/bills/unpaid');
    return response.data.data || response.data;
  },

  async getOverdueBills(): Promise<Bill[]> {
    const response = await apiClient.get('/bills/overdue');
    return response.data.data || response.data;
  },

  async getUnpaidStats(): Promise<{ count: number; totalAmount: number }> {
    const response = await apiClient.get('/bills/stats/unpaid');
    return response.data.data || response.data;
  },

  async setDueDate(billId: string, dueDate: string): Promise<Bill> {
    const response = await apiClient.post(`/bills/${billId}/set-due-date`, null, {
      params: { dueDate }
    });
    return response.data.data || response.data;
  },

  async sendPaymentConfirmation(billId: string): Promise<{ success: boolean; message: string }> {
    const response = await apiClient.post(`/bills/${billId}/send-confirmation`);
    return response.data.data || response.data;
  },
};

// ========== FARMER REPORT SERVICE ==========
export interface FarmerReportData {
  farmer: Farmer;
  bills: Bill[];
  totalBills: number;
  totalAmount: number;
  totalWeight: number;
  unpaidAmount: number;
  unpaidBills: number;
  isFiltered: boolean;
  totalBillsUnfiltered?: number;
}

export const farmerReportService = {
  async getFarmerReport(farmerId: string, filters?: {
    startDate?: string;
    endDate?: string;
    paymentStatus?: string;
  }): Promise<FarmerReportData> {
    const params = new URLSearchParams();
    if (filters?.startDate) params.append('startDate', filters.startDate);
    if (filters?.endDate) params.append('endDate', filters.endDate);
    if (filters?.paymentStatus) params.append('paymentStatus', filters.paymentStatus);

    const queryString = params.toString();
    const url = `/bills/farmer-report/${farmerId}${queryString ? `?${queryString}` : ''}`;
    const response = await apiClient.get(url);
    return response.data.data || response.data;
  },
};
