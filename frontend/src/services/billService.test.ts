import { describe, it, expect, vi } from 'vitest';
import { billService, farmerService, paymentService } from './billService';
import apiClient from './apiClient';

// Mock apiClient
vi.mock('./apiClient');

describe('billService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('create', () => {
        it('creates a bill successfully', async () => {
            const mockBillData = {
                farmerId: 'farmer-1',
                vehicleNumber: 'MH01AB1234',
                grossWeight: 100,
                pattiWeight: 5,
                boxCount: 2,
                tutWastage: 3,
                ratePerKg: 50,
                majuri: 350,
            };

            const mockResponse = {
                data: {
                    data: {
                        id: 'bill-1',
                        billNumber: 'B001',
                        ...mockBillData,
                        createdAt: '2024-01-01T00:00:00Z',
                    },
                },
            };

            vi.mocked(apiClient.post).mockResolvedValue(mockResponse);

            const result = await billService.create(mockBillData);

            expect(apiClient.post).toHaveBeenCalledWith('/bills', mockBillData);
            expect(result.id).toBe('bill-1');
            expect(result.billNumber).toBe('B001');
        });

        it('handles API errors correctly', async () => {
            const mockError = new Error('Network error');
            vi.mocked(apiClient.post).mockRejectedValue(mockError);

            await expect(billService.create({} as any)).rejects.toThrow('Network error');
        });
    });

    describe('getById', () => {
        it('returns bill when found', async () => {
            const mockResponse = {
                data: {
                    data: {
                        id: 'bill-1',
                        billNumber: 'B001',
                        createdAt: '2024-01-01T00:00:00Z',
                    },
                },
            };

            vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

            const result = await billService.getById('bill-1');

            expect(apiClient.get).toHaveBeenCalledWith('/bills/bill-1');
            expect(result).not.toBeNull();
            expect(result?.id).toBe('bill-1');
        });

        it('returns null when bill not found (404)', async () => {
            const mockError: any = {
                response: { status: 404 },
            };
            vi.mocked(apiClient.get).mockRejectedValue(mockError);

            const result = await billService.getById('non-existent');

            expect(result).toBeNull();
        });
    });

    describe('update', () => {
        it('updates a bill successfully', async () => {
            const mockBillData = {
                farmerId: 'farmer-1',
                grossWeight: 150,
                pattiWeight: 5,
                boxCount: 2,
                tutWastage: 3,
                ratePerKg: 55,
                majuri: 400,
            };

            const mockResponse = {
                data: {
                    data: {
                        id: 'bill-1',
                        billNumber: 'B001',
                        ...mockBillData,
                    },
                },
            };

            vi.mocked(apiClient.put).mockResolvedValue(mockResponse);

            const result = await billService.update('bill-1', mockBillData);

            expect(apiClient.put).toHaveBeenCalledWith('/bills/bill-1', mockBillData);
            expect(result.id).toBe('bill-1');
        });
    });

    describe('delete', () => {
        it('deletes a bill successfully', async () => {
            vi.mocked(apiClient.delete).mockResolvedValue({ data: {} });

            await billService.delete('bill-1');

            expect(apiClient.delete).toHaveBeenCalledWith('/bills/bill-1');
        });
    });

    describe('searchWithFilters', () => {
        it('constructs correct query string with filters', async () => {
            const mockResponse = {
                data: {
                    data: {
                        bills: [
                            { id: 'bill-1', billNumber: 'B001', createdAt: '2024-01-01T00:00:00Z' },
                        ],
                    },
                },
            };

            vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

            await billService.searchWithFilters({
                mobile: '9876543210',
                paymentStatus: 'UNPAID',
            });

            expect(apiClient.get).toHaveBeenCalledWith(
                expect.stringContaining('mobileNumber=9876543210')
            );
            expect(apiClient.get).toHaveBeenCalledWith(
                expect.stringContaining('paymentStatus=UNPAID')
            );
        });

        it('returns empty array when no bills found', async () => {
            const mockResponse = {
                data: { data: { bills: [] } },
            };

            vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

            const result = await billService.searchWithFilters({});

            expect(result).toEqual([]);
        });
    });
});

describe('farmerService', () => {
    describe('findByMobile', () => {
        it('returns farmer when found', async () => {
            const mockResponse = {
                data: {
                    data: {
                        id: 'farmer-1',
                        name: 'Test Farmer',
                        mobileNumber: '9876543210',
                    },
                },
            };

            vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

            const result = await farmerService.findByMobile('9876543210');

            expect(result).not.toBeNull();
            expect(result?.name).toBe('Test Farmer');
        });

        it('returns null when farmer not found', async () => {
            const mockError: any = { response: { status: 404 } };
            vi.mocked(apiClient.get).mockRejectedValue(mockError);

            const result = await farmerService.findByMobile('0000000000');

            expect(result).toBeNull();
        });
    });

    describe('upsert', () => {
        it('creates or updates farmer', async () => {
            const farmerData = {
                mobileNumber: '9876543210',
                name: 'New Farmer',
                address: 'Test Address',
            };

            const mockResponse = {
                data: {
                    data: {
                        id: 'farmer-1',
                        ...farmerData,
                    },
                },
            };

            vi.mocked(apiClient.post).mockResolvedValue(mockResponse);

            const result = await farmerService.upsert(farmerData);

            expect(apiClient.post).toHaveBeenCalledWith('/farmers', farmerData);
            expect(result.id).toBe('farmer-1');
        });
    });
});

describe('paymentService', () => {
    describe('recordPayment', () => {
        it('records payment successfully', async () => {
            const mockResponse = {
                data: {
                    data: {
                        id: 'bill-1',
                        paymentStatus: 'PAID',
                        paidAmount: 5000,
                    },
                },
            };

            vi.mocked(apiClient.post).mockResolvedValue(mockResponse);

            const result = await paymentService.recordPayment('bill-1', 5000);

            expect(apiClient.post).toHaveBeenCalledWith(
                '/bills/bill-1/record-payment',
                null,
                { params: { amount: 5000 } }
            );
        });
    });

    describe('getUnpaidStats', () => {
        it('returns unpaid statistics', async () => {
            const mockResponse = {
                data: {
                    data: {
                        count: 10,
                        totalAmount: 50000,
                    },
                },
            };

            vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

            const result = await paymentService.getUnpaidStats();

            expect(result.count).toBe(10);
            expect(result.totalAmount).toBe(50000);
        });
    });
});
